# 0008 — Localization from Day One

**Status:** Accepted

## Decision
All user-facing strings in `strings.xml`. Portuguese translation in `values-pt/`. No hardcoded strings in composables.

## Context
Internationalization is easy to add at the start, painful to retrofit. Demonstrates proper Android resource handling.

## Alternatives
- **Hardcoded strings**: Faster development, but no i18n path, harder to maintain consistency.
- **Third-party i18n library**: Unnecessary — Android's resource system handles locale resolution natively.

## Consequences
- Error messages, labels, accessibility descriptions all go through `stringResource()`.
- Adding a new language = one `values-XX/strings.xml` file, zero code changes.
- `UiError` enum mapped to `@StringRes` in the UI layer — ViewModel stays Android-free.
