# Travel CRM Monorepo

闈㈠悜鏃呰绀剧殑涓€鏈?CRM 绯荤粺锛屽寘鍚細

- `backend/`锛歋pring Boot 3 鍚庣锛堢粍缁囥€丷BAC銆佸鎴枫€佷骇鍝併€佽鍗曟祦绋嬨€佽储鍔°€佹姤琛ㄣ€侀檮浠躲€佸璁★級
- `frontend/`锛歏ue 3 + TypeScript + Ant Design Vue 绠＄悊鍚庡彴锛圥C锛?
## 蹇€熷惎鍔?
### 1) 鍚庣

```bash
cd backend
mvn spring-boot:run
```

鐜鍙橀噺锛堝彲閫夛級锛?
- `DB_URL` / `DB_USER` / `DB_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT`
- `JWT_SECRET`
- `UPLOAD_DIR`

榛樿绠＄悊鍛樿处鍙凤細`admin / Admin@123`

### 2) 鍓嶇

```bash
cd frontend
npm install
npm run dev
```

榛樿浠ｇ悊锛歚/api -> http://localhost:8080`

## 宸插疄鐜?API锛堟牳蹇冿級

- 閴存潈锛歚POST /auth/login`銆乣GET /auth/me`
- 鑿滃崟鏉冮檺锛歚GET /permissions/menus`
- 瑙掕壊鎺堟潈锛歚POST /roles/{id}/grant`銆乣GET /roles/{id}/permissions`
- 缁勭粐锛歚CRUD /departments`銆乣CRUD /users`銆乣CRUD /roles`
- 瀹㈡埛锛歚CRUD /customers`銆乣CRUD /customers/{id}/travelers`銆乣POST /customers/import`
- 浜у搧锛歚CRUD /routes`銆乣CRUD /departures`銆乣CRUD /departures/{id}/prices`
- 璁㈠崟锛歚CRUD /orders`銆乣POST /orders/{id}/submit`銆乣POST /orders/{id}/approve`銆乣POST /orders/{id}/reject`
- 娴佺▼锛歚GET /workflow/templates`銆乣POST /workflow/templates`銆乣PUT /workflow/templates/{id}`銆乣DELETE /workflow/templates/{id}`
- 璐㈠姟锛歚POST /orders/{id}/receivables`銆乣POST /receipts`銆乣POST /refunds`銆乣POST /payables`銆乣POST /payments`銆乣POST /finance/{id}/review`
- 鎶ヨ〃锛歚GET /reports/sales-funnel`銆乣GET /reports/cashflow-aging`銆乣GET /reports/profit`
- 鎶ヨ〃瀵煎嚭锛歚GET /reports/sales-funnel/export`銆乣GET /reports/cashflow-aging/export`銆乣GET /reports/profit/export`

## 娴嬭瘯

```bash
cd backend
mvn test
```

褰撳墠鐜涓嬭嫢鍑虹幇 `JAVA_HOME` 鏈厤缃紝璇峰厛瀹夎 JDK 17 骞惰缃?`JAVA_HOME`銆?

## 自动化测试（本地执行）

### Frontend（Vitest + Playwright）

```bash
cd frontend
npm install
npm run test:unit
npm run test:coverage
npm run test:e2e:chromium
```

- 覆盖率报告目录：`frontend/coverage/unit/`
- Web 自动化默认使用 Chromium

### Backend（Spring Boot Test + MockMvc）

```bash
cd backend
mvn test
mvn verify -Pcoverage-gate
```

- 覆盖率报告目录：`backend/target/coverage/backend/`
- `coverage-gate` profile 开启 70% 行覆盖率门禁

### Miniapp（Node Test Runner）

```bash
cd miniapp
npm run test
```

