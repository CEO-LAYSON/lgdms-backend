# ADR-0004: Caching Strategy

## Status
Accepted

## Context
Frequently accessed data needs caching for performance. The SRS requires <3 second response time.

## Decision
Use Redis for distributed caching with:
- Cache frequently accessed entities (users, roles, permissions)
- Cache dashboard KPIs (refreshed every hour)
- Cache reference data (cylinder sizes, price categories)
- TTL based on data volatility
- Cache warming jobs

Cache names defined in constants for consistency.

## Consequences
- Improved response times
- Reduced database load
- Need to handle cache invalidation
- Additional infrastructure (Redis)
