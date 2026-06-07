# Webnovel-Inspired Profile & Community Redesign (No-Money Edition)

## Guiding Principles (read first)

1. **No real money, ever.** There are no purchases, no IAP, no donations-for-currency, no
   crypto. Every "currency" in this app is **earned through reading and participation only**.
   Its entire purpose is the *feeling of receiving something* and being recognized — not revenue.
2. **No author features.** We do not host authors. Remove everything author-facing:
   author replies to reviews/comments, "follow author", author notes, **gifts to authors**,
   and "Top Gifter" leaderboards. Author/fan-club interaction lives in **Discord**, not the app.
3. **No in-app book clubs.** Clubs are a Discord responsibility. We don't duplicate them.
   The app's job is to *funnel users into Discord*, where the real community lives.
4. **Discord is the community backbone.** The app is the reading + recognition surface;
   Discord is the conversation surface. Every social action should have a "take it to Discord"
   path. See the dedicated Discord section — this is the most important new direction.
5. **Reuse, don't duplicate.** The Supabase DB already has `users`, `leaderboard`,
   `book_reviews`, `community_quotes`, `badges`/`user_badges`. Extend these. Do **not** create
   a parallel `user_profiles` table that duplicates `leaderboard`.
6. **No server but Supabase.** The only backend is Supabase: Postgres tables, RLS,
   `SECURITY DEFINER` RPC functions, triggers, `pg_cron`, and Supabase Auth. No bot, no relay,
   no edge server of ours. All reward/achievement logic runs as SQL inside Supabase; Discord
   only ever receives *outbound webhooks fired by the client*.
7. **Local-first, account-optional.** Anything derivable from on-device data (reading time,
   chapters, books, streak, genres, level/XP, achievement *progress*) must render **signed-out**.
   Only cloud/social features (leaderboard, followers, comments, cloud stone balance, sharing,
   Discord link) require sign-in, behind a friendly "Join the community" CTA.
8. **Webnovel-style UI throughout.** All redesigned surfaces follow the Webnovel visual language:
   bold covers, podiums, ranked numerals, rarity-tinted emblems, carousels and rails. Badge /
   achievement / cosmetic **artwork prompts live in [`image-prompts.md`](./image-prompts.md)** —
   generate, upload to Supabase Storage, swap the URLs in; emoji fallbacks until then.

---

## 🧭 Information Architecture — dissolve the Hub into Profile + Discover

The Community/Reading **Hub screen is removed**. After deleting reviews, gamification and badge
rows it's a thin link list, and its features belong on two strong home surfaces plus Settings:

| Today (Hub row) | New home |
|---|---|
| Rewards / Spirit Stones / Titles | **Profile** (gamification folded in) |
| Badge Store / Manage Badges | **Profile** → "Customize" sheet + badge showcase |
| Reading Buddy (companion + daily quote) | **Profile** (personal companion widget) |
| My Quotes (personal collection) | **Profile** → Quotes tab |
| Daily Check-in | **Profile** header action |
| Community Reviews (firehose) | **Deleted** → social feed + Discover (see Reviews §) |
| Popular Books | **Discover** storefront (community home) |
| Leaderboard | **Discover** → "Hall of Readers" |
| Character Art (+ Discord) | **Discover** → community gallery / Discord link |
| Community News | **Discover** top card |
| Glossary / Community Source / User Sources / Legado Sources | **Settings → Sources** |
| Feature Store / Plugin Repositories / Developer Portal | **Settings → Extensions** |
| Admin: User Management / (badge verification deleted) | **Settings → Admin** |

Result — two community-facing destinations, no hub middleman:
- **Profile = "you"**: identity, stats, gamification, cosmetics, companion, quotes, your activity.
- **Discover = "everyone"**: trending books, leaderboard, top reviews, character art, news.
- Tools/sources/plugins/admin go to **Settings** where utilities belong (not "community").

Delete `CommunityHubScreen` (+ `CommunityHubScreenSpec`), its route and `CommonNavHost` entry, and
re-point whatever opened the Hub (e.g. a Settings/More row or bottom-nav slot) to **Discover**.

---

## 👤 Signed-out / Local-first behavior

The Profile and Discover must be useful **before** an account exists.

**Profile, signed-out** — render entirely from local `readingStatistics` (SQLDelight):
- Show: reading time, chapters read, books completed, current/longest streak, favorite genres,
  computed **Level/XP** (client mirror of `calculate_level`), and **achievement progress** with
  locally-unlocked achievements celebrated offline.
- Hide/replace with CTAs: followers/following, public comments, leaderboard rank, cloud Spirit
  Stone balance, Share-to-Discord, Customize purchases → each shows a single inline
  **"Sign in to join the community"** prompt instead of the live control.
- Header swaps the avatar/username block for a "Guest Reader" card + Sign-in button.

**Discover, signed-out** — fully browsable (popular books, leaderboard, top reviews, news are
public reads). Actions that write (vote, follow, review) prompt sign-in on tap.

**Sign-in reconciliation** — on first sign-in, push local cumulative stats via
`rpc('sync_reading_stats', …)` (monotonic-merge, §Achievement Engine); the server re-evaluates and
becomes canonical. Locally pre-unlocked achievements simply confirm server-side; never double-pay
(the `UNIQUE`/`is_completed` guard handles it).

---

## Currency Model (virtual, earned-only)

We keep a single, simple economy. Drop "Power Stones vs Spirit Stones" duplication and the
"trade stones / buy stones" mechanics — those existed to sell currency. We don't sell anything.

| Currency | How it is earned | What it is spent on |
|----------|------------------|---------------------|
| **XP** (level) | Reading time, chapters read, books completed, streaks, reviews, check-ins | Nothing — it only raises your **Level** and unlocks titles/badges |
| **Spirit Stones** (soft currency) | Daily check-in, achievements, streak milestones, first review of the day, events | **Cosmetic-only sinks**: profile themes, avatar frames, titles, profile badges |

