# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build (produces fat jar with dependencies)
mvn clean package

# Run locally
java -cp target/classes:target/dependency/* org.bodzan.Main

# Heroku worker process (defined in pom.xml heroku plugin)
java $JAVA_OPTS -cp target/classes:target/dependency/* org/bodzan/Main
```

There are no automated tests in this project.

## Architecture

This is a Discord music bot built with **Discord4J 3.2.5** (reactive, Project Reactor-based) and **LavaPlayer 1.3.77** for audio.

### Reactive programming model

All Discord event handling uses Project Reactor (`Mono`/`Flux`). Slash command interactions are handled via `ApplicationCommandInteractionEvent`. Commands are registered globally (and to specific guild IDs) at startup inside `Main.main()`.

### Audio pipeline

```
YouTube API search (HTTP + Jackson)
  → LavaPlayer AudioPlayerManager.loadItem()
    → AudioTrackScheduler (queue management)
      → LavaPlayerAudioProvider (Opus frame bridge)
        → Discord4J voice connection
```

- `GuildAudioManager` — per-guild singleton, keyed by Snowflake in a `ConcurrentHashMap`. Creates the `AudioPlayer`, `AudioTrackScheduler`, and `LavaPlayerAudioProvider` for each guild.
- `AudioTrackScheduler` — wraps a `synchronized` list as the play queue; implements `AudioLoadResultHandler` (LavaPlayer callback) and `AudioEventAdapter` (track-end auto-advance).
- `LavaPlayerAudioProvider` — implements Discord4J's `AudioProvider`; polls LavaPlayer's `MusicManager` for Opus frames each tick.

### Slash commands (all in `Main.java`)

| Command | Behaviour |
|---------|-----------|
| `/join` | Bot joins the invoking user's voice channel |
| `/play <keyword>` | Searches YouTube via REST API, loads first result with LavaPlayer, queues it |
| `/skip` | Advances `AudioTrackScheduler` to next track |
| `/leave` | Bot disconnects from voice channel |

### JSON deserialization

The `org.bodzan.json` package contains POJOs for the YouTube Data API v3 search response. `ObjectMapper` (Jackson, pulled in transitively) is used directly in `Main.java` to deserialize search results.

### Configuration

Bot token, YouTube API key, and target guild IDs are currently hardcoded in `Main.java` (lines ~55, ~213–217). Heroku deployment reads `$JAVA_OPTS` from the environment but the application secrets are not yet externalised.
