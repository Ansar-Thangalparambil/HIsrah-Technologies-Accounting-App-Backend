# Task Navigation Guide

Use this guide to quickly identify Task 1 vs Task 2 in code and Swagger.

## Swagger Grouping

- Task 1 group: `Task 1 - Chart of Accounts`
- Task 2 group: `Task 2 - Manual Journal Entry`

## URL Prefixes

- Task 1 endpoints start with: `/api/accounts`
- Task 2 endpoints start with: `/api/journal-entries`

## Code Files By Task

### Task 1 (Chart of Accounts)

- `controller/AccountController.java`
- `service/AccountService.java`
- `entity/Account.java`
- `entity/AccountType.java`
- `dto/request/AccountUpsertRequest.java`
- `dto/response/AccountResponse.java`
- `dto/response/AccountTreeResponse.java`

### Task 2 (Manual Journal Entry)

- `controller/JournalEntryController.java`
- `service/JournalEntryService.java`
- `entity/JournalEntry.java`
- `entity/JournalEntryLine.java`
- `entity/JournalEntryStatus.java`
- `dto/request/JournalEntryUpsertRequest.java`
- `dto/request/JournalEntryLineRequest.java`
- `dto/request/JournalEntryActionRequest.java`
- `dto/response/JournalEntryResponse.java`
- `dto/response/JournalEntryLineResponse.java`

## Test Files By Task

- Task 1: `src/test/java/com/hisrah/accounting/service/AccountServiceTest.java`
- Task 2: `src/test/java/com/hisrah/accounting/service/JournalEntryServiceTest.java`
