# NovelManagementSystem (NMS)

> 基于 **Spring Cloud 微服务架构** 的小说内容管理平台，覆盖「读者浏览 — 作者创作 — 管理员审核」三大角色闭环，集成 **AI 写作辅助** 与 **三级内容安全审核**，提供基于 Elasticsearch 的高性能全文检索。

---

## 📖 项目简介

NMS 是一个多模块、分布式的小说内容管理平台。系统按业务域拆分为 9 个独立微服务，通过 **Spring Cloud Gateway** 统一入口、**Nacos** 注册发现、**OpenFeign** 服务间调用、**RabbitMQ** 异步解耦（ES 数据同步、事件广播），并集成 **Spring AI Alibaba + DashScope** 提供智能写作与内容审核能力。

前端采用 **Vue 3 + TypeScript**，提供访客端、作者端、管理端三个门户。

### 核心能力一览

| 角色 | 核心功能 |
|------|----------|
| **访客 Visitor** | 浏览/搜索小说、在线阅读、评论与楼中楼回复、收藏小说、关注作者、阅读进度追踪（断点续读） |
| **作者 Author** | 创作管理小说/章节、AI 写作辅助（续写 / 润色 / 知识提取 / 风格学习 / 自审）、评论管理 |
| **管理员 Manager** | 仪表盘统计、小说/作者/访客管理、敏感词词库管理、人工审核（三级审核最终环节） |
| **平台能力** | 全文检索（ES）、内容安全审核（本地 DFA → AI → 人工）、文件存储（MinIO）、邮件通知 |

---

## ✨ 项目亮点（技术难点与设计思考）

> 以下为项目中最能体现技术深度与工程思考的设计点，适合面试交流展开。

### 1. 防绕过的统一鉴权链路

系统设计了一套「网关统一鉴权 + 下游零信任校验」的认证方案，杜绝服务被绕过网关直连或请求头伪造：

```
客户端请求
   │  携带 token
   ▼
┌─────────────────────────────────────────────────────┐
│ Gateway · AuthGlobalFilter                          │
│  1. 路由匹配（AntPathMatcher）找到鉴权规则         │
│  2. 校验白名单（登录/注册/公开接口放行）           │
│  3. 解析 JWT，校验签名 + 提取 role                  │
│  4. 角色 RBAC 校验（VISITOR/AUTHOR/MANAGER）       │
│  5. 校验通过：写入 X-User-* 头 + X-Gateway-Auth 密钥 │
└──────────────────────┬──────────────────────────────┘
                       │  透传用户信息头
        ┌──────────────┴───────────────┐
        ▼                              ▼
┌─────────────────────┐     ┌──────────────────────────┐
│ UserInfoFilter      │     │ /internal/** 路径        │
│ (最高优先级 Filter) │     │ 网关层 SetStatus=403 屏蔽 │
│ 校验 X-Gateway-Auth │     │ 仅允许微服务间 Feign 调用 │
│ → 反伪造            │     └──────────────────────────┘
│ 注入 ThreadLocal    │
└─────────┬───────────┘
          │  RoleContextUtil.getCurrentUser()
          ▼
┌─────────────────────────────┐
│ FeignRequestInterceptor    │
│ 服务间调用透传 X-User-* 头  │
│ 保证下游也能拿到用户上下文  │
└─────────────────────────────┘
```

**关键设计点：**
- **`X-Gateway-Auth` 密钥头**：网关写入固定密钥，下游 `UserInfoFilter` 校验该密钥，**防止恶意请求直接伪造 `X-User-Id` 头访问下游服务**。
- **`/internal/**` 路径屏蔽**：网关对所有服务的内部接口返回 `403`，内部接口仅供 Feign 调用，避免敏感能力外泄。
- **角色路由级控制**：在 `application.yml` 中以路径前缀声明式配置 `allowed-roles`，如 `/api/ai-server/**` 仅 `AUTHOR` 可访问。

