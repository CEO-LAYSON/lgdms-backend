# ADR-0003: Role-Based Access Control

## Status
Accepted

## Context
The SRS specifies 6 user roles with different permissions. We need a flexible RBAC system.

## Decision
We implement RBAC with:
- Users
- Roles (6 predefined from SRS)
- Permissions (fine-grained access)
- Many-to-many relationships

Permissions follow pattern: `resource:action` (e.g., `user:create`)

Method-level security with `@PreAuthorize` annotations.

## Consequences
- Highly flexible permission system
- Can add new roles without code changes
- Security at service/controller level
