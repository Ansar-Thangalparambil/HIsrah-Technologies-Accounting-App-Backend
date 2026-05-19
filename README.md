# HISRAH Accounting Module Assessment

Spring Boot REST API implementation for HISRAH Accounting assessment.

## 1) Project Objective

This repository implements:

- Task 1: Chart of Accounts Management API (completed)
- Task 2: Manual Journal Entry API (completed)
- Task 3: Financial Reporting API (completed)

Current status: Task 1, Task 2, and Task 3 are implemented with validations, workflow rules, reporting endpoints, tests, and Swagger UI grouping.

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
2. Expand `Task 1 - Chart of Accounts` for Task 1 APIs
3. Expand `Task 2 - Manual Journal Entry` for Task 2 APIs
4. Expand `Task 3 - Financial Reporting` for Task 3 APIs
5. Click `Try it out`
6. Enter payload
7. Click `Execute`
8. Verify response code and response JSON

### 6.2 How To Identify Task 1 vs Task 2

- Task 1 URLs start with: `/api/accounts`
- Task 2 URLs start with: `/api/journal-entries`
- Task 3 URLs start with: `/api/v1/reports`
- Swagger groups them separately using tags:
  - `Task 1 - Chart of Accounts`
  - `Task 2 - Manual Journal Entry`
  - `Task 3 - Financial Reporting`
- Additional beginner guides: `docs/task-navigation-guide.md`, `docs/task-files-by-task.md` (full file list per task)
- Submission text for PDF placeholders: `docs/submission-sections-filled.md`
- Interview notes: `docs/interview-viva-notes.md`
- Final audit checklist: `docs/final-audit-checklist.md`
- Postman collection: `docs/postman/hisrah-accounting-collection.json`

## 7) Task 2: Manual Journal Entry API

### 7.1 Implemented Endpoints (9/9)

- `POST /api/journal-entries` - Create draft journal entry
- `GET /api/journal-entries` - List journal entries
- `GET /api/journal-entries/{id}` - Get journal entry by id
- `PUT /api/journal-entries/{id}` - Edit draft/rejected entry
- `DELETE /api/journal-entries/{id}` - Delete draft entry
- `PATCH /api/journal-entries/{id}/submit` - DRAFT -> PENDING_APPROVAL
- `PATCH /api/journal-entries/{id}/approve` - PENDING_APPROVAL -> APPROVED
- `PATCH /api/journal-entries/{id}/reject` - PENDING_APPROVAL -> REJECTED
- `PATCH /api/journal-entries/{id}/reverse` - APPROVED -> REVERSED + mirror entry

### 7.2 Business Rules Implemented

- Double-entry balance rule (`422` if debit total != credit total)
- Minimum 2 lines per entry
- Each line has either debit or credit, not both
- Amounts non-negative and max 2 decimal places
- accountId must exist and be active
- entryDate cannot exceed today + 1 day
- entryDate cannot be in simulated locked periods
- Four-eyes rule: creator cannot approve own entry (`403`)
- Only DRAFT entries can be deleted
- Only DRAFT/REJECTED entries can be edited
- Approved entries cannot return to DRAFT/PENDING
- referenceNo auto format `JE-YYYYMMDD-NNNN`
- Reversal creates approved mirror entry linked by `reversalOf`

## 8) Postman Quick Test Order

### Task 1

1. `POST /api/accounts`
2. `GET /api/accounts`
3. `GET /api/accounts/tree`

### Task 2

1. `POST /api/journal-entries`
2. `PUT /api/journal-entries/{id}`
3. `PATCH /api/journal-entries/{id}/submit`
4. `PATCH /api/journal-entries/{id}/approve`
5. `PATCH /api/journal-entries/{id}/reverse`

### Task 3

1. `GET /api/v1/reports/trial-balance?dateFrom=2026-01-01&dateTo=2026-12-31`
2. `GET /api/v1/reports/profit-loss?dateFrom=2026-01-01&dateTo=2026-12-31`
3. `GET /api/v1/reports/general-ledger?accountId=1&dateFrom=2026-01-01&dateTo=2026-12-31`

## 9) Task 3: Financial Reporting API

### 9.1 Implemented Endpoints (3/3)

- `GET /api/v1/reports/trial-balance`
- `GET /api/v1/reports/profit-loss`
- `GET /api/v1/reports/general-ledger`

### 9.2 Implemented Reporting Rules

- Only APPROVED journal entries are included in all reports
- Trial Balance:
  - Supports `dateFrom`, `dateTo`, `includeZeroBalance`
  - Calculates `totalDebits`, `totalCredits`, `netBalance` per account
  - Validates grand total debits equals grand total credits
- Profit & Loss:
  - Revenue = credit - debit for REVENUE accounts
  - Total Expenses = debit - credit for EXPENSE accounts
  - COGS = expense movement for account codes `5000-5999`
  - Gross Profit = Revenue - COGS
  - Net Profit = Revenue - Total Expenses
  - Expense rows grouped under parent account subtotal
- General Ledger:
  - Account-specific approved lines in date range
  - Opening balance before `dateFrom`
  - Movement and running balance per line

## 10) Sample cURL Commands

Task 1 create root:

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"code":"1000","name":"Assets","type":"ASSET","parentId":null}'
```

Task 2 create journal entry:

```bash
curl -X POST http://localhost:8080/api/journal-entries \
  -H "Content-Type: application/json" \
  -d '{
    "entryDate":"2026-05-18",
    "description":"Office expense",
    "createdBy":"ansar",
    "lines":[
      {"accountId":1,"debitAmount":100.00,"creditAmount":0.00},
      {"accountId":2,"debitAmount":0.00,"creditAmount":100.00}
    ]
  }'
```

Task 3 trial balance:

```bash
curl "http://localhost:8080/api/v1/reports/trial-balance?dateFrom=2026-01-01&dateTo=2026-12-31&includeZeroBalance=false"
```

## 11) Testing

Run tests:

```powershell
.\mvnw.cmd test
```

Current unit tests:

- `AccountServiceTest` (6 test methods)
- `JournalEntryServiceTest` (4 test methods)

## 12) Git Workflow (Systematic, From Local to GitHub)

### 12.1 First-Time Repository Setup

```powershell
git init
git branch -M main
git remote add origin <your-github-repo-url>
```

### 12.2 Daily Commit Flow

```powershell
git status
git add .
git commit -m "Implement Task 1 Chart of Accounts API with validation and tests"
git push -u origin main
```

### 12.3 Suggested Commit Message Pattern

- `feat(task1): add account CRUD and hierarchy endpoints`
- `fix(task1): enforce depth validation for subtree re-parenting`
- `docs(readme): add setup and API testing instructions`
- `test(task1): add service unit tests for rules`
- `feat(task2): add manual journal entry workflow and reversal`
- `feat(task3): add trial balance profit-loss and general ledger reports`

## 13) Submission Checklist

- [x] Application runs with one command
- [x] Task 1 endpoints implemented
- [x] Consistent JSON response format
- [x] Validation rules implemented
- [x] Unit tests added
- [x] Swagger UI added
- [x] README with setup and API examples
- [x] Task 2 implementation
- [x] Task 3 implementation

## 14) Project Structure

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

## 15) Notes

- If API returns `404`, stop existing process and run:
  - `.\mvnw.cmd clean spring-boot:run`
- Always verify using Swagger first, then Postman.