Rules:
- **No money path in, no money path out.** Remove `badges.price DECIMAL(10,2)` money pricing →
  reprice badges in **Spirit Stones** (`cost_spirit_stones INT`). Remove `payment_proofs`,
  `nft_wallets`, `eth_wallet_address`, `is_supporter` from the gamification flow (leave the
  columns if other code depends on them, but nothing new should read/write them).
- **Spend is cosmetic only.** Spirit Stones never unlock content, never gate chapters, never
  affect rankings. They buy *identity*: themes, frames, titles, badges. This keeps it fair and
  removes any pay-to-win / pay-to-read perception.
- **Power Stones / book voting**: keep as a *free, daily, earned* vote (a "like" with a budget),
  purely to drive the community Trending list. No currency cost, no author payout.

---

## Profile Screen Layout (trimmed: no authors, no gifts)

```
┌─────────────────────────────────────────────────────────┐
│  COVER IMAGE (full width, ~220dp, dark gradient bottom)  │
│  ┌──────────────────────────────────────────────────┐   │
│  │  [Avatar 80dp, level-colored ring]      [⋮] [✏️]  │   │
│  │  Username (bold, 22sp)                            │   │
│  │  Lvl 5 • Novice Reader  •  💎 150 Spirit Stones   │   │
│  │  "Bio…" (14sp, 2 lines, expandable)               │   │
│  │  📅 Joined Jan 2024                               │   │
│  │  🎮 Discord: linked ✓ (@handle)  🟢 12 online    │   │
│  └──────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  STATS ROW (4 cols): ⏱ Time · 📚 Books · 🔥 Streak · ⭐ │
│  XP PROGRESS BAR → next level   •   🏆 Leaderboard #123  │
├─────────────────────────────────────────────────────────┤
│  ACHIEVEMENT SHOWCASE (horizontal scroll, earned badges) │
├─────────────────────────────────────────────────────────┤
│  ACTIVE TITLE  📛 "Speed Reader"  [Change]               │
├─────────────────────────────────────────────────────────┤
│  FAVORITE BOOKS (showcase, horizontal scroll)  [Share ↗]│
├─────────────────────────────────────────────────────────┤
│  READING ACTIVITY (last 7 days)                          │
│   📖 "Book" – Ch 45 (2h ago)                             │
│   🏅 Earned "100 Chapters" achievement (1d ago)          │
│   ⭐ Reviewed "Another Book" (2d ago)                    │
│   (NO "gifted author" rows — removed)                    │
├─────────────────────────────────────────────────────────┤
│  FOLLOWING 12 · FOLLOWERS 34   [View All →]              │
├─────────────────────────────────────────────────────────┤
│  PUBLIC COMMENTS (reader-to-reader on your wall)         │
│   [Avatar] User1: "Great taste!" 👍12                   │
│   [+ Write a public comment…]                            │
├─────────────────────────────────────────────────────────┤
│  💬 JOIN THE CONVERSATION ON DISCORD  [Open Server →]    │
└─────────────────────────────────────────────────────────┘
```

**Three-dot menu:** Edit Profile · Share Profile (→ Discord/link) · Copy Link · Report (others).

**Edit Profile fields:** Cover (upload/preset) · Avatar · Username · Bio (≤200) · Discord link
(OAuth, see below) · Profile Theme · Show/Hide Reading Activity · Show/Hide Favorite Books.
*(Removed: Twitter, Website, generic "Discord server" free-text — replaced by real Discord linking.)*

---

## Community Features (trimmed)

### Kept
1. **Daily Check-in** — consecutive days raise the reward. Day 1: 10 stones · Day 7: 50 + badge ·
   Day 30: 200 + exclusive title. Streak resets if missed. *Optionally* posts your streak to a
   Discord `#reading-streaks` channel (toggle).
2. **Book Voting (Power Stones)** — free daily vote budget; drives **Trending** list. No author payout.
3. **Reviews & Ratings** — 1–5 stars + text; other readers mark "Helpful". Top reviewers earn a
   badge. **No author response field.** (Reconcile with existing `book_reviews`; add `helpful_count`.)
4. **Following / Followers** — reader↔reader only. Following feed = friends' reading activity.
   **Remove "follow author".**
5. **Reading Challenges** — weekly/monthly community goals; rewards = badges, titles, stones.
6. **Achievements & Titles** — earned via milestones; titles give cosmetic flair (and a small,
   purely cosmetic XP-boost flavor if desired). Bought with Spirit Stones, never money.
7. **Leaderboard** — already exists; surface rank on profile.

### Removed (handled in Discord, or money-related)
- ❌ Author–reader interaction, follow author, author notes
- ❌ Gift system / gifts to authors / "Top Gifter"
- ❌ In-app Book Clubs (Discord owns clubs)
- ❌ Buying currency, trading stones, real-money badge prices, NFT/crypto badges

---

## 💬 Discord Integration (the main new direction)

Goal: make the app and the Discord server feel like one product. The app *recognizes* you;
Discord is where you *talk*. Every recognition moment offers a one-tap bridge to Discord.

### 0. Fix what's already there (cleanup, do first)
- There are **two different invite links** in the codebase: `Constants.discord =
  "https://discord.gg/HBU6zD8c5v"` and a hardcoded `"https://discord.gg/HHZZfnCm"` in
  `CommunityHubScreen.kt`. **Pick one, route everything through `Constants.discord`.**
- Generalize the existing `DiscordQuoteRepositoryImpl` (quote → webhook embed + image) into a
  reusable `DiscordShareRepository` that can post *any* embed (achievement, review, milestone,
  favorite-books showcase). The webhook + embed + image plumbing already works — reuse it.

### 1. Link your Discord account (foundation) — **Supabase-only**
- Use **Supabase Auth's native Discord OAuth provider** (Dashboard → Auth → Providers → Discord).
  No backend of our own — Supabase handles the OAuth dance and redirect.
