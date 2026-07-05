# NovelManagementSystem (NMS)

> 基于 Spring Cloud 微服务架构的小说管理系统，支持读者浏览、作者创作、管理员审核三大角色，集成 AI 写作辅助与内容安全审核。

---

## 项目简介

NMS 是一个多模块的小说内容管理平台，采用 **Spring Cloud Gateway + Nacos + Feign + RabbitMQ** 微服务体系。前端使用 **Vue 3 + TypeScript + Element Plus**，提供访客端、作者端、管理端三个独立门户。

### 核心功能

- **访客端** — 浏览小说、搜索、在线阅读、评论互动、收藏小说、关注作者、阅读进度追踪
- **作者端** — 创作管理小说/章节、AI 写作辅助（续写、润色、知识提取）、评论管理
- **管理端** — 仪表盘统计、小说/作者/访客管理、敏感词管理、三级内容审核（本地→AI→人工）
- **搜索服务** — 基于 Elasticsearch 的全文检索，通过 RabbitMQ 事件实时同步索引
- **AI 服务** — 集成 Spring AI Alibaba + DashScope，提供智能写作与内容审核能力

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **框架** | Spring Boot 3.4.8 / Spring Cloud 2024.0.2 / Spring Cloud Alibaba 2023.0.3.4 |
| **网关** | Spring Cloud Gateway |
| **服务发现** | Nacos |
| **远程调用** | Spring Cloud OpenFeign + LoadBalancer |
| **消息队列** | RabbitMQ |
| **ORM** | MyBatis-Plus 3.5.7 |
| **数据库** | MySQL 8.x |
| **缓存** | Redis (Lettuce) |
| **搜索引擎** | Elasticsearch 8.13.4 |
| **对象存储** | MinIO |
| **AI** | Spring AI Alibaba 1.1.2 + DashScope (DeepSeek) |
| **认证** | JWT (jjwt 0.12.5) + Argon2 密码哈希 |
| **邮箱** | Spring Mail (QQ SMTP) |
| **熔断** | Resilience4j |
| **前端** | Vue 3 / TypeScript / Vite / Element Plus / Pinia / ECharts / Tailwind CSS |

---

## 模块结构

```
NovelManagementSystem
├── common/                # 公共模块（工具类、常量、异常、Feign接口、过滤器、事件定义）
├── pojo/                  # 实体类模块（DTO、VO、Entity）
├── gateway-server/        # API 网关（端口 5100）
├── author-server/         # 作者服务（端口 5200）
├── visitor-server/        # 访客服务（端口 5210）
├── common-server/         # 公共服务（端口 5220）— 文件、邮箱、验证码、敏感词管理
├── ai-server/             # AI 服务（端口 5230）— 内容审核、AI 写作
├── novel-server/          # 小说服务（端口 5250）
├── comment-server/        # 评论服务（端口 5260）
├── search-server/         # 搜索服务（端口 5270）— Elasticsearch 全文检索
├── manager-server/        # 管理服务（端口 5280）
├── novel-web/             # 前端 (Vue 3) — 开发端口 3000
└── pom.xml                # 父 POM
```

## 服务端口

| 服务 | 端口 |
|------|------|
| Gateway | 5100 |
| Author Server | 5200 |
| Visitor Server | 5210 |
| Common Server | 5220 |
| AI Server | 5230 |
| Novel Server | 5250 |
| Comment Server | 5260 |
| Search Server | 5270 |
| Manager Server | 5280 |

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.x
- Redis
- RabbitMQ
- Nacos
- Elasticsearch 8.x
- MinIO

### 1. 启动基础设施

确保以下服务已运行：MySQL、Redis、RabbitMQ、Nacos（127.0.0.1:8848）、Elasticsearch、MinIO。

### 2. 配置环境变量

```bash
# AI 服务 DashScope API Key（接入阿里云百炼平台获取）
Spring_ai_alibaba=your-api-key

# QQ 邮箱 SMTP 授权码（邮箱服务用）
QQ_SMTP/IMAP=your-smtp-password
```

### 3. 初始化数据库

在 MySQL 中创建数据库，编码集使用utf8mb4：

```sql
CREATE DATABASE nms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 构建后端

```bash
mvn clean install -DskipTests
```

### 5. 启动服务

按顺序启动各模块，或在 IDE 中直接运行各模块的 `*Application.java` 主类：

```
Gateway → Common Server → AI Server → Novel Server → Comment Server
→ Author Server → Visitor Server → Search Server → Manager Server
```

### 6. 启动前端

```bash
cd novel-web
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:3000`。

---

## 项目架构

```
                          ┌─────────────────┐
                          │   Nacos 注册中心  │
                          └────────┬────────┘
                                   │
                         ┌─────────▼─────────┐
        ┌────────────────│   Gateway (:5100) ├────────────────┐
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
          └──────────┘  └──────────┘  └────┬─────┘
                                           │
                                    ┌──────▼──────┐
                              RabbitMQ│  ⇄  │Elasticsearch
                                    └─────────────┘
                         ┌──────────┐
                         │ AI Server│
                         │ (:5230)  │
                         └────┬─────┘
                              │
                         DashScope API
```

---

## 内容审核流程

```
用户发布评论
    │
    ▼
本地敏感词检测
    │
    ├── 高风险词 → 自动拒绝
    │
    └── 低风险词/无风险
            │
            ▼
        AI 智能审核 (DashScope)
            │
            ▼
        保存评论（待人工审核）
            │
            ▼
        管理员人工审核
```

---