### 2. 三级内容安全审核（本地 → AI → 人工）

针对 UGC 内容（评论、章节），设计了分层递进、性能与精度兼顾的审核机制：

```
用户提交内容
    │
    ▼
① 本地敏感词检测（DFA 算法，common-server）
    │  内存中树形匹配，O(n) 复杂度，毫秒级
    ├── 命中「高危词」→ 直接拒绝（20005 HIGH_RISK_SENSITIVE_WORD）
    │
    └── 命中「低危词」/ 无命中
            │
            ▼
     ② AI 智能审核（ai-server · DashScope 大模型）
        │  Prompt 模板化，返回安全评分 + 风险等级
        ├── 不通过 → 回滚已落库数据，返回拦截
        │
        └── 通过 / 服务降级
                │
                ▼
        ③ 人工审核（manager-server · ManualAudit）
            管理员最终裁定，置 audit_level
```

**工程细节：**
- **审核在事务外执行**：避免长事务长时间持有 DB 连接（AI 调用耗时不可控），仅最终 DB 写入在 `TransactionTemplate` 内。
- **AI 审核失败容错**：Feign 调用异常时，评论已落库并标记 `audit_level=0`（待人工审核），保证用户体验不因 AI 服务抖动而中断。
- **AI 拒绝时数据回滚**：通过 `transactionTemplate` 补偿式删除已保存评论 / 恢复原始内容，保证数据一致。

### 3. DFA 敏感词检测算法

自实现基于 **确定性有限自动机（DFA）** 的敏感词检测（`common` 模块 `DFAUtil`），将敏感词库构建为前缀树，文本扫描时按字符沿树状态转移，单次遍历即可完成全部词匹配：

- **`@PostConstruct` 启动加载**：服务启动时从 MySQL 拉取全部启用敏感词，构建 DFA 树并建立 `wordLevelMap`（词→等级）。
- **增删后热刷新**：新增敏感词增量加入 DFA 树，删除/批量操作后全量重建。
- **分级处理**：`SensitiveLevelEnum` 区分 `HIGH`（直接拒绝）/ `LOW`（需人工审核），与三级审核联动。
- **扩展性预留**：注释中指出当词库达到十万级或性能瓶颈时，可平滑升级至 **AC 自动机**。

### 4. 搜索服务：ES + RabbitMQ 数据一致性方案

`search-server` 基于 Elasticsearch 提供检索能力，并通过「事件驱动增量同步 + Feign 全量同步」双通道保证 MySQL 与 ES 数据一致：

- **增量同步**：业务服务（`novel-server`/`author-server`）在数据变更后通过 `NovelEventPublisher` 发布事件到 RabbitMQ，`search-server` 的 `NovelUpdatedListener`/`AuthorUpdatedListener` 监听消费，回调 Feign 拉取最新数据写入 ES。
- **全量同步**：`DataSyncServiceImpl.syncAll()` 提供索引初始化与全量重建入口。
- **冗余存储设计**：`NovelDocument` 冗余了作者信息、分类信息，避免搜索时跨服务联表，单次 ES 查询即可返回列表所需全部字段。
- **检索能力**：`NativeQuery` + `BoolQuery`，支持多字段权重检索（`name^3`、`subName^2`）、关键词高亮、分类聚合统计、前缀搜索建议。

**ES 索引设计**：文本字段采用 `ik_max_word`（索引时最大粒度分词）/ `ik_smart`（查询时智能分词）组合，兼顾召回率与精度；分类名同时维护 `Text`（分词搜索）与 `Keyword`（聚合统计）双字段。

### 5. Resilience4j 三重保护 + 多级缓存降级

当 ES 不可用时，`NovelSearchFallbackService` 提供降级搜索方案，并施加三重保护防止 MySQL 被打爆：

