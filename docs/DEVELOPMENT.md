# NovelManagementSystem 开发文档

## 项目概述

**项目名称**: NovelManagementSystem (小说管理系统)  
**版本**: v1.0.0  
**开发框架**: Spring Boot 3.5.7 + MyBatis-Plus  
**Java版本**: JDK 17  
**数据库**: MySQL 5.7+

---

## 一、项目架构

```
NovelManagementSystem/
├── common/              # 公共模块 - 工具类、拦截器、枚举、统一响应
├── pojo/                # 实体模块 - Entity、DTO、VO
├── manage-server/       # 管理端服务 (端口5200)
└── visitor-server/      # 访客端服务 (端口5210)
```

### 1.1 模块依赖关系

```
manage-server → pojo → common
visitor-server → pojo → common
```

### 1.2 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.7 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| MySQL | 5.7+ | 数据库 |
| Redis | - | 缓存 |
| MinIO | 8.5.17 | 文件存储 |
| JWT (jjwt) | 0.12.5 | 认证授权 |
| Argon2 | 2.11 | 密码加密 |
| Lombok | 1.18.28 | 代码简化 |
| Swagger | 1.6.2 | API文档 |

---

## 二、数据库设计

### 2.1 E-R图

```
manager (管理员)
    │
    │ author_id
    ▼
novel (小说)
    │
    ├─► novel_character (角色) ──► character_ability (角色能力)
    │
    ├─► faction (势力)
    │       │
    │       ├─► character_faction (角色-势力关联)
    │       │
    │       └─► location_faction (地点-势力关联)
    │
    ├─► location (地点)
    │
    ├─► item (物品)
    │
    ├─► level (等级阶段)
    │
    └─► ability_level (能力等级)

novel_level1_category (一级分类)
    │
    └─► novel_level2_category (二级分类)
            │
            └─► novel_level3_category (三级分类)
                    │
                    └─► novel_category_relation (小说分类关联)

visitor (访客)
```

### 2.2 数据表清单

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| manager | 管理员表 | id, name, account, password, avatar |
| novel | 小说表 | id, name, sub_name, tags, introduction, author_id, url |
| novel_character | 角色表 | id, name, category, stage, novel_id, url |
| character_ability | 角色能力表 | id, character_id, ability, ability_level |
| faction | 势力表 | id, name, novel_id, description |
| location | 地点表 | id, name, description, novel_id, url |
| item | 物品表 | id, name, novel_id, character_id, quantity, importance |
| level | 等级表 | id, stage, novel_id, sort_order |
| ability_level | 能力等级表 | id, ability, novel_id, sort_order |
| character_faction | 角色-势力关联 | id, character_id, faction_id |
| location_faction | 地点-势力关联 | id, location_id, faction_id |
| visitor | 访客表 | id, name, account, password, vip_level |
| novel_level1_category | 一级分类 | id, name |
| novel_level2_category | 二级分类 | id, name, level1_id |
| novel_level3_category | 三级分类 | id, name, is_hot, level2_id |
| novel_category_relation | 小说分类关联 | id, novel_id, level3_id |

---

## 三、API接口文档

### 3.1 管理端接口 (manage-server:5200)

#### 3.1.1 管理员模块 (ManagerController)

**基础路径**: `/wang/manager`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /login | 管理员登录 | account, password |
| POST | /addManager | 添加管理员 | ManagerDTO |
| DELETE | /delete/{id} | 删除管理员 | id |
| GET | /list | 分页查询管理员 | pageNum, pageSize |
| PUT | /update | 修改管理员 | ManagerDTO |
| GET | /query | 多条件查询 | name, account |

#### 3.1.2 小说模块 (NovelController)

**基础路径**: `/wang/novel`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 新增小说 | NovelDTO |
| DELETE | /delete/{id} | 删除小说 | id |
| GET | /list | 分页查询小说 | pageNum, pageSize |
| GET | /search | 模糊查询小说 | name, subName, pageNum, pageSize |
| PUT | /update | 修改小说 | NovelDTO |

#### 3.1.3 角色模块 (NovelCharacterController)

