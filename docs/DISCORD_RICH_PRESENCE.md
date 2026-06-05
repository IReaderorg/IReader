# Discord Rich Presence (desktop)

IReader can publish your current activity — which tab you're browsing, the book
you're reading, or an active TTS session — to Discord as Rich Presence.

## Design: file-IPC, no SDK in the app

IReader itself has **no Discord dependency**. It only writes a small JSON state file;
a separate, standalone bridge process does the actual Discord RPC. This keeps the app
free of any Discord SDK and lets the presence layer be swapped or disabled without
touching the app.

```
 IReader (desktop)                     ireader-discord bridge (separate process)
 ┌────────────────────┐   writes       ┌─────────────────────────────────────┐
 │ ActivityStateHolder │ ────────────▶ │ polls ~/.cache/IReader/              │
 │ DiscordStatePublisher│  JSON file    │ discord_state.json → Discord IPC RPC │
 │ TTSActivityBridge   │               │ (holds the Discord application ID)   │
 └────────────────────┘               └─────────────────────────────────────┘
```

The **Discord application / client ID and all RPC logic live in the bridge**, not in
IReader. Without the bridge running, IReader simply writes a file nobody reads.

## State file

- Path: `~/.cache/IReader/discord_state.json`
- Written atomically (`*.tmp` + rename) so the poller never reads a partial file.
- Writes are debounced ~300 ms; a 20 s heartbeat re-writes the current activity so a
  motionless reading session doesn't look "stale" to the bridge.
- On app shutdown / idle the file is **deleted** — the bridge treats a missing file as
  "clear presence" (`rpc.clear()`). The bridge should also treat a file older than
  ~30 s as "IReader closed".

### JSON schema

```jsonc
{
  "mode":         "browsing" | "perusing" | "reading" | "tts" | "idle",
  "tab":          "Library",        // browsing only
  "subTab":       null,             // browsing, optional
  "book":         "Book Title",     // perusing / reading / tts
  "author":       "Author",
  "cover":        "https://…",      // raw cover URL (bridge may re-host)
  "bookUrl":      "https://…",      // source URL (book.key) for a Discord button
  "chapter":      "Chapter 12",     // reading / tts
  "chapterIndex": 12,               // 1-based
  "chapterCount": 40,
  "startedAt":    1717459200000     // epoch millis; presence "elapsed" start
}
```

`idle` is never published as presence — it's the signal to clear. State precedence is
**TTS > Reading**; Browsing/Perusing are last-writer-wins (navigation is sequential).

## Enabling / disabling

Controlled by `AppPreferences.discordRichPresenceEnabled()` (default **on**). The
publisher + TTS bridge are eager-initialised at desktop startup only when this is set;
toggling it takes effect on the next app start. (Settings UI toggle: TODO — the pref
and the startup gate exist; the screen control is a small follow-on.)

## The companion bridge

The RPC bridge is a separate project (not in this repository). It is the component that
owns the Discord application ID, opens the Discord IPC socket, maps the JSON `mode` to
presence assets/buttons, and calls `rpc.clear()` when the file disappears. Ship/install
it separately, or replace the file consumer with any process that understands the
schema above.

> Maintainer note: because the app side is only a state-file writer, this feature does
> nothing visible on its own. If a self-contained experience is preferred, the RPC
> could instead be implemented in-app (Kotlin Discord IPC over the local socket); the
> file-IPC approach was chosen to keep IReader SDK-free.
