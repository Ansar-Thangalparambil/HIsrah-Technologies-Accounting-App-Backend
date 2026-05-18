# HISRAH Accounting Module Assessment

Spring Boot REST API implementation for HISRAH Accounting assessment.

## 1) Project Objective

This repository implements:

- Task 1: Chart of Accounts Management API (completed)
- Task 2: Manual Journal Entry (planned)
- Task 3: Financial Reporting (planned)

Current status: Task 1 is implemented with validation, hierarchy support, soft deactivation logic, tests, and Swagger UI.

## 2) Technology Stack

- Java 21
- Spring Boot 4.x
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- H2 in-memory database
- Springdoc OpenAPI (Swagger UI)
- Maven
- JUnit 5 + Mockito

## 3) Step-by-Step Installation and Setup

This follows the same practical sequence used in the installation guide and project execution flow.

### 3.1 Install Required Software

Install these tools first:

1. Java JDK 21 or higher
2. Git
3. Maven (optional, wrapper is included)
4. IDE (Cursor / VS Code / IntelliJ)
5. Postman (optional, for API testing)

### 3.2 Verify Installations

Run:

```powershell
java -version
git --version
mvn -version
```

If `mvn` is not installed globally, use the included Maven wrapper commands from this repo.

### 3.3 Clone Repository

```powershell
git clone <your-github-repo-url>
cd accounting
```

### 3.4 Build and Run

Windows:

```powershell
.\mvnw.cmd clean spring-boot:run
```

Linux/macOS:

```bash
./mvnw clean spring-boot:run
```

When startup succeeds, you will see: `Started AccountingApplication`.

## 4) Local Access Links

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:accountingdb`
  - Username: `sa`
  - Password: (empty)

## 5) Task 1: Chart of Accounts API

### 5.1 Implemented Endpoints (7/7)

- `POST /api/accounts` - Create account
- `GET /api/accounts` - List accounts
- `GET /api/accounts/{id}` - Get account by id
- `PUT /api/accounts/{id}` - Update account
- `PATCH /api/accounts/{id}/deactivate` - Soft deactivate account
- `PATCH /api/accounts/{id}/activate` - Reactivate account
- `GET /api/accounts/tree` - Get nested hierarchy

### 5.2 Validation Rules Enforced

- Account code must be unique
- Account code must be numeric and 4-7 digits
- Name is required and max 100 characters
- Type is required enum
- `parentId` must reference an existing active account
- Parent and child must share same account type
- Hierarchy depth cannot exceed 4
- Deactivation blocked for accounts with posted journal lines (`409 Conflict`)
- Type/code mapping:
  - ASSET -> starts with `1`
  - LIABILITY -> starts with `2`
  - EQUITY -> starts with `3`
  - REVENUE -> starts with `4`
  - EXPENSE -> starts with `5` or `6`

### 5.3 Request Example (Create/Update)

```json
{
  "code": "1110",
  "name": "Bank Account",
  "type": "ASSET",
  "parentId": null
}
```

### 5.4 Standard Response Format

```json
{
  "success": true,
  "message": "Account created",
  "data": {
    "id": 1,
    "code": "1110",
    "name": "Bank Account",
    "type": "ASSET",
    "parentId": null,
    "active": true
  },
  "timestamp": "2026-05-18T10:15:30.000Z"
}
```

## 6) API Testing Guide (Swagger + Postman)

### 6.1 Swagger (Beginner Friendly)

1. Open `http://localhost:8080/swagger-ui/index.html`
2. Expand `Account Controller`
3. Click `Try it out`
4. Enter payload
5. Click `Execute`
6. Verify response code and response JSON

### 6.2 Postman Quick Test Order

1. `POST /api/accounts` (create root)
2. `POST /api/accounts` (create child)
3. `GET /api/accounts`
4. `GET /api/accounts/{id}`
5. `PUT /api/accounts/{id}`
6. `GET /api/accounts/tree`
7. `PATCH /api/accounts/{id}/deactivate`
8. `PATCH /api/accounts/{id}/activate`

## 7) Sample cURL Commands

Create root:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"code":"1000","name":"Assets","type":"ASSET","parentId":null}'
```

Create child:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"code":"1110","name":"Bank Account","type":"ASSET","parentId":1}'
```

Get tree:

```bash
curl http://localhost:8080/api/accounts/tree
```

Deactivate:

```bash
curl -X PATCH http://localhost:8080/api/accounts/1/deactivate
```

## 8) Testing

Run tests:

```powershell
.\mvnw.cmd test
```

Current unit tests:

- `AccountServiceTest` (6 test methods)

## 9) Git Workflow (Systematic, From Local to GitHub)

### 9.1 First-Time Repository Setup

```powershell
git init
git branch -M main
git remote add origin <your-github-repo-url>
```

### 9.2 Daily Commit Flow

```powershell
git status
git add .
git commit -m "Implement Task 1 Chart of Accounts API with validation and tests"
git push -u origin main
```

### 9.3 Suggested Commit Message Pattern

- `feat(task1): add account CRUD and hierarchy endpoints`
- `fix(task1): enforce depth validation for subtree re-parenting`
- `docs(readme): add setup and API testing instructions`
- `test(task1): add service unit tests for rules`

## 10) Submission Checklist

- [x] Application runs with one command
- [x] Task 1 endpoints implemented
- [x] Consistent JSON response format
- [x] Validation rules implemented
- [x] Unit tests added
- [x] Swagger UI added
- [x] README with setup and API examples
- [ ] Task 2 implementation
- [ ] Task 3 implementation

## 11) Project Structure

```text
src/main/java/com/hisrah/accounting
  ├─ controller
  ├─ service
  ├─ repository
  ├─ entity
  ├─ dto/request
  ├─ dto/response
  ├─ exception
  └─ config
```

## 12) Notes

- If API returns `404`, stop existing process and run:
  - `.\mvnw.cmd clean spring-boot:run`
- Always verify using Swagger first, then Postman.