**基础路径**: `/wang/novel-character`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加角色 | NovelCharacterDTO |
| PUT | /update | 修改角色 | NovelCharacterDTO |
| GET | /list | 分页查询角色 | pageNum, pageSize, novelId |
| GET | /{id} | 查询角色详情 | id |
| GET | /search | 模糊查询角色 | pageNum, pageSize, name, novelId |
| DELETE | /{id} | 删除角色 | id |

#### 3.1.4 势力模块 (FactionController)

**基础路径**: `/wang/faction`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加势力 | FactionDTO |
| PUT | /update | 修改势力 | FactionDTO |
| DELETE | /delete/{id} | 删除势力 | id |
| GET | /{id} | 查询势力详情 | id |
| GET | /list | 分页查询势力 | pageNum, pageSize, novelId |
| GET | /search | 模糊查询势力 | pageNum, pageSize, name, novelId |

#### 3.1.5 地点模块 (LocationController)

**基础路径**: `/wang/location`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加地点 | LocationDTO |
| PUT | /update | 修改地点 | LocationDTO |
| DELETE | /delete/{id} | 删除地点 | id |
| GET | /{id} | 查询地点详情 | id |
| GET | /list | 分页查询地点 | pageNum, pageSize, novelId |
| GET | /search | 模糊查询地点 | pageNum, pageSize, name, novelId |

#### 3.1.6 物品模块 (ItemController)

**基础路径**: `/wang/item`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加物品 | ItemDTO |
| PUT | /update | 修改物品 | ItemDTO |
| DELETE | /delete/{id} | 删除物品 | id |
| GET | /{id} | 查询物品详情 | id |
| GET | /list | 分页查询物品 | pageNum, pageSize, novelId |
| GET | /search | 模糊查询物品 | pageNum, pageSize, name, novelId |
| GET | /character/{characterId} | 查询角色物品 | characterId |

#### 3.1.7 等级模块 (LevelController)

**基础路径**: `/wang/level`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加等级 | LevelDTO |
| POST | /addBatch | 批量添加等级 | List<LevelDTO> |
| PUT | /update | 修改等级 | LevelDTO |
| DELETE | /delete/{id} | 删除等级 | id |
| GET | /{id} | 查询等级详情 | id |
| GET | /list/{novelId} | 查询小说等级列表 | novelId |

#### 3.1.8 能力等级模块 (AbilityLevelController)

**基础路径**: `/wang/ability-level`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 添加能力等级 | AbilityLevelDTO |
| POST | /addBatch | 批量添加能力等级 | List<AbilityLevelDTO> |
| PUT | /update | 修改能力等级 | AbilityLevelDTO |
| DELETE | /delete/{id} | 删除能力等级 | id |
| GET | /{id} | 查询能力等级详情 | id |
| GET | /list/{novelId} | 查询小说能力等级列表 | novelId |

#### 3.1.9 小说分类模块 (NovelCategoryController)

**基础路径**: `/wang/category`

**一级分类**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /level1/add | 添加一级分类 |
| PUT | /level1/update | 修改一级分类 |
| DELETE | /level1/delete/{id} | 删除一级分类 |
| GET | /level1/list | 查询所有一级分类 |

**二级分类**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /level2/add | 添加二级分类 |
| PUT | /level2/update | 修改二级分类 |
| DELETE | /level2/delete/{id} | 删除二级分类 |
| GET | /level2/list/{level1Id} | 查询二级分类 |

**三级分类**
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /level3/add | 添加三级分类 |
| PUT | /level3/update | 修改三级分类 |
| DELETE | /level3/delete/{id} | 删除三级分类 |
| GET | /level3/list/{level2Id} | 查询三级分类 |
| GET | /level3/hot | 查询热门分类 |

**分类树与关联**
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tree | 获取完整分类树 |
| POST | /relation/set | 设置小说分类 |
| GET | /relation/{novelId} | 获取小说分类 |

#### 3.1.10 角色-势力关联模块 (CharacterFactionController)

**基础路径**: `/wang/character-faction`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 为角色添加势力 | characterId, factionId |
| POST | /addBatch | 批量添加势力 | characterId, List<factionId> |
| DELETE | /remove | 移除角色势力 | characterId, factionId |
| DELETE | /clear/{characterId} | 清空角色势力 | characterId |
| GET | /factions/{characterId} | 获取角色的势力列表 | characterId |
| GET | /characters/{factionId} | 获取势力的角色列表 | factionId |