- On link, read the identity and store `discord_id`, `discord_username`, `discord_avatar` on
  `public.users`. Linked accounts earn a **"Verified Reader"** badge in-app (granted by our
  Supabase function, not a bot).
- Show the linked Discord handle + avatar on the profile header.

### 2. Live server presence (zero-auth, high value, easy)
- Use Discord's **public Guild Widget API**: `GET https://discord.com/api/guilds/<GUILD_ID>/widget.json`
  (requires "Enable Server Widget" in server settings — no bot, no token).
- Surface **"🟢 N readers online now"** in the Discover header and on the profile, with an
  instant-invite button straight from the widget payload. Cache ~60s.

### 3. Share-to-Discord everywhere (extend existing webhook)
One-tap "Share ↗" that posts a nice embed (reuse the image-card generator) to the right channel:
- **Achievement unlocked** → `#achievements`
- **Level up / streak milestone** → `#milestones`
- **New review** → `#reviews`
- **Favorite-books showcase** → `#bookshelf`
- **Daily check-in streak** (optional toggle) → `#reading-streaks`
- Quotes already work → keep, route through the unified repository.

### 4. Channel deep-links in context
Place "Discuss on Discord" buttons where they belong, deep-linking to specific channels
(`discord://` with web fallback): book detail → `#book-discussion`, character art → art channel,
help/settings → `#support`, Discover → server home.

### 5. Community News feed (announcements) — **Supabase-only, no bot**
- We will **not** run a Discord bot or relay (that would be a server). Instead, admins post
  announcements **from the existing in-app Admin panel** into a `community_announcements`
  Supabase table.
- The app shows a **"Community News"** card at the top of Discover from that table.
- *Optional, still serverless*: when an admin posts, the app also fires the **existing Discord
  webhook** so the same announcement lands in `#announcements`. One author action → both
  surfaces, no backend.

### 6. Spirit Stones → Discord perks — **out of scope (needs a bot)**
- Auto-assigning Discord roles requires a bot/server, which we are explicitly avoiding. **Cut
  this.** Spirit Stones spend stays **100% in-app cosmetic** (themes, frames, titles, badges).
  Discord roles, if ever wanted, are assigned manually by mods — not our app's job.

### 7. Discord Rich Presence (desktop target only, optional)
- On the desktop build, show **"Reading <Book> · Ch N on IReader"** as Discord activity, so the
  user's friends see what they're reading. Pure flex, drives curiosity → joins.

### Discord priority order
Cleanup (§0) → Live presence widget (§2) → Share-to-Discord (§3) → Channel deep-links (§4) →
Account linking (§1, Supabase Auth) → Community News (§5, admin-authored). §6 cut. §7 optional.

> **No-server rule (applies to the whole plan):** the only backend is Supabase — Postgres
> tables, RLS, `SECURITY DEFINER` RPC functions, triggers, and Supabase Auth. No bot, no relay,
> no edge server of our own. Discord receives data only through *outbound webhooks fired by the
> client*; it never calls us.

---

## 🏆 Achievement & Reward Engine (Supabase-only, no server)

This is the heart of the "feeling of receiving something." It must be **fair, idempotent,
once-only, and impossible to fake from a replaying client** — and it runs entirely inside
Supabase (Postgres functions + RLS), no backend of ours.

### Design goals
1. **Derive everything from canonical stats.** Achievements are computed from the user's *real*
   cumulative numbers (reading minutes, chapters, books, streak, reviews, votes, check-ins,
   genres, speed). No client ever says "give me achievement X" — it says "here are my stats,"
   and Supabase decides what was earned.
2. **Grant exactly once.** `UNIQUE(user_id, achievement_id)` + an `is_completed` guard means
   re-running the evaluator is safe and never double-pays.
3. **Catalog lives in the DB**, not the binary — so we can add/tune achievements without an app
   release. The app renders whatever `achievement_definitions` contains.
4. **Cheat-resistant by construction.** Stat writes go through a `SECURITY DEFINER` function that
   only allows **monotonic increase**; `user_achievements`, `xp`, and `spirit_stones` are
   **read-only via RLS** and writable *only* by the evaluator function. The client cannot grant
   itself rewards directly.

### Data model (new tables)
```sql
-- Editable catalog. Add rows = add achievements, no app update.
CREATE TABLE IF NOT EXISTS public.achievement_definitions (
    id            TEXT PRIMARY KEY,            -- 'reading_100h'
    name          TEXT NOT NULL,               -- 'Bookworm'
    description   TEXT NOT NULL,               -- 'Read for 100 hours total'
    icon          TEXT NOT NULL DEFAULT '🏅',
    category      TEXT NOT NULL,               -- READING_TIME, CHAPTERS, BOOKS, STREAK,
                                               -- CHECKIN, REVIEW, VOTE, SOCIAL, GENRE, SPEED, SPECIAL
    tier          TEXT NOT NULL DEFAULT 'BRONZE', -- BRONZE/SILVER/GOLD/PLATINUM/LEGENDARY
    metric        TEXT NOT NULL,               -- which canonical stat to compare
    threshold     BIGINT NOT NULL,             -- value of metric needed
    reward_xp     INT  NOT NULL DEFAULT 0,
    reward_stones INT  NOT NULL DEFAULT 0,
    reward_title_id TEXT,                       -- optional cosmetic title granted
    reward_badge_id TEXT,                       -- optional cosmetic badge granted
    is_secret     BOOLEAN DEFAULT false,        -- hidden until earned
    is_active     BOOLEAN DEFAULT true,
    sort_order    INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS public.user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    achievement_id TEXT REFERENCES public.achievement_definitions(id) ON DELETE CASCADE,
    progress     BIGINT  DEFAULT 0,            -- current metric value, capped at threshold
    is_completed BOOLEAN DEFAULT false,
    earned_at    TIMESTAMPTZ,
    UNIQUE(user_id, achievement_id)
);
```
`metric` values map to canonical stats already available in the app/DB:
`READING_MINUTES`, `CHAPTERS_READ`, `BOOKS_COMPLETED`, `STREAK_DAYS`, `LONGEST_STREAK`,
`CHECKINS_TOTAL`, `REVIEWS_WRITTEN`, `HELPFUL_RECEIVED`, `VOTES_CAST`, `GENRES_EXPLORED`,
`AVG_WPM`, `FOLLOWERS`, `DISCORD_LINKED`.
(Source: `ReadingStatisticsType1` + `leaderboard` + counts from `book_reviews` / `power_stone_votes`
/ `daily_checkins` / `user_follows` / `users.discord_id`.)

