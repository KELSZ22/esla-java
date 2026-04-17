---
description: Database Schema - Complete database table and field definitions for ESLA application
auto_execution_mode: 2
---

# Database Schema

This document defines the complete database structure for the ESLA (Electronic Savings & Loan Application) system.

## Core Laravel Tables

### users

| Field                     | Type      | Constraints |
| ------------------------- | --------- | ----------- |
| id                        | bigint    | primary key |
| name                      | string    | -           |
| email                     | string    | unique      |
| email_verified_at         | timestamp | nullable    |
| password                  | string    | -           |
| two_factor_secret         | text      | nullable    |
| two_factor_recovery_codes | text      | nullable    |
| two_factor_confirmed_at   | timestamp | nullable    |
| remember_token            | string    | nullable    |
| created_at                | timestamp | -           |
| updated_at                | timestamp | -           |

### password_reset_tokens

| Field      | Type      | Constraints |
| ---------- | --------- | ----------- |
| email      | string    | primary key |
| token      | string    | -           |
| created_at | timestamp | nullable    |

### sessions

| Field         | Type     | Constraints        |
| ------------- | -------- | ------------------ |
| id            | string   | primary key        |
| user_id       | bigint   | nullable, indexed  |
| ip_address    | string   | nullable, 45 chars |
| user_agent    | text     | nullable           |
| payload       | longText | -                  |
| last_activity | integer  | indexed            |

### cache

| Field      | Type       | Constraints |
| ---------- | ---------- | ----------- |
| key        | string     | primary key |
| value      | mediumText | -           |
| expiration | integer    | indexed     |

### cache_locks

| Field      | Type    | Constraints |
| ---------- | ------- | ----------- |
| key        | string  | primary key |
| owner      | string  | -           |
| expiration | integer | indexed     |

### jobs

| Field        | Type                | Constraints |
| ------------ | ------------------- | ----------- |
| id           | bigint              | primary key |
| queue        | string              | indexed     |
| payload      | longText            | -           |
| attempts     | unsignedTinyInteger | -           |
| reserved_at  | unsignedInteger     | nullable    |
| available_at | unsignedInteger     | -           |
| created_at   | unsignedInteger     | -           |

### job_batches

| Field          | Type       | Constraints |
| -------------- | ---------- | ----------- |
| id             | string     | primary key |
| name           | string     | -           |
| total_jobs     | integer    | -           |
| pending_jobs   | integer    | -           |
| failed_jobs    | integer    | -           |
| failed_job_ids | longText   | -           |
| options        | mediumText | nullable    |
| cancelled_at   | integer    | nullable    |
| created_at     | integer    | -           |
| finished_at    | integer    | nullable    |

### failed_jobs

| Field      | Type      | Constraints |
| ---------- | --------- | ----------- |
| id         | bigint    | primary key |
| uuid       | string    | unique      |
| connection | text      | -           |
| queue      | text      | -           |
| payload    | longText  | -           |
| exception  | longText  | -           |
| failed_at  | timestamp | -           |

## Application Tables

### ledgers

| Field       | Type      | Constraints                                               |
| ----------- | --------- | --------------------------------------------------------- |
| id          | bigint    | primary key                                               |
| description | string    | -                                                         |
| type        | enum      | 'channel 3', 'resort', 'executive', 'consultant', 'other' |
| date        | date      | -                                                         |
| deleted_at  | timestamp | nullable                                                  |
| created_at  | timestamp | -                                                         |
| updated_at  | timestamp | -                                                         |

**Purpose**: Stores ledger information for different member types.

### members

| Field        | Type            | Constraints                                               |
| ------------ | --------------- | --------------------------------------------------------- |
| id           | bigint          | primary key                                               |
| name         | string          | -                                                         |
| email        | string          | unique                                                    |
| phone        | string          | -                                                         |
| address      | string          | -                                                         |
| member_type  | enum            | 'channel 3', 'resort', 'executive', 'consultant', 'other' |
| member_since | date            | default: now()                                            |
| premium      | unsignedInteger | default: 0                                                |
| deleted_at   | timestamp       | nullable                                                  |
| created_at   | timestamp       | -                                                         |
| updated_at   | timestamp       | -                                                         |

**Purpose**: Stores member information with different membership types.

### form_data

| Field             | Type         | Constraints                                           |
| ----------------- | ------------ | ----------------------------------------------------- |
| id                | bigint       | primary key                                           |
| ledger_id         | bigint       | foreign key → ledgers.id, nullable, on delete cascade |
| member_id         | bigint       | foreign key → members.id, nullable, on delete cascade |
| form_number       | integer      | nullable                                              |
| is_loan           | boolean      | default: false                                        |
| date              | date         | nullable                                              |
| should_be_paid    | decimal 10,2 | nullable                                              |
| actual_payment    | decimal 10,2 | nullable                                              |
| balance           | decimal 10,2 | nullable                                              |
| under_paid        | decimal 10,2 | nullable                                              |
| scheduled_payment | decimal 10,2 | nullable                                              |
| premium_total     | decimal 10,2 | nullable                                              |
| premium           | decimal 10,2 | nullable                                              |
| actual_payroll    | decimal 10,2 | nullable                                              |
| remarks           | text         | nullable                                              |
| deleted_at        | timestamp    | nullable                                              |
| created_at        | timestamp    | -                                                     |
| updated_at        | timestamp    | -                                                     |

