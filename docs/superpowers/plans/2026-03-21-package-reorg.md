# Package Restructure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize backend modules into module-aligned packages (`controller`, `service`, `repository`, `entity`) and make each controller delegate exclusively to service beans so no business code resides inside controllers.

**Architecture:** Each domain (`org`, `customer`, `product`, `order`, `workflow`, `finance`, `rbac`) gets layered packages (e.g., `com.jincai.crm.system.controller`, `.service`, `.repository`, `.entity`). Controllers remain entry points, services hold business logic, repositories remain JPA interfaces, DTOs live near controllers/services.

**Tech Stack:** Spring Boot 3 + Spring Data JPA + MyBatis-Plus/JPA (existing mix), Ant Design Vue front-end (no changes yet).

---

### Task 1: Module Package Restructure

**Files:**
- Move existing files into `com/jincai/crm/<module>/(controller|service|repository|entity|dto)` per module.
- Example: `backend/src/main/java/com/jincai/crm/org/DepartmentController.java` → `backend/src/main/java/com/jincai/crm/org/controller/DepartmentController.java`.
- Update package declarations and imports in every moved file.

- [ ] **Step 1: Outline new directories**  
  - Create new directories under each module (`org`, `customer`, `product`, `order`, `workflow`, `finance`, `rbac`, `audit`, `notification`, `file`).  
  - Place controllers into `controller`, services into `service`, repositories into `repository`, entities into `entity`, DTOs/requests into `dto`.
  
- [ ] **Step 2: Move files**  
  - Use `mv` or editor to relocate controllers (e.g., department/customer/order controllers) into appropriate controller packages.  
  - Move `Entity` classes (AppUser, Customer, RouteProduct, etc.) into `entity` packages.  
  - Keep DTO/request classes near controllers/services (e.g., `OrderRequest` into `dto` or `request`).  
  - Adjust package statements and imports as part of move.

- [ ] **Step 3: Update `@ComponentScan`/Spring Boot base package references (if needed)**  
  - Verify `TravelCrmApplication.java` still scanning `com.jincai.crm` (no change) but ensure new package structure compile-time.  
  - Run `mvn -q -DskipTests compile` to ensure no package errors.

### Task 2: Create Service Layer per Module

**Files:**
- New service interfaces/impls for each module currently holding logic in controllers: `org`, `customer`, `product`, `order`, `workflow`, `finance`, `rbac`.
- Controllers now simply `@Autowired` service and delegate.

- [ ] **Step 1: Identify business logic in controllers**  
  - For `OrgView` controllers: `DepartmentController`, `UserController`, `RoleController` (and `PermissionController` if needed) contain validations/business rules – capture logic in new services (e.g., `DepartmentService`, `UserService`, `RoleService`).
  - Same for `CustomerController`, `ProductController`, `OrderController`, `WorkflowTemplateController`, `FinanceController`, etc.

- [ ] **Step 2: Implement service classes**  
  - Extract methods from each controller into service class (`DepartmentService` etc.).  
  - Services remain `@Service`, use repositories, handle transactions, validations, transformations.  
  - Keep controllers responsible for HTTP layer only (request binding, response wrapping).

- [ ] **Step 3: Update controllers**  
  - Inject services via constructor/field; call service methods; return `ApiResponse`.  
  - Remove inline business code (validation, mapping, repository calls).  
  - Keep helper methods (e.g., tree builders) in services or shared utils.

- [ ] **Step 4: Add service tests (where missing)**  
  - For extracted logic introduce unit tests in `backend/src/test/java/com/jincai/crm/<module>/` focusing on service behavior (e.g., `DepartmentServiceTest`).  
  - Steps: write failing test, run, implement service method, rerun.

### Task 3: Update Wiring and DTO Imports

**Files:**
- Controllers now reference DTOs from new package paths; services use new repository packages.

- [ ] **Step 1: Update imports**  
  - In controllers/services, ensure imported `Repository` and `Entity` classes reflect new packages.  
  - Adjust `@Autowired` or constructor injection to new service class names/locations.

- [ ] **Step 2: Adjust Spring Security references**  
  - If `PermissionController` or `RoleController` moved, ensure security beans reference correct packages (e.g., `@PreAuthorize` expressions remain unchanged).

- [ ] **Step 3: Ensure DTOs still serializable**  
  - For view/request records moved to `dto`, verify Jackson still sees them (records unaffected).

### Task 4: Verify & Document

**Files:**
- `backend/src/main/java/com/jincai/crm/TravelCrmApplication.java` (if necessary to mention reorganized packages).  
- `docs/superpowers/plans/2026-03-21-package-reorg.md` (this plan).  

- [ ] **Step 1: Compile Backend**  
  - Run `mvn -q -DskipTests compile` (expect success).

- [ ] **Step 2: Run targeted tests**  
  - Execute `mvn -q -Dtest=*ServiceTest test` (or relevant service tests) to ensure extracted logic works.

- [ ] **Step 3: Build Frontend**  
  - Run `npm run build` to confirm no regressions.

- [ ] **Step 4: Document package decisions**  
  - Briefly note new package layout in README or internal doc if helpful (optional but ensures future devs know structure).
