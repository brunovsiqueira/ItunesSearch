# 0009 — State Management with StateFlow + Immutable State

**Status:** Accepted

## Decision
Use `MutableStateFlow` in ViewModels, exposed as `StateFlow`. All state classes are immutable `data class` with `@Immutable` annotation. UI events modeled as `sealed interface`.

## Context
Need predictable, testable state management that integrates with Compose's lifecycle-aware collection (`collectAsStateWithLifecycle`).

## Alternatives
- **Compose `mutableStateOf`**: Zero boilerplate, Compose-native. But harder to unit test outside Compose, not lifecycle-aware.
- **`stateIn` with `WhileSubscribed(5000)`**: Google-recommended for stream-derived state. Used where state derives purely from repository Flows; `MutableStateFlow` used where state mixes user input + async data.
- **MVI libraries (Orbit, Redux)**: Structured reducers/side effects. Extra dependency, black box.
- **LiveData**: Lifecycle-aware but legacy, not coroutine-native.

## Consequences
- Immutable state prevents accidental mutation — all updates via `_state.update { it.copy(...) }`.
- Sealed event interfaces give exhaustive `when` — compiler catches missing event handlers.
- `@Immutable` annotation enables Compose compiler skip optimizations.
- Testable with `StandardTestDispatcher` — advance time, assert state values directly.
- Computed properties on state (`displayedSongs`, `hasPrevious`, `showEmptyState`) keep logic out of the UI layer.
