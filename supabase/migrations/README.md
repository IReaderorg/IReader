# Supabase Migrations

Additive migrations layered on top of `../schema.sql`. Run **in numeric order** in the Supabase
SQL Editor (or via `supabase db push`). Each is idempotent where possible.

| # | File | What it does |
|---|------|--------------|
| base | `../schema.sql` | Core schema (users, leaderboard, reviews, badges, …) — run first if fresh |
| 001 | `001_profile_gamification.sql` | Profile/economy columns, gamification tables, RLS, reward-engine functions (`sync_reading_stats`, `evaluate_achievements`, `checkin_daily`, `vote_book`, `spend_stones`), triggers, snapshot cron |
| 002 | `002_achievement_seed.sql` | Seeds `achievement_definitions` (the achievement catalog) + the `verified_reader` badge |

## Design notes

- **No money / no NFT** in any new flow. Spirit Stones are earned-only; cosmetics cost stones, never cash.
- **Rewards are unforgeable.** `xp`, `spirit_stones`, `user_achievements` and the stone ledger are
  written **only** by `SECURITY DEFINER` functions (which run as the table owner and bypass RLS).
  Direct client UPDATE on those `public.users` columns is `REVOKE`d; those tables have **no client
  INSERT/UPDATE policies**.
- **Reading totals stay in `public.leaderboard`** (canonical, still client-trusted as today). The
  economy (xp/stones/level/check-in) lives on `public.users`.
- **Achievement catalog lives in the DB** (`achievement_definitions`) — add/tune rows without an
  app release. Artwork: see [`../../plans/image-prompts.md`](../../plans/image-prompts.md); leave
  `icon` emoji + `image_url` NULL until uploaded so the app never shows a broken image.

## Requirements / caveats

- Assumes the Supabase `auth` schema (`auth.uid()`, `auth.users`) — present on any Supabase project.
- `pg_cron` is optional: migration 001 schedules `rch_daily` (reader-count snapshot) and `lb_weekly`
  (rank snapshot) **only if** the extension is available; otherwise it logs a NOTICE. Enable pg_cron
  in the Supabase Dashboard (Database → Extensions) to get rank-movement arrows + the Rising rail,
  or run those two INSERTs on a schedule another way.
- Client RPC entry points (call via PostgREST): `sync_reading_stats`, `checkin_daily`, `vote_book`,
  `spend_stones`. `sync_reading_stats` and `checkin_daily` return the newly-unlocked achievements /
  reward for the celebration UI.

## Local validation

```bash
# spin up a throwaway Postgres, stub the Supabase auth schema, apply everything
docker run -d --name ir_pg -e POSTGRES_PASSWORD=pw -v "$PWD/..":/sql postgres:16
docker exec -i ir_pg psql -U postgres -c \
  "CREATE SCHEMA auth; CREATE TABLE auth.users(id uuid primary key);
   CREATE FUNCTION auth.uid() RETURNS uuid LANGUAGE sql AS 'SELECT gen_random_uuid()';"
docker exec -i ir_pg psql -U postgres -v ON_ERROR_STOP=1 -f /sql/schema.sql
docker exec -i ir_pg psql -U postgres -v ON_ERROR_STOP=1 -f /sql/migrations/001_profile_gamification.sql
docker exec -i ir_pg psql -U postgres -v ON_ERROR_STOP=1 -f /sql/migrations/002_achievement_seed.sql
docker rm -f ir_pg
```
