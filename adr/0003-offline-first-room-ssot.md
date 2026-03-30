# 0003 — Offline-First with Room as Single Source of Truth

**Status:** Accepted

## Decision
Room database is the single source of truth. UI reads from Room via `Flow`, API writes to Room. UI never reads from API directly.

## Context
Challenge requires "offline first UX" and "display recently played songs." Need cached search results that survive network failures and app restarts.

## Alternatives
- **API-first with local cache fallback**: Simpler, but UI flickers between empty → loading → data on every navigation.
- **In-memory cache only**: Fast, but lost on process death. No persistence.
- **DataStore**: Good for key-value, not for relational queries (search by query, filter by album).

## Consequences
- Search: `fetchSearchResults()` writes to Room → `getSearchResultsStream()` emits from Room.
- Network fails → Room still has cached data → UI shows stale results + offline banner.
- Album tracks use `OnConflictStrategy.IGNORE` to avoid overwriting search metadata.
- Audio previews cached via Media3 `SimpleCache` (50MB LRU) for offline playback.
