# 0006 — Shared Singleton AudioPlayer

**Status:** Accepted

## Decision
Single `AudioPlayer` instance in `AppContainer`, shared across all screens. Wraps Media3 ExoPlayer behind an interface.

## Context
Each navigation to PlayerScreen created a new ExoPlayer instance. Playing multiple songs caused audio overlap. Also needed foundation for mini player.

## Alternatives
- **ViewModel-scoped player**: Each PlayerVM owns its own ExoPlayer. Simple but causes overlap on back-stack navigation.
- **MediaSessionService**: Full background playback support with notification controls. Correct for a production music app, overkill for 30-second previews.

## Consequences
- `play(newUrl)` on same ExoPlayer stops previous playback — impossible to overlap.
- `AudioPlayer` interface enables `FakeAudioPlayer` in tests.
- `NowPlayingState` (separate singleton) holds current `Song` metadata + playlist context for mini player.
- When navigating to a song that's already playing, playback continues seamlessly (no restart).
- Disk caching via `SimpleCache` (50MB LRU) — played songs replay offline.