```java
@RateLimiter(name = "novelSearchRateLimiter", fallbackMethod = "searchFallback")   // 限流：防雪崩
@CircuitBreaker(name = "novelSearchCircuitBreaker", fallbackMethod = "searchFallback") // 熔断：DB 慢时快速失败
@Bulkhead(name = "novelSearchBulkhead", fallbackMethod = "searchFallback")         // 隔离：保护连接池
public Result searchNovelsFromMySQL(NovelSearchDTO dto) { ... }
```

- **Redis 缓存**：查询结果按 `role + userId + dtoHash` 构建缓存 Key，**Key 内含角色与用户 ID 防越权**。
- **兜底返回**：三重保护触发时返回用户请求页码的空列表，避免前端分页错乱。

### 6. 评论系统：N+1 查询优化与楼中楼树结构

`comment-server` 的 `CommentServiceImpl` 针对评论列表场景做了批量查询优化：

- **树结构设计**：`rootId`（根评论）+ `parentId`（父评论）支持楼中楼回复。
- **批量 VO 转换**：`batchConvertToVO` 先收集所有 `userId`/`novelId`，按用户类型分组（访客/作者/管理员）批量 Feign 拉取头像与小说作者 ID，**一次列表查询只发 4 次 Feign 调用**，彻底解决 N+1 问题。
- **安全分页**：`safePageNum`/`safePageSize` 防止恶意翻页（页码封顶 10000，页大小封顶 100）。
- **BFS 级联删除**：删除评论时用广度优先迭代收集所有子回复，避免递归栈溢出。

### 7. AI 写作辅助：知识库 Wiki 式检索注入

`ai-server` 提供完整的 AI 写作辅助能力链，核心是 **基于知识库的结构化 Prompt 工程**：

- **知识库管理**：`KnowledgeItem` 支持角色设定、场景设定、剧情线索、主题、关键物品、写作风格六类知识条目。
- **Wiki 式检索**（`KnowledgeRetriever`）：从 MySQL 取小说全部知识项，按类型组织为结构化 Markdown 文档（角色/场景/剧情分章节），截断至 8000 字注入 Prompt，让 AI 续写时保持人设与世界观一致。
- **写作能力矩阵**：
  - `ContinuationEngine` — 章节续写（结合上下文 + 知识库）
  - `PolishingEngine` — 文本润色
  - `KnowledgeExtractor` — 从正文反向提取知识入库
  - `SelfReviewer` — AI 自审（生成后自检）
  - `ContentReviewer` — 内容合规审查（色情/政治/价值观，返回安全评分与风险等级）
- **双模型配置**：文本模型（DeepSeek，写作与审核）+ 语音模型（sambert，语音合成），通过 `ChatClient` 抽象统一调用。

---

## 🛠 技术栈

### 后端

| 层级 | 技术 | 版本 |
|------|------|------|
| **基础框架** | Spring Boot | 3.4.8 |
| **微服务** | Spring Cloud / Spring Cloud Alibaba | 2024.0.2 / 2023.0.3.4 |
| **网关** | Spring Cloud Gateway | — |
| **服务发现/配置** | Nacos | — |
| **服务间调用** | Spring Cloud OpenFeign + LoadBalancer | 4.2.2 |
| **熔断限流** | Resilience4j（CircuitBreaker / RateLimiter / Bulkhead） | 2.2.0 |
| **消息队列** | RabbitMQ（spring-boot-starter-amqp） | — |
| **ORM** | MyBatis-Plus | 3.5.7 |
| **数据库** | MySQL | 8.x |
| **缓存** | Redis（Lettuce） | — |
| **搜索引擎** | Elasticsearch（elasticsearch-java client） | 8.13.4 |
| **对象存储** | MinIO | 8.5.17 |
| **AI** | Spring AI Alibaba + DashScope（DeepSeek 文本 / sambert 语音） | 1.1.2.2 |
| **认证** | JWT（jjwt）+ Argon2id 密码哈希 | 0.12.5 / 2.11 |
| **邮件** | Spring Mail（QQ SMTP） | — |
| **工具库** | Hutool / Jackson | 5.8.24 / — |
| **JDK** | Java | 17 |

