# ADR-0002: Ledger-Based Stock Tracking

## Status
Accepted

## Context
The system needs to track stock movements and prevent negative stock. We need an immutable record of all transactions.

## Decision
We will use a ledger-based approach:
- StockLedger table records every movement
- Running balance is calculated from ledger
- No direct updates to stock quantities
- EmptyLedger for empty cylinder tracking

Benefits:
- Complete audit trail
- Can reconstruct history at any point
- Prevents data loss
- Easy reconciliation

## Consequences
- More storage required
- Queries need to sum ledger entries
- Better data integrity
