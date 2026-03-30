# 0002 — Manual Dependency Injection

**Status:** Accepted

## Decision
Use manual constructor injection via `AppContainer` class. Dependencies provided to Compose via `CompositionLocal`.

## Context
4 screens, ~10 objects to wire (1 API, 1 DB, 2 DAOs, 2 repos, 1 audio player, 1 connectivity observer). Need testability without framework overhead.

## Alternatives
- **Hilt**: Compile-time safe, Google-endorsed. But annotation processing slows builds, Dagger learning curve, overkill for this scope.
- **Koin**: Simple DSL, no code gen. But runtime resolution (crashes if misconfigured), reflection-based.

## Consequences
- Every dependency visible in one file (`AppContainer.kt`).
- Compile-time safety — missing dependency = compilation error.
- Screens access container via `LocalAppContainer.current` (Compose-idiomatic).
- Tests pass fakes directly via constructors — no test rules or framework setup.
- Does not scale to 50+ screens. For a large production app, Hilt would be the right choice.