### 前端

| 分类 | 技术 |
|------|------|
| **框架** | Vue 3.4 + TypeScript 5.4 |
| **构建** | Vite 5 |
| **UI** | Element Plus 2.6 + Tailwind CSS 3.4 |
| **状态/路由** | Pinia 2 + Vue Router 4 |
| **HTTP** | Axios |
| **图表** | ECharts 5 + vue-echarts |
| **其他** | VueUse / Iconify / dayjs / Sass |
| **工程化** | ESLint + Prettier + unplugin-auto-import / unplugin-vue-components |

---

## 🏗 模块结构

```
NovelManagementSystem
├── common/                # 公共模块：DFA工具、JWT、Argon2、统一响应Result、
│                          # 全局异常、Feign接口与Fallback、UserInfoFilter、
│                          # FeignRequestInterceptor、事件定义、枚举
├── pojo/                  # 实体类模块：Entity / DTO / VO
├── gateway-server/        # API 网关 (:5100)：路由、鉴权、跨域、内部接口屏蔽
├── author-server/         # 作者服务 (:5200)：创作、章节、AI 写作调用入口
├── visitor-server/        # 访客服务 (:5210)：收藏、关注、阅读进度、浏览历史
├── common-server/        # 公共服务 (:5220)：文件(MinIO)、邮箱、验证码、敏感词管理
├── ai-server/             # AI 服务 (:5230)：写作辅助引擎、内容审核、知识库
├── novel-server/          # 小说服务 (:5250)：小说/章节/分类、排行榜、降级搜索
├── comment-server/        # 评论服务 (:5260)：评论树、三级审核、批量VO转换
├── search-server/         # 搜索服务 (:5270)：ES 全文检索、数据同步、搜索建议
├── manager-server/        # 管理服务 (:5280)：仪表盘统计、人工审核、排行榜同步任务
├── novel-web/             # 前端 (Vue 3)：开发端口 3000
└── pom.xml                # 父 POM（统一依赖管理）
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 5100 | 统一入口，JWT 鉴权 + 路由 |
| Author Server | 5200 | 作者业务 |
| Visitor Server | 5210 | 访客业务 |
| Common Server | 5220 | 公共能力（文件/邮件/验证码/敏感词） |
| AI Server | 5230 | AI 写作与审核 |
| Novel Server | 5250 | 小说核心业务 |
| Comment Server | 5260 | 评论业务 |
| Search Server | 5270 | ES 检索 |
| Manager Server | 5280 | 管理后台 |

---

## 🧩 项目架构

```
                          ┌─────────────────┐
                          │  Nacos 注册/配置 │
                          └────────┬────────┘
                                   │
                         ┌─────────▼─────────┐
        ┌────────────────│ Gateway (:5100)   │────────────────┐
        │   JWT鉴权+角色路由 │ (AuthGlobalFilter)│  /internal/** 屏蔽│
        │                └───────────────────┘                │
        │                                                     │
  ┌─────▼──────┐  ┌──────────┐  ┌──────────┐       ┌───────▼──────┐
  │ Author Srv │  │Visitor Srv│  │ Novel Srv │       │ Manager Srv  │
  │   (:5200)  │  │  (:5210)  │  │  (:5250)  │       │   (:5280)    │
  └─────┬──────┘  └─────┬────┘  └────┬─────┘       └───────┬──────┘
        │               │            │                     │
        └───────┬───────┴─────┬──────┴──────┬─────────────┘
                │             │             │
          ┌─────▼────┐  ┌─────▼────┐  ┌────▼─────┐
          │ Common   │  │ Comment  │  │  Search  │
          │ (:5220)  │  │ (:5260)  │  │ (:5270)  │
          │ 文件/邮件 │  │ 评论+审核 │  │  ES检索  │
          │ 验证码/   │  └────┬─────┘  └────┬─────┘
          │ 敏感词DFA │       │             │ 事件
          └──────────┘       │      ┌──────▼──────┐
                             │  RabbitMQ  ⇄  ES 同步
                             │      └─────────────┘
                       ┌─────▼──────┐
                       │  AI Server │ (:5230)
                       │  写作/审核  │
                       └─────┬──────┘
                             │
                       DashScope API
                  (DeepSeek 文本 / sambert 语音)
```

---

## 📐 核心流程

### 统一鉴权流程

详见上方「项目亮点 1」，核心为：**网关 JWT 解析 + 角色路由 → 下游 `X-Gateway-Auth` 密钥校验防伪造 → Feign 透传用户上下文**。

### 内容审核流程

详见上方「项目亮点 2」，核心为：**本地 DFA（高危直接拒/低危进人工）→ AI 大模型审核（不通过回滚）→ 人工终审**。

### ES 数据同步流程

```
业务数据变更 (novel-server / author-server)
        │
        │ NovelEventPublisher.publish()
        ▼
     RabbitMQ (novel.updated / author.updated)
        │
        ▼
 Search Server Listener 消费
        │
        │ Feign 回调业务服务拉取最新数据
        ▼
    写入 Elasticsearch 索引
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.x
- Redis
- RabbitMQ
- Nacos 2.x（127.0.0.1:8848）
- Elasticsearch 8.x（需安装 `analysis-ik` 中文分词插件）
- MinIO

### 1. 启动基础设施

确保以下服务已运行：MySQL、Redis、RabbitMQ、Nacos、Elasticsearch（含 IK 分词插件）、MinIO。

### 2. 配置环境变量

```bash
# AI 服务 DashScope API Key（阿里云百炼平台获取）
Spring_ai_alibaba=your-api-key

# QQ 邮箱 SMTP 授权码（邮箱服务用）
QQ_SMTP/IMAP=your-smtp-password
```

### 3. 初始化数据库

在 MySQL 中创建数据库（编码 utf8mb4）：

```sql
CREATE DATABASE nms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> 建表脚本由业务模块通过 MyBatis-Plus 维护，按各模块实体类与 Mapper 执行即可。

### 4. 构建后端

```bash
mvn clean install -DskipTests
```

### 5. 启动服务

按顺序启动各模块，或在 IDE 中直接运行各模块的 `*Application.java` 主类：

```
Gateway → Common Server → Novel Server → Comment Server
→ AI Server → Author Server → Visitor Server
→ Search Server → Manager Server
```

### 6. 启动前端

```bash
cd novel-web
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:3000`。

---

## 📁 关键代码索引

| 关注点 | 关键类 |
|--------|--------|
| 网关鉴权 | `gateway-server/.../filter/AuthGlobalFilter` |
| 鉴权规则配置 | `gateway-server/src/main/resources/application.yml`（`auth.rules`） |
| 下游用户上下文 | `common/.../filter/UserInfoFilter` + `utils/RoleContextUtil` |
| Feign 头透传 | `common/.../feign/FeignRequestInterceptor` |
| DFA 敏感词 | `common/.../utils/DFAUtil` + `common-server/.../SensitiveWordServiceImpl` |
| 三级审核（评论） | `comment-server/.../CommentServiceImpl` |
| AI 写作引擎 | `ai-server/.../engine/*`（Continuation/Polishing/ContentReview/SelfReview） |
| 知识库检索 | `ai-server/.../retriever/KnowledgeRetriever` |
| ES 数据同步 | `search-server/.../DataSyncServiceImpl` + `listener/*` |
| ES 检索 | `search-server/.../SearchServiceImpl` |
| 熔断降级搜索 | `novel-server/.../NovelSearchFallbackService` |

---

## 📌 说明

- 本项目为个人学习/求职展示用途，聚焦微服务架构与工程实践。
- 配置文件（Nacos/数据库密码等）为本地开发默认值，生产部署请通过 Nacos 配置中心与环境变量管理。