### The granting logic (two SQL functions — this is the "good reward logic")
```sql
-- 1) Client calls this after a reading session / on app open. SECURITY DEFINER.
--    Rejects any decrease (anti-spoof); stores the user's canonical cumulative stats.
sync_reading_stats(p_minutes, p_chapters, p_books, p_streak, p_longest, p_avg_wpm, p_genres)
    → updates public.users / public.leaderboard with GREATEST(old, new) per column
    → then calls evaluate_achievements(auth.uid())
    → returns the list of newly-unlocked achievements (for the celebration UI)

-- 2) Pure, idempotent evaluator. SECURITY DEFINER. Never double-pays.
evaluate_achievements(p_user UUID) RETURNS SETOF achievement_unlock
  FOR each row d IN achievement_definitions WHERE is_active:
     v := canonical metric value for (p_user, d.metric)         -- single CASE lookup
     UPSERT user_achievements(p_user, d.id, progress = LEAST(v, d.threshold))
     IF v >= d.threshold AND NOT already completed:
         mark is_completed, earned_at = now()
         users.xp           += d.reward_xp
         users.spirit_stones += d.reward_stones
         INSERT spirit_stone_transactions(+d.reward_stones, 'ACHIEVEMENT', d.id)
         IF d.reward_title_id: INSERT user_titles(...)            -- cosmetic
         IF d.reward_badge_id: INSERT user_badges(...)            -- cosmetic
         INSERT reading_activity('ACHIEVEMENT', d.name)           -- shows on profile feed
         yield d as newly-unlocked
  recompute users.level via calculate_level(users.xp)
```
- **Level curve** lives in one place: `calculate_level(xp BIGINT)` (e.g. a gentle quadratic so
  early levels are quick and later ones meaningful). Client mirrors it only for display; the DB
  is source of truth. (Replaces the current "level = minutes/60" coupling.)
- **Daily check-in, voting, reviews** each call their own `SECURITY DEFINER` RPC
  (`checkin_daily`, `vote_book`, `submit_review`) that awards its stones, then calls
  `evaluate_achievements` so check-in/review/vote achievements unlock in the same round-trip.
- **RLS:** `achievement_definitions` = public read. `user_achievements`, `users.xp/stones`,
  `spirit_stone_transactions` = **select-only for the owner; no client insert/update** — the
  `SECURITY DEFINER` functions are the only writers. This makes rewards unforgeable without a
  server.

### Reward tuning (per tier)
| Tier | XP | Stones | Extra |
|------|----|--------|-------|
| Bronze | 25 | 10 | — |
| Silver | 75 | 25 | — |
| Gold | 200 | 50 | sometimes a Title |
| Platinum | 500 | 100 | Title |
| Legendary | 1200 | 250 | exclusive Title + Badge |

### Achievement catalog (brainstorm — seed `achievement_definitions`)
Each line is a tiered ladder; thresholds escalate, rewards scale by tier.
- **⏱ Reading Time** (min): 60 · 600 · 3 000 · 6 000 · 15 000 · 30 000 · 60 000
- **📖 Chapters**: 10 · 100 · 500 · 1 000 · 5 000 · 10 000
- **📚 Books Completed**: 1 · 5 · 10 · 25 · 50 · 100
- **🔥 Streak (days)**: 3 · 7 · 14 · 30 · 100 · 365
- **📅 Check-ins (cumulative)**: 7 · 30 · 100 · 365
- **⭐ Reviews written**: 1 · 10 · 50 — top tier grants "Critic" title
- **👍 Helpful votes received**: 10 · 100 · 500
- **🗳 Power-Stone votes cast**: 10 · 100 · 1 000
- **🧭 Genres Explored** (distinct favorite genres): 3 · 5 · 10 — "Genre Explorer" title
- **⚡ Avg Reading Speed (WPM)**: 300 · 500 — "Speed Reader" title
- **👥 Social**: first follower · 10 followers · 50 followers · first profile comment
- **🎮 Discord**: link account → "Verified Reader" (instant)
- **🌟 Special/seasonal** (`is_secret`): event-only, manually toggled via `is_active`

### Client flow
1. After a reading session (or on app open), the reader-tracking layer calls
   `rpc('sync_reading_stats', …)`.
2. The RPC returns `newly_unlocked: [...]`.
3. If non-empty, show a **celebration sheet** (icon burst, "+XP +💎", confetti) and a toast per
   unlock, with a **"Share ↗"** button → Discord (§3 above).
4. Profile's achievement showcase + reading-activity feed update from Supabase.

---

## 🎨 UI Redesign — Leaderboard (Webnovel-grade, designed from scratch)

> Ignore the current leaderboard UI entirely. New concept: a **ranked-ladder "Hall of Readers"**
> — a podium hero, tier ladder, metric/period switching, live rank chase.

