# 0001 — MVVM + Unidirectional Data Flow

**Status:** Accepted

## Decision
Use MVVM with strict Unidirectional Data Flow. State flows down via `StateFlow`, events flow up via sealed interfaces.

## Context
The challenge requires MVVM. We needed a clear, testable pattern for state management across 4 screens with shared playback state.

## Alternatives
- **MVI**: Structured reducers/side effects, but adds framework complexity for same benefit.
- **MVP**: Not Compose-idiomatic, interface-heavy.
- **Molecule (headless Compose)**: Elegant but unusual in Android ecosystem, harder to explain.

## Consequences
- Every screen follows the same pattern: `ViewModel` exposes `StateFlow<State>`, screen calls `onEvent()`.
- Navigation events handled via composable callbacks, not ViewModel-emitted one-shot events (per Google guidelines).
- ViewModels are testable with `StandardTestDispatcher` + Turbine.
