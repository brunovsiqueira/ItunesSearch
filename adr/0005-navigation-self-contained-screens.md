# 0005 — Compose Navigation with Self-Contained Screens

**Status:** Accepted

## Decision
Type-safe Compose Navigation routes. Each screen creates its own ViewModel via `LocalAppContainer`. NavHost stays thin — only maps routes to screens and passes navigation callbacks.

## Context
Initial approach had ViewModel creation inside NavHost lambdas, which would grow linearly with every screen. Needed a pattern that scales.

## Alternatives
- **ViewModel wiring in NavHost**: Explicit, all in one place. But NavHost becomes a god-file.
- **NavGraphBuilder extensions**: Modular, but still needs callback wiring at extension level.

## Consequences
- Each screen file has two composables: public entry point (creates VM) + private stateless content (testable, previewable).
- Navigation events via callbacks (`onNavigateToPlayer: (Long) -> Unit`), not ViewModel.
- Adding a new screen = one `composable<Route>` line in NavHost + a self-contained screen file.
- `NavDestination.hasRoute<T>()` for type-safe route checks (e.g., hiding mini player on PlayerScreen).
