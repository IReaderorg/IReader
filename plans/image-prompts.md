# Image Generation Prompts — Badges, Achievements & Cosmetics

Generate these, upload to **Supabase Storage** (buckets below), then set the matching
`image_url` / `icon` column. Until a URL exists, the app falls back to the emoji listed per item.

**Storage layout**
```
badges/        achievements/        frames/        themes/
```
Filenames below are the suggested object keys (match them to row IDs in
`achievement_definitions.id` / `badges.id`).

---

## 0. Global Art Direction (prepend to EVERY prompt)

> **BASE STYLE:** A single centered game-UI emblem icon on a fully transparent background.
> Webnovel / xianxia fantasy aesthetic: ornate metal medallion with glowing energy accents,
> semi-realistic painterly mobile-game art, soft inner glow, subtle floating spark particles,
> clean bold silhouette that stays readable at 64px, crisp anti-aliased edges, dramatic studio
> rim-lighting, front view, perfectly symmetrical. **No text, no letters, no numbers, no
> watermark.** Square 1:1, 1024×1024, PNG with alpha.

**RARITY MODIFIER (append one, picks the frame material + aura):**
- `BRONZE` → weathered bronze & copper, warm brown patina, modest amber glow, simple rope border.
- `SILVER` → polished silver & steel, engraved filigree, cool white-blue glow.
- `GOLD` → radiant gold with baroque filigree, warm golden halo, a few small inset gems.
- `PLATINUM` → platinum with cyan diamond inlays, prismatic shimmer, elegant thin spires.
- `LEGENDARY` → iridescent mythril alloy, intense multicolor aura, energy flames, floating
  glowing runes, dramatic god-rays behind the emblem.

**Tier → rarity mapping for the achievement ladders** (lowest→highest threshold):
`BRONZE, BRONZE, SILVER, GOLD, PLATINUM, LEGENDARY` (use as many as the ladder has tiers).

---

## 1. Achievement Emblems

Compose each as: `BASE STYLE` + the **motif** below + `RARITY MODIFIER`.
Keep the motif identical across a ladder so only the material/aura escalates by tier.

| Ladder (emoji fallback) | Motif phrase to insert | File key pattern |
|---|---|---|
| ⏱ Reading Time | "an ornate hourglass with luminous flowing sand, faint clock runes orbiting it" | `achievements/reading_time_{tier}.png` |
| 📖 Chapters | "an open glowing spellbook with pages turning into light motes" | `achievements/chapters_{tier}.png` |
| 📚 Books Completed | "a neat stack of leather tomes crowned with a glowing ribbon bookmark" | `achievements/books_{tier}.png` |
| 🔥 Streak | "a stylized soaring flame curl forming an upward spiral" | `achievements/streak_{tier}.png` |
| 📅 Check-in | "a calendar tablet with a radiant check-mark and a small rising sun" | `achievements/checkin_{tier}.png` |
| ⭐ Reviews Written | "a crossed quill pen and a five-point star dripping ink-light" | `achievements/reviews_{tier}.png` |
| 👍 Helpful Votes | "a glowing thumbs-up gesture encircled by a laurel wreath" | `achievements/helpful_{tier}.png` |
| 🗳 Power-Stone Votes | "a faceted floating power gemstone radiating energy beams" | `achievements/votes_{tier}.png` |
| 🧭 Genres Explored | "an antique compass laid over an unfurled treasure map of realms" | `achievements/genres_{tier}.png` |
| ⚡ Reading Speed | "a lightning bolt streaking across an open book leaving motion trails" | `achievements/speed_{tier}.png` |
| 👥 Social / Followers | "three abstract glowing reader silhouettes linked by a constellation line" | `achievements/social_{tier}.png` |
| 🌟 Special / Seasonal | "a floating mythic relic crown wreathed in sakura petals and embers" | `achievements/special_{tier}.png` |

**Example fully-assembled prompt** (`achievements/streak_gold.png`):
> A single centered game-UI emblem icon on a fully transparent background. Webnovel / xianxia
> fantasy aesthetic: ornate metal medallion with glowing energy accents, semi-realistic painterly
> mobile-game art, soft inner glow, subtle floating spark particles, clean bold silhouette
> readable at 64px, crisp edges, dramatic rim-lighting, front view, symmetrical. No text, no
> letters, no numbers. Square 1:1, 1024×1024, PNG alpha. Motif: a stylized soaring flame curl
> forming an upward spiral. RARITY: radiant gold with baroque filigree, warm golden halo, small
> inset gems.