```
┌───────────────────────────────────────────────────────────┐
│  ◀  Hall of Readers                                    ⓘ  │  ← collapsing top bar
│  ╭─ Weekly ─╮ Monthly │ All-Time           (segmented pills)│
│  Reading Time ▾   (metric switch: Time·Chapters·Streak·Books)│
├───────────────────────────────────────────────────────────┤
│            ✦  themed gradient banner  ✦                    │
│                     ┌──────┐                               │
│            ┌──────┐ │  👑  │ ┌──────┐                       │  ← PODIUM
│            │  ②  │ │ AVTR │ │  ③  │                        │     #1 raised + crown + glow
│            │ AVTR │ │ Lv28 │ │ AVTR │                       │     #2 left, #3 right
│            │ Lv24 │ │ Mira │ │ Lv21 │                       │     level ring on avatar
│            │ Kano │ │12 480│ │ Ren  │                       │
│            │ 9 120│ │ min  │ │8 740 │                       │
│            └──────┘ └──────┘ └──────┘                       │
├───────────────────────────────────────────────────────────┤
│  TIER: 💠 Platinum  ·  Top 4%        [how tiers work →]    │  ← user's tier band
├───────────────────────────────────────────────────────────┤
│  4  ▲2  [av] Aria   «Speed Reader»            8 210 min    │  ← rows from #4
│  5  ▼1  [av] Devon  «Bookworm»                7 905 min    │     rank·movement·avatar·
│  6  –   [av] Sol                              7 640 min    │     title chip·metric
│  7  ▲5  [av] Yuki   «Night Owl»               7 110 min    │
│  …                                                         │
├───────────────────────────────────────────────────────────┤
│  ▸ YOU  #128 ▲6   [av]  4 980 min                          │  ← STICKY pinned bar
│     ▕▔▔▔▔▔▔▔░░░▏  210 min to pass #127                     │     chase loop to next rank
└───────────────────────────────────────────────────────────┘
```
**Key ideas**
- **Podium hero** with #1 center-raised, gold crown + animated glow/shimmer; #2/#3 flanking,
  silver/bronze rings. Avatars carry the level-colored ring used everywhere.
- **Segmented period** (Weekly / Monthly / All-Time) + **metric dropdown** (Time / Chapters /
  Streak / Books). Webnovel separates "Power" vs "Collection" rankings; we generalize to metrics.
- **Tier ladder** (Bronze→Silver→Gold→Platinum→Diamond→Legend) by rank percentile, with an
  emblem and "Top X%" — gives mid-pack users a goal that isn't "be #1."
- **Rank-movement chevrons** (▲ green / ▼ red / – gray) vs. previous period snapshot
  (store a `leaderboard_snapshots` table or a `prev_rank` column refreshed weekly).
- **Sticky "YOU" bar** always visible with a progress bar to the next rank — the core retention
  hook. Tapping any row → existing `UserProfileDialog` / new public profile.
- Skeleton-shimmer load, pull-to-refresh, seasonal banner theming, empty state ("Be the first").

---

## 🎨 UI Redesign — Popular Books (Webnovel bookstore, from scratch)

> Ignore the current popular-books UI. New concept: a **discovery storefront** — featured
> carousel, genre chips, themed rails, and a numbered Top-10 ranking centerpiece.

```
┌───────────────────────────────────────────────────────────┐
│  ◀  Discover                                       🔍  ☰  │
├───────────────────────────────────────────────────────────┤
│  ╔═══════════════ FEATURED CAROUSEL ═══════════════╗      │  ← auto-advancing banners
│  ║  [blurred cover bg]      ┌────┐                  ║      │     blurred cover backdrop +
│  ║   🔥 #1 this week        │COVR│  Shadow Monarch  ║      │     sharp thumb, genres,
│  ║                          │    │  Fantasy·Action  ║      │     reader count, ▶ Read CTA
│  ║                          └────┘  2 341 reading   ║      │
│  ║                                  [ ▶ Read ]      ║      │
│  ╚═══════════════ ● ○ ○ ○ ═════════════════════════╝      │
├───────────────────────────────────────────────────────────┤
│  [All][Fantasy][Romance][Sci-Fi][Action][Drama]…   chips  │  ← genre filter (from genres[])
├───────────────────────────────────────────────────────────┤
│  🔥 Trending Now                              See all →    │  ← horizontal rail
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ →                            │     rank badge corner,
│  │①COV│ │②COV│ │③COV│ │④COV│                              │     flame + reader count,
│  │Titl│ │Titl│ │Titl│ │Titl│                              │     ⭐ rating from reviews
│  │🔥2.3k│🔥1.9k│🔥1.5k│🔥1.2k│                            │
│  └────┘ └────┘ └────┘ └────┘                              │
├───────────────────────────────────────────────────────────┤
│  📈 Rising This Week                          See all →    │  ← biggest reader-count growth
│  ┌────┐ ┌────┐ ┌────┐ →                                   │
├───────────────────────────────────────────────────────────┤
│  🏆 Top 10 Most Read                                       │  ← numbered ranking centerpiece
│  ╔═╗ ┌──┐ Reverend Insanity      ⭐4.8  Ongoing           │     big gradient numerals
│  ║1║ │CV│ Fantasy · Xianxia      ▕▇▇▇▇▇▏ 4 120 readers    │     (1-3 gold/silver/bronze),
│  ╚═╝ └──┘                                                  │     status pill, reader bar
│  ╔═╗ ┌──┐ Lord of Mysteries      ⭐4.9  Completed          │
│  ║2║ │CV│ Mystery · Steampunk    ▕▇▇▇▇░▏ 3 880 readers    │
│  …                                                         │
├───────────────────────────────────────────────────────────┤
│  🆕 New & Noteworthy            (2-col staggered grid)     │
│  ┌──────┐ ┌──────┐                                        │
│  │ COVER│ │ COVER│   recently added community books        │
│  └──────┘ └──────┘                                        │
└───────────────────────────────────────────────────────────┘
```
**Key ideas**
- **Featured carousel**: full-bleed, blurred-cover backdrop with a sharp thumbnail, auto-advance
  + page dots; the week's #1 by `readerCount`. Big ▶ Read CTA.
- **Genre chips** sourced from `community_books.genres[]`; tapping filters every rail below.
- **Themed rails**: 🔥 Trending (by `readerCount`), 📈 Rising (largest recent growth — needs a
  daily `reader_count_history` snapshot or derive from `lastRead`), each a horizontal pager with
  corner rank badges, flame + count, and ⭐ rating averaged from `book_reviews`.
