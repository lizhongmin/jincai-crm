# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

旅行社 CRM 系统的 Monorepo，采用前后端分离架构：
- `backend/`: Spring Boot 3.3.5 后端
- `frontend/`: Vue 3 + TypeScript + Ant Design Vue 前端

核心功能模块：组织架构 (RBAC)、客户管理、产品管理、订单流程、财务管理、报表、附件、审计、工作流、消息通知。

## 常用命令

### 后端
```bash
# 启动开发服务器
cd backend && mvn spring-boot:run

# 运行测试
cd backend && mvn test

# 构建打包
cd backend && mvn clean package
```

### 前端
```bash
# 启动开发服务器
cd frontend && npm install && npm run dev

# 构建生产版本
cd frontend && npm run build

# 预览构建结果
cd frontend && npm run preview
```

## 环境配置

后端启动时需要配置以下环境变量（可选，有默认值）：
- `DB_URL` / `DB_USER` / `DB_PASSWORD`: 数据库连接信息（默认连接 MySQL）
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD`: Redis 连接信息
- `JWT_SECRET`: JWT 签名密钥
- `UPLOAD_DIR`: 文件上传目录

默认管理员账号：`admin / Admin@123`

前端开发服务器默认代理配置：`/api -> http://localhost:8080`

## 架构要点

### 后端核心机制

**认证授权**
- JWT 无状态认证，通过 `JwtAuthFilter` 验证 Token
- 基于角色的访问控制 (RBAC)，支持菜单权限控制
- Spring Security 配置 (`SecurityConfig`)，使用 `@PreAuthorize` 注解进行方法级权限控制

**数据权限**
- `DataScope` 枚举：SELF、DEPARTMENT、DEPARTMENT_TREE、ALL
- `DataScopeResolver` 根据用户的数据权限范围解析可访问的部门 ID
- 所有 JPA 实体继承 `BaseEntity`，包含 `tenantId`（多租户隔离）

**国际化**
- `I18nService` 统一处理消息国际化
- 消息文件位置：`backend/src/main/resources/i18n/messages*.properties`
- 支持 Accept-Language 请求头切换语言

**审计**
- `BaseEntity` 包含 `createdBy`、`createdAt`、`updatedBy`、`updatedAt` 字段
- `JpaAuditConfig` 启用 JPA 审计功能
- 独立的 `AuditLog` 模块记录操作日志

### 前端架构

**状态管理**
- Pinia store (`auth.ts`) 管理认证状态（token、profile、allowedMenuPaths）
- Token 存储在 localStorage

**API 通信**
- 统一的 axios 实例 (`api/http.ts`)， baseURL 为 `/api`
- 请求拦截器自动添加 `Authorization: Bearer {token}` 头
- 401 响应自动跳转到登录页

**路由**
- Vue Router 动态路由配置，基于菜单权限控制访问

## 模块结构

**后端模块**（按包划分）：
- `auth/`: 认证服务、登录安全、验证码
- `system/`: 部门、用户、角色、权限管理
- `customer/`: 客户、旅客、跟进记录
- `product/`: 线路、出团日期、价格
- `order/`: 订单、订单状态流转
- `finance/`: 应收、应付、退款、财务审核
- `report/`: 销售漏斗、账龄分析、利润分析
- `workflow/`: 工作流模板、任务处理
- `file/`: 附件上传下载
- `audit/`: 审计日志
- `integration/`: 外部事件
- `notification/`: 消息通知

## 代码规范

- 使用 Lombok 简化代码
- 编码：UTF-8
- 编辑器配置遵循 `.editorconfig`：文件缩进 2 空格，Java 文件缩进 4 空格
- 使用 `@PreAuthorize` 注解在 Controller 方法上进行权限控制
- 所有 API 响应统一使用 `ApiResponse<T>` 包装
- 业务异常抛出 `BusinessException`
