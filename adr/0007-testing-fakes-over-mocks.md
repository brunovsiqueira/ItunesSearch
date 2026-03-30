# 0007 — Testing with Fakes over Mocks

**Status:** Accepted

## Decision
Hand-written fake implementations for all interfaces. No mocking frameworks (MockK, Mockito).

## Context
Need testable ViewModels and repositories. Google's architecture guide strongly recommends fakes over mocks.

## Alternatives
- **MockK**: Quick setup, flexible. But reflection-based, tests couple to implementation details, less readable.
- **Mockito**: Java standard. But Kotlin support is secondary, verbose.

## Consequences
- Fakes: `FakeSongRepository`, `FakeAlbumRepository`, `FakeAudioPlayer`, `FakeConnectivityObserver`, `NoOpLogWriter`.
- `TestData` factory for consistent test fixtures.
- Tests are readable — fakes document expected behavior explicitly.
- 26 unit tests + 1 Maestro E2E flow covering the main user journey.
- Interfaces on all boundaries (`SongRepository`, `AudioPlayer`, `ConnectivityObserver`) — required for faking, also enables swapping implementations.