---

## 2. Discord "Verified Reader" Badge (single)
> `BASE STYLE` + "a heraldic shield emblem with a bold check-mark at its center, two crossed open
> books behind it, a small headset/controller silhouette woven into the crest (generic, NOT the
> Discord logo)" + `GOLD`.
> File: `badges/verified_reader.png` · emoji fallback 🎮

---

## 3. Cosmetic Badges (bought with Spirit Stones — `badges.type = 'COSMETIC'`)
One prompt each; all share `BASE STYLE`. Offer in several rarities by swapping the modifier.

| Cosmetic badge | Motif phrase | File key |
|---|---|---|
| Bookworm | "a cute coiled dragon-worm reading a tiny glowing book" | `badges/cosmetic_bookworm.png` |
| Night Owl | "a stylized owl perched on a crescent moon holding a lantern" | `badges/cosmetic_night_owl.png` |
| Lore Keeper | "an ancient locked grimoire with a glowing keyhole eye" | `badges/cosmetic_lore_keeper.png` |
| Cultivator | "a meditating figure silhouette ringed by orbiting qi stones" | `badges/cosmetic_cultivator.png` |
| Sword Saint | "a glowing jian sword piercing an open book, petals swirling" | `badges/cosmetic_sword_saint.png` |
| Star Reader | "a shooting star trailing pages across a night sky" | `badges/cosmetic_star_reader.png` |

---

## 4. Avatar Frames (cosmetic rings around the profile avatar)
Different framing — produce as a **ring/border with empty transparent center**.

> **FRAME STYLE:** An ornate circular avatar frame ring, top-down front view, with a fully
> transparent EMPTY center (no face, no portrait), thick decorative border only, webnovel/xianxia
> fantasy game-UI style, glowing accents, symmetrical, crisp edges, no text. 1024×1024 PNG alpha.

| Frame | Add to FRAME STYLE | File key |
|---|---|---|
| Jade Vine | "carved green jade with golden leaves winding around the ring" | `frames/jade_vine.png` |
| Phoenix Flame | "wreathed in stylized phoenix fire feathers, warm orange-gold" | `frames/phoenix_flame.png` |
| Frost Crown | "icy crystalline spires and snowflakes, cool blue glow" | `frames/frost_crown.png` |
| Dragon Coil | "a sinuous Eastern dragon coiling fully around the ring, gold scales" | `frames/dragon_coil.png` |
| Starlit | "a thin elegant ring of constellations and drifting stardust, violet" | `frames/starlit.png` |
| Lotus Throne | "blooming lotus petals around the lower ring, serene pink-gold" | `frames/lotus_throne.png` |

---

## 5. Profile Themes / Cover Banners (wide background art)
Different aspect — wide, atmospheric, low-contrast so text overlays stay legible.

> **BANNER STYLE:** A wide atmospheric fantasy banner background, webnovel cover-art style,
> painterly, cinematic depth, soft focus and gentle dark gradient toward the bottom third for
> text legibility, NO characters' faces in focus, NO text. 16:6 ratio, 1920×720, PNG/JPG.

| Theme | Scene prompt | File key |
|---|---|---|
| Default Dawn | "misty mountain peaks at sunrise with floating sect pavilions" | `themes/dawn.jpg` |
| Ink Wash | "minimal black ink-wash bamboo forest, lots of negative space" | `themes/ink_wash.jpg` |
| Starfall Night | "a vast starfield over a quiet cliff library, aurora glow" | `themes/starfall.jpg` |
| Cherry Sect | "sakura courtyard with drifting petals and stone lanterns at dusk" | `themes/cherry_sect.jpg` |
| Abyss Depths | "glowing bioluminescent underwater ruins, deep teal" | `themes/abyss.jpg` |
| Golden Empire | "opulent golden palace hall with hanging scrolls, warm light" | `themes/golden_empire.jpg` |

---

## 6. Production notes
- Keep one **seed/style reference** across a set so the family looks cohesive.
- Export achievement/badge emblems with **trimmed transparent padding** (~8%).
- Provide @1x/@2x or a single 1024 master the app downsamples.
- After upload, set `achievement_definitions.icon` / `badges.image_url` to the public Storage URL;
  leaving them null keeps the emoji fallback so the app never shows a broken image.
