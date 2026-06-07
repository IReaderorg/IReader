# Spirit Stone Feature

## Overview
Spirit Stones are the in-app virtual currency. Users earn them by reading and spend them in the shop to buy cosmetic items like profile badges, frames, backgrounds, and titles.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│               SpiritStoneScreen                      │
│  (Composable UI - observes SpiritStoneViewModel)     │
├─────────────────────────────────────────────────────┤
│              SpiritStoneViewModel                    │
│  (State: SpiritStoneScreenState)                    │
│  - balance, totalEarned, totalSpent                 │
│  - shopItems: List<ShopItem>                       │
│  - ownedItemIds: Set<String>                       │
├─────────────────────────────────────────────────────┤
│            EarnSpiritStonesUseCase                   │
│  (Domain logic - calculates earning)                │
│  - 1 stone per 30 min reading                      │
│  - 5 stones per book completed                     │
│  - 10 stones per 7-day streak                      │
│  - 3 stones per chapter read                       │
│  - 1 stone per daily login                         │
└─────────────────────────────────────────────────────┘
```

## Files

| File | Purpose |
|------|---------|
| `SpiritStoneScreen.kt` | UI showing balance card and shop items |
| `SpiritStoneViewModel.kt` | State management for balance and shop |
| `SpiritStoneScreenSpec.kt` | Navigation wiring |
| `README.md` | This documentation |

## Earning Rates

| Action | Spirit Stones |
|--------|--------------|
| 30 minutes reading | 1 |
| Book completed | 5 |
| 7-day streak milestone | 10 |
| Chapter read | 3 |
| Daily login | 1 |

## Shop Items

| Item | Price | Type |
|------|-------|------|
| Profile Badge | 50 | PROFILE_BADGE |
| Profile Frame | 100 | PROFILE_FRAME |
| Profile Background | 150 | PROFILE_BACKGROUND |
| Profile Title | 200 | PROFILE_TITLE |
| Special Badge | 300 | SPECIAL_BADGE |
| Animated Effect | 500 | ANIMATED_EFFECT |

## Data Flow

```
User reads → EarnSpiritStonesUseCase
    ↓
Update SpiritStoneBalance (balance += earned)
    ↓
User purchases item → balance -= item.price
    ↓
Add to UserInventory (ownedItems)
    ↓
SpiritStoneViewModel updates state → UI re-renders
```

## Reusable Components Used
- `FeatureScreenScaffold` - Common top bar with back navigation
