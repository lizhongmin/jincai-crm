# Travel CRM Monorepo

面向旅行社的一期 CRM 系统，包含：

- `backend/`：Spring Boot 3 后端（组织、RBAC、客户、产品、订单流程、财务、报表、附件、审计）
- `frontend/`：Vue 3 + TypeScript + Ant Design Vue 管理后台（PC）

## 快速启动

### 1) 后端

```bash
cd backend
mvn spring-boot:run
```

环境变量（可选）：

- `DB_URL` / `DB_USER` / `DB_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT`
- `JWT_SECRET`
- `UPLOAD_DIR`

默认管理员账号：`admin / Admin@123`

### 2) 前端

```bash
cd frontend
npm install
npm run dev
```

默认代理：`/api -> http://localhost:8080`

## 已实现 API（核心）

- 鉴权：`POST /auth/login`、`GET /auth/me`
- 菜单权限：`GET /permissions/menus`
- 角色授权：`POST /roles/{id}/grant`、`GET /roles/{id}/permissions`
- 组织：`CRUD /departments`、`CRUD /users`、`CRUD /roles`
- 客户：`CRUD /customers`、`CRUD /customers/{id}/travelers`、`POST /customers/import`
- 产品：`CRUD /routes`、`CRUD /departures`、`CRUD /departures/{id}/prices`
- 订单：`CRUD /orders`、`POST /orders/{id}/submit`、`POST /orders/{id}/approve`、`POST /orders/{id}/reject`
- 流程：`GET /workflow/templates`、`POST /workflow/templates`、`PUT /workflow/templates/{id}`、`DELETE /workflow/templates/{id}`
- 财务：`POST /orders/{id}/receivables`、`POST /receipts`、`POST /refunds`、`POST /payables`、`POST /payments`、`POST /finance/{id}/review`
- 报表：`GET /reports/sales-funnel`、`GET /reports/cashflow-aging`、`GET /reports/profit`
- 报表导出：`GET /reports/sales-funnel/export`、`GET /reports/cashflow-aging/export`、`GET /reports/profit/export`

## 测试

```bash
cd backend
mvn test
```

当前环境下若出现 `JAVA_HOME` 未配置，请先安装 JDK 17 并设置 `JAVA_HOME`。