**Purpose**: Stores form data linking ledgers and members with payment information.

### member_service_charge_refunds

| Field       | Type      | Constraints              |
| ----------- | --------- | ------------------------ |
| id          | bigint    | primary key              |
| member_id   | bigint    | foreign key → members.id |
| description | string    | -                        |
| date_from   | date      | -                        |
| date_to     | date      | -                        |
| deleted_at  | timestamp | nullable                 |
| created_at  | timestamp | -                        |
| updated_at  | timestamp | -                        |

**Purpose**: Stores service charge refund periods for members.

### member_service_charge_refund_forms

| Field              | Type         | Constraints                                                       |
| ------------------ | ------------ | ----------------------------------------------------------------- |
| id                 | bigint       | primary key                                                       |
| mscr_refund_id     | bigint       | foreign key → member_service_charge_refunds.id, on delete cascade |
| form_number        | integer      | nullable                                                          |
| date_loan          | date         | nullable                                                          |
| principal          | decimal 10,2 | nullable                                                          |
| interest           | decimal 10,2 | nullable                                                          |
| service_charge     | decimal 10,2 | nullable                                                          |
| total              | decimal 10,2 | nullable                                                          |
| no_of_months       | decimal 5,2  | nullable                                                          |
| collected_interest | decimal 10,2 | nullable                                                          |
| total_interest     | decimal 10,2 | nullable                                                          |
| refund_60          | decimal 10,2 | nullable                                                          |
| refund_40          | decimal 10,2 | nullable                                                          |
| remarks            | string       | nullable                                                          |
| has_balance        | boolean      | default: false                                                    |
| deleted_at         | timestamp    | nullable                                                          |
| created_at         | timestamp    | -                                                                 |
| updated_at         | timestamp    | -                                                                 |

**Purpose**: Detailed refund form data with 60/40 split calculations.

### loans

| Field                        | Type         | Constraints                                             |
| ---------------------------- | ------------ | ------------------------------------------------------- |
| id                           | bigint       | primary key                                             |
| form_data_id                 | bigint       | foreign key → form_data.id, nullable, on delete cascade |
| ledger_id                    | bigint       | foreign key → ledgers.id, nullable, on delete cascade   |
| member_id                    | bigint       | foreign key → members.id, nullable, on delete cascade   |
| form_number                  | integer      | nullable                                                |
| date                         | date         | nullable                                                |
| start_deduction_on_loan_date | boolean      | default: false                                          |
| start_deduction_date         | date         | nullable                                                |
| principal                    | decimal 10,2 | nullable                                                |
| service_charge               | decimal 10,2 | nullable                                                |
| service_charge_balance       | decimal 10,2 | nullable                                                |
| interest                     | decimal 10,2 | nullable                                                |
| interest_balance             | decimal 10,2 | nullable                                                |
| cutoffs                      | integer      | nullable                                                |
| cutoffs_amount               | decimal 10,2 | nullable                                                |
| total                        | decimal 10,2 | nullable                                                |
| remarks                      | text         | nullable                                                |
| deleted_at                   | timestamp    | nullable                                                |
| created_at                   | timestamp    | -                                                       |
| updated_at                   | timestamp    | -                                                       |

**Purpose**: Stores loan information with deduction schedules and balances.

## Key Relationships

### Member Types

- channel 3
- resort
- executive
- consultant
- other

### Foreign Key Relationships

- `form_data.ledger_id` → `ledgers.id`
- `form_data.member_id` → `members.id`
- `member_service_charge_refunds.member_id` → `members.id`
- `member_service_charge_refund_forms.mscr_refund_id` → `member_service_charge_refunds.id`
- `loans.form_data_id` → `form_data.id`
- `loans.ledger_id` → `ledgers.id`
- `loans.member_id` → `members.id`

### Cascade Deletes

- Deleting a ledger will cascade to related form_data and loans
- Deleting a member will cascade to related form_data and loans
- Deleting a service charge refund will cascade to related refund forms
- Deleting form_data will cascade to related loans

## Business Logic Notes

### Premium System

- Members have a premium field (unsigned integer, default 0)
- Premium calculations stored in form_data (premium_total, premium)

### Loan Calculations

- Loans track principal, service_charge, and interest separately
- Balances maintained for service_charge_balance and interest_balance
- Cutoff system for payment scheduling (cutoffs, cutoffs_amount)

### Refund Calculations

- 60/40 split for refunds (refund_60, refund_40)
- Interest tracking (collected_interest, total_interest)
- Balance tracking (has_balance flag)

### Payment Tracking

- should_be_paid: expected payment amount
- actual_payment: actual amount paid
- balance: remaining balance
- under_paid: amount underpaid
- scheduled_payment: next scheduled payment