- **Top-10 ranking** centerpiece: oversized gradient numerals (1–3 gold/silver/bronze), cover,
  title, author, genre chips, a reader-count bar, and a status pill (Ongoing/Completed/Hiatus).
- **Card actions**: tap → detail with **▶ Read / + Library**, a **Power-Stone vote** button
  (free daily, §Community), and **💬 Discuss on Discord** deep-link.
- **NSFW** covers blurred by default (honor `is_nsfw`), tap-to-reveal. Source resolution reuses
  the existing `ResolvePopularBookSourceUseCase`. Skeleton shimmer, pull-to-refresh, empty state.

---

## 🗑 Screens to delete from the UI (fold into Profile)
Per `profile-gamification-integration.md` — remove these standalone screens entirely; their
content now lives inside the redesigned **Profile**:
- `RewardScreen` (+ `RewardViewModel`, `RewardScreenSpec`)
- `SpiritStoneScreen` (+ `SpiritStoneViewModel`, `SpiritStoneScreenSpec`)
- `UserTitleScreen` (+ `UserTitleViewModel`, `UserTitleScreenSpec`)
- `CommunityHubScreen` (+ `CommunityHubScreenSpec`) — the **whole hub is dissolved** into
  Profile + Discover + Settings (see §Information Architecture). Re-point its entry to Discover.
- Reading Buddy + My Quotes stop being hub destinations and **fold into Profile** (companion
  widget + Quotes tab); keep their data/use-cases, drop the hub plumbing.
- Remove all DI registrations (`ScreenModelModule.kt`), nav routes, and `CommonNavHost` entries
  for the above.

---

## 🏷 Badge System Consolidation (delete the money store, keep the badges)

The current badge feature is really **three separate things** — treat them differently:

**1. Delete the money/NFT machinery outright** (violates no-money + no-crypto, §Principles 1 & 6):
- `BadgeStoreScreen` (+ `BadgeStoreViewModel`, `BadgeStoreScreenSpec`) — it exists to sell
  donation/supporter badges for **real money** via a payment-proof flow. Gone.
- `AdminBadgeVerificationScreen` (+ ViewModel, Spec) — it approves those money purchases. Gone.
- Drop **NFT_EXCLUSIVE** badges, `payment_proofs`, `nft_wallets`, `eth_wallet_address`,
  `is_supporter` from all new flows. Remove the `badges.type = 'PURCHASABLE'/'NFT_EXCLUSIVE'`
  paths; keep only `ACHIEVEMENT` + a new `COSMETIC` type bought with **Spirit Stones**.

**2. Fold badge *management* into the Profile** (no standalone screen):
- `BadgeManagementScreen` (+ ViewModel, Spec) — equipping/showcasing badges belongs on the
  profile you're editing. Delete it; the **Achievement/Badge showcase on Profile** gains an
  "edit showcase" affordance (reuse `SetPrimaryBadgeUseCase`/`SetFeaturedBadgesUseCase`).

**3. Keep `badges`/`user_badges` data** — they're ideal as cosmetic + achievement rewards:
- Acquiring cosmetics (badges, avatar frames, profile themes, titles for **Spirit Stones**)
  lives in a **"Customize" bottom-sheet launched from the Profile** — *not* a Community Hub entry
  and *not* a full standalone store screen. A profile scroll shouldn't host a shop grid, but a
  sheet from the profile keeps all the logic on the profile, as intended.

**Hub note:** the Community Hub is dissolved entirely (§Information Architecture), so its
**"Badges & Customization"** section and the admin **badge verification** row disappear with it.
Identity/cosmetics live on **Profile**; the hub is no longer a destination.

---

## ⭐ Reviews: kill the firehose, make reviews a discovery *signal*

`AllReviewsScreen` ("Community Reviews" in the hub) is a **global firehose** of every book +
chapter review from everyone — reviews ripped out of context, about unknown books, from strangers
you don't follow. It's the weakest possible surface, and reviews already live in two better
places: **book detail** (`ReviewSection`/`ReviewsBottomSheet`) and the **reader**
(`ChapterReviewSection`/`ChapterReviewsBottomSheet`).

**Decision: delete the standalone feed; redistribute its value into three higher-signal places.**

1. **Followed-reader reviews → the social / Following feed.** A review from someone you follow is
   a *recommendation*; surface it in the Profile/Following activity feed and a Community "Friends
   Activity" view: *"Aria rated Lord of Mysteries ★★★★★"* → tap → book. Reviews become
   **discovery through trust** (the real social-reading magic), reusing the `user_follows` graph.
2. **Reviews feed the Discover storefront.** Aggregate `book_reviews` → the ⭐ rating + a "what
   readers say" snippet on book cards and the Top-10. Review *content* becomes part of book
   discovery, not a separate tab.
3. **"Top Reviews this week" — finite & curated, not a doomscroll.** A small rotating module
   sorted by `helpful_count`, shown in Discover / Reading Buddy. Rewards good reviewers (ties to
   the **top-reviewer achievement/badge**); each has **"Share to Discord"** (existing webhook) so
   the *discussion* moves to Discord while the app keeps reviews as structured data.

**Delete:** `AllReviewsScreen` (+ `AllReviewsViewModel`, `AllReviewsState`, `AllReviewsScreenSpec`)
and the `onAllReviews` **"Community Reviews"** row in `CommunityHubScreen.kt` (+ its nav route /
`CommonNavHost` entry). **Keep:** `book_reviews`/`chapter_reviews` data, `WriteReviewDialog`,
`ReviewCard`, book-detail and in-reader review sections. Chapter reviews stay **in-reader only**.

---

## Supabase Schema — reconcile with existing DB

> The repo already has `public.users`, `public.leaderboard`, `public.book_reviews`,
> `public.community_quotes`, `public.badges`, `public.user_badges`. **Extend these**; only add
> genuinely new tables. Do NOT create `user_profiles` (it duplicates `users` + `leaderboard`).

