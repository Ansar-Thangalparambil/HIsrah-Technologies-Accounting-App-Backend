# Task 1 Architecture Guide (Interview Friendly)

This guide explains how Task 1 is designed and how to present it clearly.

## 1) Layered Design

### Controller Layer

File: `src/main/java/com/hisrah/accounting/controller/AccountController.java`

- Receives HTTP requests.
- Validates request body format (`@Valid`).
- Calls service methods.
- Returns standard response envelope (`ApiResponse`).

### Service Layer

File: `src/main/java/com/hisrah/accounting/service/AccountService.java`

- Contains business rules.
- Handles validation logic that requires DB checks.
- Converts entities to response DTOs.
- Manages hierarchy logic (tree, depth, parent checks).

### Repository Layer

Files:

- `src/main/java/com/hisrah/accounting/repository/AccountRepository.java`
- `src/main/java/com/hisrah/accounting/repository/JournalEntryLineRepository.java`

- Uses Spring Data JPA for database access.
- No SQL writing needed for common queries.

### Entity Layer

Files:

- `src/main/java/com/hisrah/accounting/entity/Account.java`
- `src/main/java/com/hisrah/accounting/entity/JournalEntryLine.java`
- `src/main/java/com/hisrah/accounting/entity/AccountType.java`

- Models database tables and relationships.
- `Account` has parent-child self reference for hierarchy.

### DTO Layer

Files:

- `dto/request/AccountUpsertRequest.java`
- `dto/response/AccountResponse.java`
- `dto/response/AccountTreeResponse.java`
- `dto/response/ApiResponse.java`

- Request DTO: input contract for create/update.
- Response DTO: output contract for API consumers.
- Prevents exposing entity internals directly.

### Exception Layer

Files:

- `exception/GlobalExceptionHandler.java`
- `exception/BadRequestException.java`
- `exception/ConflictException.java`
- `exception/NotFoundException.java`

- Converts business errors into clean HTTP responses.
- Keeps controller and service code cleaner.

## 2) Request Flow

1. Client calls endpoint (example: `POST /api/accounts`).
2. Controller receives JSON and validates basic format.
3. Service applies business rules (uniqueness, parent validity, type matching, depth, range).
4. Repository saves/reads from DB.
5. Service maps entity to response DTO.
6. Controller returns `ApiResponse`.
7. If any rule fails, exception handler returns structured error JSON.

## 3) Business Rules Coverage

- Unique account code.
- Numeric account code (4-7 digits).
- Valid account type.
- Parent account must exist and be active.
- Parent and child type must match.
- Hierarchy depth maximum 4.
- Deactivation blocked if posted journal lines exist.
- Code prefix must match account type group.

## 4) Why this architecture is good

- Separation of concerns.
- Easier testing (service can be unit tested).
- Easier maintenance (rules centralized in service).
- Consistent API output (single response envelope).
- Scalable for Task 2 and Task 3.

## 5) Interview Script (short)

"I implemented Task 1 using layered architecture: controller, service, repository, entity, DTO, and exception handler.  
Controller only handles HTTP concerns, while service enforces accounting business rules like code uniqueness, hierarchy constraints, type consistency, and safe deactivation checks.  
Repository uses Spring Data JPA, and all endpoints return a consistent JSON envelope.  
I also added unit tests for service logic, which is the core of the module."
