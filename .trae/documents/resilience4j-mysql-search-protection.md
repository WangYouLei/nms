# 为 MySQL 降级搜索添加 Resilience4j 保护 + Redis 缓存

## Context

项目 ES 降级到 MySQL 搜索时([searchNovelsFromMySQL](file:///d:/java/NovelManagementSystem/novel-server/src/main/java/com/wang/novel/service/impl/NovelServiceImpl.java#L231))存在以下风险:
- 无限流:ES 宕机后所有请求涌入 MySQL,可能压垮数据库
- 无熔断:MySQL 慢查询时请求堆积,拖垮整个服务
- 无并发隔离:LIKE 全表扫描占满连接池
- 无缓存:重复搜索反复查询数据库

本次改动通过 Resilience4j 三重保护(RateLimiter + CircuitBreaker + Bulkhead) + Redis 缓存解决以上问题。

用户已确认方案:抽取独立 Service、不用 TimeLimiter(超时交给 JDBC socketTimeout)、只加应用层保护(不动数据库索引)。

## 关键发现(依赖问题)

**当前 `spring-cloud-starter-circuitbreaker-resilience4j:3.2.2` 不足以让 @RateLimiter/@Bulkhead 注解生效**:
- `@CircuitBreaker` 可生效(circuitbreaker jar 已传递)
- `@RateLimiter` **不生效**(`resilience4j-ratelimiter` jar 未在依赖链中)
- `@Bulkhead` **不生效**(`resilience4j-bulkhead` 声明为 optional,不传递)

需补充 `resilience4j-ratelimiter:2.2.0` 和 `resilience4j-bulkhead:2.2.0` 两个核心模块。

## 实现步骤

### 1. 补充依赖(2 个文件)

**根 [pom.xml](file:///d:/java/NovelManagementSystem/pom.xml#L189-L194)**(在 `resilience4j-timelimiter` 之后加 dependencyManagement):
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-bulkhead</artifactId>
    <version>2.2.0</version>
</dependency>
```

**[novel-server/pom.xml](file:///d:/java/NovelManagementSystem/novel-server/pom.xml#L28)**(common 依赖之后加,无需 version):
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-ratelimiter</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-bulkhead</artifactId>
</dependency>
```
仅加在 novel-server,避免所有服务被动引入。

### 2. 新建 NovelSearchFallbackService

**路径**: `novel-server/src/main/java/com/wang/novel/service/impl/NovelSearchFallbackService.java`

**职责**: 承接从 NovelServiceImpl 迁移过来的 MySQL 降级查询逻辑,叠加三重保护 + 缓存。

**设计要点**:
- 注入 `NovelMapper`、`CacheService`(用 CacheService 而非 RedisTemplate,统一异常处理)
- `RoleContextUtil.getCurrentUser()` 仍在本类调用(权限过滤 + 缓存 Key 隔离)
- `convertToListVO` 在本类内重新实现(3 行 BeanUtils.copyProperties,避免循环依赖;原 NovelServiceImpl 的同名方法保留,因被 getHotNovels/getNovelsByCategory/getAuthorDetail 复用)
- 三个注解:`@RateLimiter(name="novelSearchRateLimiter")` + `@CircuitBreaker(name="novelSearchCircuitBreaker")` + `@Bulkhead(name="novelSearchBulkhead")`,共用 `fallbackMethod="searchFallback"`
- fallbackMethod 返回 `Result.success(空 PageResult)`,让前端能正常渲染(降级路径不宜再抛错)
- 缓存 Key 必须含 `role + userId` 防越权(Author 只能搜自己的小说,否则 A 命中 B 的缓存)

**方法骨架**:
```java
@RateLimiter(name = "novelSearchRateLimiter", fallbackMethod = "searchFallback")
@CircuitBreaker(name = "novelSearchCircuitBreaker", fallbackMethod = "searchFallback")
@Bulkhead(name = "novelSearchBulkhead", fallbackMethod = "searchFallback")
public Result searchNovelsFromMySQL(NovelSearchDTO dto) {
    // 1. 参数校验(pageNum/pageSize,复用 MAX_PAGE_SIZE=100 / DEFAULT_PAGE_SIZE=10)
    // 2. 取 loginUser → role + userId
    // 3. 构建 cacheKey = buildNovelSearchKey(role, userId, dtoHash)
    // 4. 查缓存(命中直接返回) — try-catch 包裹
    // 5. 构建 LambdaQueryWrapper(权限过滤 + LIKE + 精确筛选 + 排序) — 逻辑同原方法
    // 6. selectPage 查询
    // 7. 转 VO → PageResult
    // 8. 回写缓存(TTL=NOVEL_SEARCH_TTL=300s) — try-catch 包裹
}

private Result searchFallback(NovelSearchDTO dto, Throwable t) {
    log.warn("[降级搜索] 触发 Resilience4j 保护: type={}, msg={}",
             t.getClass().getSimpleName(), t.getMessage());
    return Result.success(PageResult.build(1, 10, 0, List.of()));
}
```

**缓存 Key 格式**: `novel:search:{role}:{userId|guest}:{dtoHash}`
- dtoHash = `Objects.hash(keyword, name, subName, authorId, isHot, isFinished, sortBy, pageNum, pageSize)`
- 不含 categoryId/categoryType/tag(原 MySQL 降级方法未使用这三个字段,只用于 ES)

### 3. 修改 NovelServiceImpl

**文件**: [NovelServiceImpl.java](file:///d:/java/NovelManagementSystem/novel-server/src/main/java/com/wang/novel/service/impl/NovelServiceImpl.java)

改动(最小化):
1. **构造函数注入** `NovelSearchFallbackService`(第 84-100 行加字段 + 参数)
2. **修改 searchNovels()**(第 204-205 行):把 `return searchNovelsFromMySQL(dto)` 改为 `return novelSearchFallbackService.searchNovelsFromMySQL(dto)`
3. **删除**原 `private Result searchNovelsFromMySQL(NovelSearchDTO dto)` 方法(第 228-329 行)
4. **保留** `convertToListVO`(第 630-634 行,被其他方法复用)
5. 清理因删除而未使用的 import(`LambdaQueryWrapper`、`Page` 等如果只被删除的方法用到)

### 4. CacheConstants 新增常量

**文件**: [CacheConstants.java](file:///d:/java/NovelManagementSystem/common/src/main/java/com/wang/common/constants/CacheConstants.java)

- 第 25 行 `NOVEL_CATEGORY_PREFIX` 之后加: `NOVEL_SEARCH_PREFIX = "novel:search:"`
- 第 114 行 `NOVEL_HOT_TTL` 之后加: `NOVEL_SEARCH_TTL = 300L`(5 分钟,搜索结果实时性敏感)
- buildKey 方法区加 `buildNovelSearchKey(String role, Long userId, String dtoHash)`,返回 `NOVEL_SEARCH_PREFIX + role + ":" + (userId!=null?userId:"guest") + ":" + dtoHash`

### 5. application.yml 配置

**文件**: [novel-server/application.yml](file:///d:/java/NovelManagementSystem/novel-server/src/main/resources/application.yml)

**5.1 datasource url 加超时参数**(第 40 行,防止 JDBC 无限等待):
```
&connectTimeout=2000&socketTimeout=30000
```

**5.2 文件末尾追加 resilience4j 配置块**(与 spring 同级):
```yaml
resilience4j:
  ratelimiter:
    instances:
      novelSearchRateLimiter:
        limit-for-period: 10          # 10 QPS(LIKE 查询 50-200ms,单实例峰值约 2 并发)
        limit-refresh-period: 1s
        timeout-duration: 0           # 超限立即拒绝,不排队
        register-health-indicator: true
  circuitbreaker:
    instances:
      novelSearchCircuitBreaker:
        failure-rate-threshold: 50         # 失败率 50% 熔断
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 3s   # 配合 JDBC socketTimeout
        wait-duration-in-open-state: 30s
        sliding-window-size: 20
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 5
        automatic-transition-from-open-to-half-open-enabled: true
        register-health-indicator: true
  bulkhead:
    instances:
      novelSearchBulkhead:
        max-concurrent-calls: 20     # 信号量隔离,HikariCP 默认 10 连接,留余量
        max-wait-duration: 0         # 超限立即拒绝
```

## 改动文件清单

| 文件 | 操作 | 改动量 |
|---|---|---|
| [pom.xml](file:///d:/java/NovelManagementSystem/pom.xml)(根) | 修改:加 2 个 dependencyManagement | +12 行 |
| [novel-server/pom.xml](file:///d:/java/NovelManagementSystem/novel-server/pom.xml) | 修改:加 2 个 dependency | +10 行 |
| [CacheConstants.java](file:///d:/java/NovelManagementSystem/common/src/main/java/com/wang/common/constants/CacheConstants.java) | 修改:加前缀/TTL/buildKey | +20 行 |
| NovelSearchFallbackService.java | **新建** | ~150 行 |
| [NovelServiceImpl.java](file:///d:/java/NovelManagementSystem/novel-server/src/main/java/com/wang/novel/service/impl/NovelServiceImpl.java) | 修改:注入新 Service + 改 searchNovels + 删原方法 | -100 +10 行 |
| [application.yml](file:///d:/java/NovelManagementSystem/novel-server/src/main/resources/application.yml) | 修改:加 resilience4j 配置 + JDBC 超时 | +25 行 |

## 验证步骤

1. **编译验证**: `mvn clean compile -pl novel-server -am` 通过
2. **依赖生效验证**: 启动 novel-server,日志无 `RateLimiterAutoConfiguration` / `BulkheadAutoConfiguration` 的 Negative matches(可选加 `logging.level.io.github.resilience4j=DEBUG`)
3. **注解生效验证**: 临时把 `limit-for-period: 1`,连续调搜索接口 2 次,第 2 次应返回空列表 + 日志 `触发 Resilience4j 保护`
4. **缓存生效验证**: 相同 DTO 连续调用 2 次,第 2 次日志应打印 `缓存命中`
5. **越权验证**: Author A 和 Author B 用相同 keyword 搜索,B 不会命中 A 的缓存(日志 `缓存未命中`)
6. **熔断验证**: 临时把 `slow-call-duration-threshold: 100ms`,关停 ES 触发降级,连续 10+ 次后进入 open 状态,调用立即返回 fallback

## 注意事项

- **AOP 自调用**: 方案把方法移到独立 Bean,由 NovelServiceImpl 跨 Bean 调用,AOP 正常生效
- **缓存一致性**: TTL 5 分钟,降级路径可接受;如需更强一致性可在 addNovel/deleteNovel/updateNovel 调 `cacheService.deleteByPattern("novel:search:*")`,当前不加(损失命中率)
- **序列化**: `PageResult` 未实现 Serializable;CacheService 用 Jackson 序列化应无问题,若报错需加 `implements Serializable`
- **Bulkhead 类型**: 默认 SemaphoreBulkhead(信号量),适合同步 MySQL 调用,无需 ThreadPoolBulkhead