#### 3.1.11 地点-势力关联模块 (LocationFactionController)

**基础路径**: `/wang/location-faction`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /add | 为地点添加势力 | locationId, factionId |
| POST | /addBatch | 批量添加势力 | locationId, List<factionId> |
| DELETE | /remove | 移除地点势力 | locationId, factionId |
| DELETE | /clear/{locationId} | 清空地点势力 | locationId |
| GET | /factions/{locationId} | 获取地点的势力列表 | locationId |
| GET | /locations/{factionId} | 获取势力的地点列表 | factionId |

#### 3.1.12 角色能力模块 (CharacterAbilityController)

**基础路径**: `/wang/AL`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /addAL | 批量添加技能等级 | List<AbilityLevelDTO> |
| POST | /addA | 批量插入角色技能 | List<CharacterAbilityDTO>, characterId |

#### 3.1.13 通用模块 (CommonController)

**基础路径**: `/wang/common`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /upload | 文件上传 | code, file |

---

### 3.2 访客端接口 (visitor-server:5210)

#### 3.2.1 访客模块 (VisitorController)

**基础路径**: `/visitor`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | /register | 访客注册 | VisitorDTO |
| POST | /login | 访客登录 | account, password |
| GET | /info/{visitorId} | 获取访客信息 | visitorId |
| PUT | /update | 修改访客信息 | VisitorDTO |
| PUT | /password | 修改密码 | visitorId, oldPassword, newPassword |

#### 3.2.2 小说浏览模块 (VisitorNovelController)

**基础路径**: `/visitor/novel`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list | 分页查询小说列表 | pageNum, pageSize, keyword, level3Id |
| GET | /hot | 查询热门小说 | limit |
| GET | /{novelId} | 获取小说详情 | novelId |
| GET | /category/{level3Id} | 按分类查询小说 | pageNum, pageSize, level3Id |

#### 3.2.3 角色浏览模块 (VisitorCharacterController)

**基础路径**: `/visitor/character`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list/{novelId} | 查询小说角色列表 | novelId, category |
| GET | /{characterId} | 获取角色详情 | characterId |
| GET | /search | 搜索角色 | novelId, name |

#### 3.2.4 势力浏览模块 (VisitorFactionController)

**基础路径**: `/visitor/faction`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list/{novelId} | 查询小说势力列表 | novelId |
| GET | /{factionId} | 获取势力详情 | factionId |
| GET | /{factionId}/characters | 获取势力下的角色列表 | factionId |
| GET | /{factionId}/locations | 获取势力下的地点列表 | factionId |

#### 3.2.5 地点浏览模块 (VisitorLocationController)

**基础路径**: `/visitor/location`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list/{novelId} | 查询小说地点列表 | novelId |
| GET | /{locationId} | 获取地点详情 | locationId |
| GET | /search | 搜索地点 | novelId, name |

#### 3.2.6 物品浏览模块 (VisitorItemController)

**基础路径**: `/visitor/item`

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | /list/{novelId} | 查询小说物品列表 | novelId |
| GET | /character/{characterId} | 查询角色物品列表 | characterId |
| GET | /{itemId} | 获取物品详情 | itemId |

#### 3.2.7 分类浏览模块 (VisitorCategoryController)