### Extend `public.users` (profile + economy fields)
```sql
ALTER TABLE public.users
  ADD COLUMN IF NOT EXISTS display_name   TEXT,
  ADD COLUMN IF NOT EXISTS bio            TEXT    DEFAULT '',
  ADD COLUMN IF NOT EXISTS avatar_url     TEXT,
  ADD COLUMN IF NOT EXISTS cover_image_url TEXT,
  ADD COLUMN IF NOT EXISTS cover_theme    TEXT    DEFAULT 'default',
  ADD COLUMN IF NOT EXISTS level          INT     DEFAULT 1,
  ADD COLUMN IF NOT EXISTS xp             BIGINT  DEFAULT 0,
  ADD COLUMN IF NOT EXISTS level_title    TEXT    DEFAULT 'Novice Reader',
  ADD COLUMN IF NOT EXISTS spirit_stones  BIGINT  DEFAULT 0,
  ADD COLUMN IF NOT EXISTS active_title_id TEXT,
  ADD COLUMN IF NOT EXISTS checkin_streak INT     DEFAULT 0,
  ADD COLUMN IF NOT EXISTS last_checkin_date DATE,
  ADD COLUMN IF NOT EXISTS is_public_profile BOOLEAN DEFAULT true,
  ADD COLUMN IF NOT EXISTS show_reading_activity BOOLEAN DEFAULT true,
  ADD COLUMN IF NOT EXISTS show_favorite_books   BOOLEAN DEFAULT true,
  -- Discord
  ADD COLUMN IF NOT EXISTS discord_id       TEXT,
  ADD COLUMN IF NOT EXISTS discord_username TEXT,
  ADD COLUMN IF NOT EXISTS discord_avatar   TEXT;
```
*(Reading totals — time/chapters/books/streak — already live in `public.leaderboard`; read them
from there instead of re-storing on the user.)*

### Reprice badges in Spirit Stones (remove money + NFT)
```sql
ALTER TABLE public.badges
  ADD COLUMN IF NOT EXISTS cost_spirit_stones INT;   -- replaces money `price`
-- Stop writing `price`. Keep only types 'ACHIEVEMENT' (earned) and 'COSMETIC' (Spirit Stones).
-- Retire 'PURCHASABLE'/'NFT_EXCLUSIVE' rows; ignore the money `price` column going forward.
-- payment_proofs / nft_wallets stay in the DB untouched but no new code reads/writes them.
```

### Modify existing `public.book_reviews` (no author response)
```sql
ALTER TABLE public.book_reviews
  ADD COLUMN IF NOT EXISTS helpful_count INT DEFAULT 0;
-- Do NOT add author_response. No author features.
```

### New tables (genuinely new)
```sql
-- reader↔reader follows
CREATE TABLE IF NOT EXISTS public.user_follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id  UUID REFERENCES public.users(id) ON DELETE CASCADE,
    following_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(follower_id, following_id)
);

CREATE TABLE IF NOT EXISTS public.user_titles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    title_id TEXT NOT NULL,
    title_name TEXT NOT NULL,
    rarity TEXT DEFAULT 'COMMON',
    is_active BOOLEAN DEFAULT false,
    acquired_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, title_id)
);

-- NOTE: `achievement_definitions` and `user_achievements` are defined in the
-- "Achievement & Reward Engine" section above — do not redefine them here.

-- earned-only ledger; type is an EARN/SPEND reason, never a purchase
CREATE TABLE IF NOT EXISTS public.spirit_stone_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    amount BIGINT NOT NULL,             -- + earned, - spent on cosmetics
    type TEXT NOT NULL,                 -- CHECKIN, ACHIEVEMENT, STREAK, COSMETIC_PURCHASE...
    description TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.power_stone_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.daily_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    checkin_date DATE NOT NULL,
    streak_day INT DEFAULT 1,
    reward_amount INT DEFAULT 10,
    UNIQUE(user_id, checkin_date)
);

CREATE TABLE IF NOT EXISTS public.profile_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    commenter_id    UUID REFERENCES public.users(id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    likes_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.reading_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL,        -- READING, REVIEW, VOTE, ACHIEVEMENT (no GIFT)
    book_id TEXT, book_title TEXT, chapter_number INT,
    description TEXT DEFAULT '',
    is_public BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Community News: admin-authored in-app (NO bot). See Discord §5.
CREATE TABLE IF NOT EXISTS public.community_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT, body TEXT,
    author_id UUID REFERENCES public.users(id) ON DELETE SET NULL,
    discord_message_url TEXT,                    -- optional link if also webhooked
    posted_at TIMESTAMPTZ DEFAULT NOW()
);

-- Weekly rank snapshot → powers ▲▼ movement chevrons on the leaderboard
CREATE TABLE IF NOT EXISTS public.leaderboard_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    metric TEXT NOT NULL,                         -- READING_MINUTES, CHAPTERS, STREAK, BOOKS
    period TEXT NOT NULL,                         -- WEEKLY, MONTHLY, ALL_TIME
    rank INT NOT NULL,
    snapshot_date DATE NOT NULL,
    UNIQUE(user_id, metric, period, snapshot_date)
);

-- Daily reader-count snapshot → powers the "📈 Rising This Week" rail
CREATE TABLE IF NOT EXISTS public.reader_count_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id TEXT NOT NULL,
    reader_count BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    UNIQUE(book_id, snapshot_date)
);
```
*(Snapshots are refreshed by a Supabase **scheduled SQL job** via `pg_cron` — still no server of
ours. Dropped vs. old plan: `shop_items`/`user_inventory` money shop, `gift`/author tables, and
the duplicate `user_profiles`/`book_reviews`. Cosmetics are sold for Spirit Stones via reused
`badges`/`user_badges` + `user_titles`.)*

Add **RLS** on every new table (owner-write; public-read where `is_public`/profile-public).

---

## Implementation Phases

