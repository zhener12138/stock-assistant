# Stock Assistant 智能库存助手

基于 Spring AI + Ollama 的智能库存管理系统，支持通过自然语言进行库存查询与管理操作。

> **项目来源：** 本项目 fork 自 [saberdaagi/stock-assistant](https://github.com/saberdaagi/stock-assistant)，在原项目基础上进行了多项功能改进和优化。

---

## 我的改造内容

以下是我在原项目基础上新增的改进：

### 已完成的改造

| 改造项 | 说明 |
|--------|------|
| **System Prompt 约束** | 为 ChatClient 添加了系统提示词，限制 LLM 仅回答库存管理相关问题，拒绝无关查询 |
| **乐观锁并发控制** | 在 InventoryItemEntity 中添加 `@Version` 字段，库存更新时自动检测并发冲突并抛出 `OptimisticLockingFailureException` |
| **库存预警功能** | 新增 `InventoryAlertService`，支持检测低于阈值的库存项并输出告警日志 |
| **缺失的依赖修复** | 补全了 `spring-boot-starter-data-redis` 和 `h2` 依赖，确保项目编译通过 |
| **缺失的 port/out 接口** | 补全了 `InventoryRepositoryPort`、`ProductRepositoryPort`、`WarehouseRepositoryPort` 接口定义 |
| **配置文件示例** | 添加了 `application-example.yml` 作为配置参考模板 |

---

## 项目架构

```
stock-assistant/
├── pom.xml                          # 父 POM
├── docker-compose.yml               # Docker 编排文件
├── Dockerfile                       # 多阶段构建
├── stock-spec/                      # OpenAPI 规范定义
│   └── v1/stock-api-v1.yaml
├── stock-server/                    # 后端服务（六边形架构）
│   ├── stock-server-api/            # API 层（Controller + 接口生成）
│   ├── stock-server-domain/         # 领域层（业务逻辑 + 领域模型）
│   ├── stock-server-persistence/    # 持久层（JPA + Redis 缓存）
│   └── stock-server-starter/        # 启动模块
└── stock-ai-client/                 # AI 对话客户端
    ├── src/main/java/.../tools/     # @Tool 注解工具类
    └── src/main/java/.../config/    # ChatClient 配置
```

### 架构特点

- **六边形架构（端口适配器模式）**：domain 层完全独立，不依赖任何外部框架
- **API 契约先行**：OpenAPI 3.0 规范定义接口，通过 openapi-generator 自动生成服务端桩代码和客户端 SDK
- **AI 能力集成**：Spring AI + Ollama 本地大模型，通过 `@Tool` 注解将 Java 方法暴露为 LLM 可调用工具

---

## 技术栈

| 模块 | 技术 |
|------|------|
| 语言 | Java 21 |
| 框架 | Spring Boot 3.4, Spring AI 1.0.0-M7 |
| 数据库 | PostgreSQL 15（生产）、H2（本地开发） |
| 缓存 | H2（本地开发）、PostgreSQL（生产） |
| AI 推理 | Ollama (llama3.2) |
| 代码生成 | OpenAPI Generator + MapStruct |
| 部署 | Docker Compose |

---

## 快速启动

### 方式一：Docker Compose（推荐）

```bash
docker compose up -d
```

启动后包含以下服务：
- PostgreSQL 数据库（端口 5432）
- Ollama + llama3.2 模型（端口 11434）
- Stock Server（端口 8080）
- AI Client（端口 8060）

### 方式二：本地开发

1. 构建项目：
   ```bash
   mvn clean install -DskipTests
   ```
2. 启动 Stock Server：
   ```bash
   cd stock-server/stock-server-starter
   mvn spring-boot:run
   ```
3. 启动 AI Client：
   ```bash
   cd stock-ai-client
   mvn spring-boot:run
   ```

### 使用示例

```bash
# 查询产品
curl -X POST http://localhost:8060/api/chat/process \
  -H "Content-Type: application/json" \
  -d '{"prompt": "查找名为 AlphaPhone X 的产品"}'

# 创建产品
curl -X POST http://localhost:8060/api/chat/process \
  -H "Content-Type: application/json" \
  -d '{"prompt": "添加一个名为 Wireless Mouse 的产品，SKU为 MOU-987，类别 ELECTRONICS"}'
```

---