**基础路径**: `/visitor/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tree | 获取分类树 |
| GET | /hot | 获取热门分类 |

---

## 四、枚举定义

### 4.1 角色类别枚举 (CharacterCategoryEnum)

| 值 | 名称 | 说明 |
|---|------|------|
| 0 | MAIN_CHARACTER | 主角 |
| 1 | MAIN_PARTNER | 主角伙伴 |
| 2 | MAIN_PARTNER_OF_PARTNER | 主角伴侣 |
| 3 | MAIN_FAMILY | 主角家人 |
| 4 | CHARACTER_MASTER | 角色师傅 |
| 5 | ENEMY | 反派 |
| 6 | WALL_HEAD_CRAW | 墙头草 |
| 7 | YOU_ARE_BOTH_GOOD_AND_EVIL | 亦正亦邪 |
| 8 | OTHER | 其他 |

### 4.2 物品重要程度

| 值 | 名称 |
|---|------|
| 0 | 普通 |
| 1 | 稀缺 |
| 2 | 罕见 |
| 3 | 珍品 |
| 4 | 孤品 |
| 5 | 传说 |
| 6 | 未定义 |

### 4.3 业务状态码 (BizCodeEnum)

| 状态码 | 枚举值 | 说明 |
|--------|--------|------|
| 10000 | SUCCESS | 操作成功 |
| 10001 | FAIL | 操作失败 |
| 20001 | USER_NOT_FOUND | 用户不存在 |
| 20002 | USER_EXIST | 用户已存在 |
| 20003 | USER_ACCOUNT_ERROR | 账号或密码错误 |
| 20004 | USER_NOT_LOGIN | 用户未登录 |
| 30001 | NOVEL_NOT_FOUND | 小说不存在 |
| 30002 | NOVEL_TITLE_EXIST | 小说标题已存在 |
| 40001 | CHARACTER_NOT_FOUND | 角色不存在 |
| 50001 | FACTION_NOT_FOUND | 势力不存在 |
| 50002 | FACTION_NAME_EXIST | 势力名已存在 |
| 60001 | LOCATION_NOT_FOUND | 地点不存在 |
| 60002 | LOCATION_NAME_EXIST | 地点名已存在 |
| 70001 | ITEM_NOT_FOUND | 物品不存在 |
| 80001 | LEVEL_NOT_FOUND | 等级不存在 |

---

## 五、项目配置

### 5.1 application.yml (manage-server)

```yaml
spring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/nms?useUnicode=true&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
  application:
    name: manager-server

server:
  port: 5200

minio:
  endpoint: http://192.168.43.223:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: nms

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  mapper-locations: classpath:/mapper/*.xml

logging:
  level:
    root: info
```

### 5.2 application.yml (visitor-server)

```yaml
spring:
  datasource:
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/nms?useUnicode=true&characterEncoding=utf8mb4
    driver-class-name: com.mysql.cj.jdbc.Driver
  application:
    name: visitor-server

server:
  port: 5210
```

---

## 六、部署说明

### 6.1 环境要求

- JDK 17+
- MySQL 5.7+
- Redis (可选)
- MinIO (文件存储)

### 6.2 构建命令

```bash
# 打包
mvn clean package -DskipTests

# 运行管理端
java -jar manage-server/target/manage-server-0.0.1-SNAPSHOT.jar

# 运行访客端
java -jar visitor-server/target/visitor-server-0.0.1-SNAPSHOT.jar
```

### 6.3 数据库初始化

执行 `docs/database_fix.sql` 脚本进行数据库初始化和优化。

---

## 七、版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-02-27 | 初始版本，完成管理端所有功能 |
| v1.1.0 | 2026-02-27 | 完成访客端所有功能 |

---

## 八、项目统计

### 8.1 管理端模块统计

| 模块 | 文件数 | 接口数 |
|------|--------|--------|
| 管理员管理 | 4 | 6 |
| 小说管理 | 4 | 5 |
| 角色管理 | 5 | 6 |
| 势力管理 | 5 | 6 |
| 地点管理 | 5 | 6 |
| 物品管理 | 5 | 7 |
| 等级管理 | 5 | 6 |
| 能力等级管理 | 5 | 6 |
| 分类管理 | 10 | 13 |
| 角色-势力关联 | 3 | 6 |
| 地点-势力关联 | 3 | 6 |
| 角色能力管理 | 4 | 2 |
| 文件上传 | 2 | 1 |
| **总计** | **60** | **76** |

### 8.2 访客端模块统计

| 模块 | 文件数 | 接口数 |
|------|--------|--------|
| 访客管理 | 4 | 5 |
| 小说浏览 | 3 | 4 |
| 角色浏览 | 3 | 3 |
| 势力浏览 | 3 | 4 |
| 地点浏览 | 3 | 3 |
| 物品浏览 | 3 | 3 |
| 分类浏览 | 3 | 2 |
| **总计** | **22** | **24** |

---

## 九、优化建议

- [ ] 添加缓存支持 (Redis)
- [ ] 添加统一异常处理
- [ ] 添加日志记录
- [ ] 添加单元测试
- [ ] 添加API文档 (Swagger UI)
- [ ] 添加参数校验
- [ ] 优化数据库索引