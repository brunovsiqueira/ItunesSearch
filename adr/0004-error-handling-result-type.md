# 0004 — Error Handling via Result<T> Sealed Interface

**Status:** Accepted

## Decision
Custom `Result<T>` sealed interface. No exception ever reaches the UI. Three-layer error mapping: Domain → ViewModel → UI.

## Context
Need robust error handling where the UI shows localized messages, ViewModels stay Android-free, and repositories log full details.

## Alternatives
- **kotlin.Result**: Built-in, but only Success/Failure with Throwable — no semantic error categories.
- **Arrow Either**: Powerful functional type, but heavy dependency for simple error handling.
- **Exception propagation**: Implicit, easy to miss, crashes if uncaught.

## Consequences
- `safeApiCall()` catches IOException → `Failure.Network`, HttpException → `Failure.Api`, rethrows `CancellationException`.
- `Result.Failure` (domain) → `UiError` enum (ViewModel) → `@StringRes` (UI composable). Each layer knows only what it needs.
- `AppLogger` logs all failures with tags for debugging. Swappable for Timber/Crashlytics.