### Phase 0 — Discord cleanup (quick win)
- Consolidate the two invite links onto `Constants.discord`.
- Refactor `DiscordQuoteRepositoryImpl` → generic `DiscordShareRepository` (embed + image card).

### Phase 1 — Supabase data layer
- `ALTER` `users`, `badges`, `book_reviews`; create all new tables above; add RLS.
- **Lock down rewards:** revoke client INSERT/UPDATE on `user_achievements`, `users.xp/stones`,
  `spirit_stone_transactions`; expose writes only through `SECURITY DEFINER` functions.
- Functions: `calculate_level(xp)`, `sync_reading_stats(...)`, `evaluate_achievements(user)`,
  `checkin_daily`, `vote_book`, `submit_review`, `spend_stones` (cosmetic-only).
- `pg_cron` jobs: weekly `leaderboard_snapshots`, daily `reader_count_history`.
- Seed `achievement_definitions` from the catalog brainstorm.

### Phase 2 — Achievement & Reward engine wiring
- Reader-tracking layer calls `rpc('sync_reading_stats')` after sessions / on app open.
- Handle the returned `newly_unlocked` list → celebration sheet + per-unlock toast + Share↗.
- Replace the in-app `RewardEngineUseCase` heuristics with the DB as source of truth (keep a
  thin client mirror of `calculate_level` for display only).

### Phase 3 — Domain layer
- Repositories + use cases for follows, titles, achievements, stones, check-in, voting,
  activity, comments, announcements, Discord share/widget. Update DI modules.

### Phase 4 — Remove screens & dissolve the Hub
- Delete `RewardScreen`, `SpiritStoneScreen`, `UserTitleScreen` (+ ViewModels + Specs).
- Delete the money/NFT badge machinery: `BadgeStoreScreen`, `AdminBadgeVerificationScreen`,
  `BadgeManagementScreen` (+ their ViewModels + Specs).
- Delete the global review firehose: `AllReviewsScreen` (+ `AllReviewsViewModel`,
  `AllReviewsState`, `AllReviewsScreenSpec`).
- Delete `CommunityHubScreen` (+ `CommunityHubScreenSpec`) entirely; re-point its launcher to
  **Discover**. Fold Reading Buddy + My Quotes into Profile (keep their data/use-cases).
- Remove all DI registrations, nav routes, `CommonNavHost` entries for the above. Move
  Glossary / Community Source / User Sources / Legado Sources / Feature Store / Plugin Repos /
  Developer Portal / Admin User-Management to **Settings** (§Information Architecture).

### Phase 5 — Profile redesign (local-first)
- Cover/avatar/level header, stats + XP bar + rank, achievement showcase, active title,
  favorite-books showcase (+Share), reading-activity feed (no gift rows), follow counts,
  public comments, Discord CTA card.
- **Local-first:** render stats/level/XP/achievement-progress from local `readingStatistics`
  when signed-out; gate social/cloud controls behind a "Join the community" CTA
  (§Signed-out behavior).
- Fold in **Reading Buddy** (companion + daily quote widget) and **My Quotes** (Quotes tab).
- **Badge showcase + "edit showcase"** inline (reuses `SetPrimaryBadgeUseCase` /
  `SetFeaturedBadgesUseCase` — replaces the deleted BadgeManagementScreen).
- **"Customize" bottom-sheet** entry: browse/equip cosmetics (badges, frames, themes, titles)
  bought with **Spirit Stones** (`spend_stones`, cosmetic-only). Replaces the deleted BadgeStore.
  Artwork from [`image-prompts.md`](./image-prompts.md); emoji fallback until URLs are set.

### Phase 6 — Edit Profile + Discord linking
- Edit modal (cover/avatar/bio/theme/visibility). **Link Discord via Supabase Auth**; show
  handle/avatar; grant "Verified Reader" through the evaluator.

### Phase 7 — Discover home (new container)
- New top-level **Discover** destination replacing the Hub; hosts Community News card, the
  Popular-Books storefront, leaderboard entry, top reviews, character-art gallery + Discord link.
- Public/browsable signed-out; write actions prompt sign-in.

### Phase 8 — Leaderboard UI rebuild (from scratch)
- New "Hall of Readers": podium hero, tier ladder, period/metric switch, ▲▼ movement from
  `leaderboard_snapshots`, sticky "YOU" chase bar. No reuse of the current layout.

### Phase 9 — Popular Books UI rebuild (from scratch)
- New storefront inside Discover: featured carousel, genre chips, Trending/Rising rails, numbered
  Top-10, New & Noteworthy grid, NSFW blur, Power-Stone vote + Discord discuss on detail.

### Phase 10 — Other-user profile
- Public view, follow/unfollow, activity, comments. No author-specific paths.

### Phase 11 — Daily check-in
- Modal, streak tracking, reward via `checkin_daily` RPC, optional Discord streak post.

### Phase 12 — Book voting + Reviews as a discovery signal
- Free daily Power-Stone vote (`vote_book`) feeding Trending.
- Keep star+text reviews on existing `book_reviews` (book detail + in-reader) with "Helpful"
  voting and the top-reviewer badge. **No author response.**
- Redistribute reviews (no global firehose): followed-reader reviews → social/Following feed;
  aggregate ratings + "what readers say" → Discover cards/Top-10; "Top Reviews this week"
  (by `helpful_count`) curated module with Share-to-Discord.

### Phase 13 — Reading activity feed + Community News
- Activity types: reading, review, vote, achievement (no gift). Privacy toggles.
- Admin-authored Community News card from `community_announcements`.

### Phase 14 — Discord deepening
- Live presence widget in Discover + profile (§2); Share-to-Discord buttons everywhere (§3);
  channel deep-links (§4). Optional desktop Rich Presence (§7).

### Phase 15 — Testing & polish
- E2E, performance, skeleton/empty states, animations. **Audit: no money path, no author
  feature, no server beyond Supabase, and every account-optional stat renders signed-out.**
