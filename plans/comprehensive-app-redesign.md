# Comprehensive App Redesign Plan

## Overview

This plan covers 6 major initiatives to transform IReader into a gamified, engaging reading app with clean architecture. Each initiative is designed to be independently executable.

---

## Initiative 1: Remove All NFT-Related Code

### Scope
137 files reference NFT. Complete removal of all NFT-related code, database tables, and features.

### Files to Delete

**Domain Layer:**
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/NFTRepository.kt`
- `domain/src/commonMain/kotlin/ireader/domain/models/remote/NFTWallet.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/nft/SaveWalletAddressUseCase.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/nft/VerifyNFTOwnershipUseCase.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/nft/GetNFTVerificationStatusUseCase.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/nft/GetNFTMarketplaceUrlUseCase.kt`
- `domain/src/commonMain/kotlin/ireader/domain/models/badge/BadgeError.kt` (NFT-related parts)

**Data Layer:**
- `data/src/commonMain/kotlin/ireader/data/nft/NFTRepositoryImpl.kt`
- `data/src/commonMain/kotlin/ireader/data/repository/NoOpNFTRepository.kt`

**Presentation Layer:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/nft/NFTBadgeScreen.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/nft/NFTBadgeViewModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NFTBadgeScreenSpec.kt`

### Files to Modify

| File | Change |
|------|--------|
| `data/src/commonMain/kotlin/ireader/data/di/ReviewModule.kt` | Remove NFT repository DI bindings and use case factories |
| `domain/src/commonMain/kotlin/ireader/domain/di/UseCasesInject.kt` | Remove NFT use case bindings |
| `presentation/src/commonMain/kotlin/ireader/presentation/core/di/PresentationModules.kt` | Remove NFTBadgeViewModel factory |
| `presentation/src/commonMain/kotlin/ireader/presentation/core/CommonNavHost.kt` | Remove NFT badge composable route |
| `presentation/src/commonMain/kotlin/ireader/presentation/core/NavigationRoutes.kt` | Remove `nftBadge` route constant |
| `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/CommunityHubScreenSpec.kt` | Remove `onNFTBadge` callback |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/CommunityHubScreen.kt` | Remove NFT badge settings item |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/store/BadgeStoreViewModel.kt` | Remove NFT badge sorting logic |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementViewModel.kt` | Remove NFT_EXCLUSIVE badge type handling |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementScreen.kt` | Remove NFT animation for badges |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/badges/BadgeIcon.kt` | Remove NFT glow effect |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/BadgeErrorMapper.kt` | Remove NFT retry config |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/sync/SupabaseConfigScreen.kt` | Remove NFT from project 6 description |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/leaderboard/LeaderboardScreen.kt` | Remove "nft" badge type from when expression |
| `presentation/src/commonMain/kotlin/ireader/presentation/ui/leaderboard/ReadingLeaderboardContent.kt` | Remove "nft" badge type from when expression |
| `data/src/commonMain/kotlin/ireader/data/badge/BadgeRepositoryImpl.kt` | Remove NFT_EXCLUSIVE badge type |
| `data/src/commonMain/kotlin/ireader/data/admin/AdminUserRepositoryImpl.kt` | Remove NFT_EXCLUSIVE badge type |
| `data/src/commonMain/kotlin/ireader/data/core/DatabaseDriverFactory.kt` | Remove NftWallets adapter |
| `data/src/commonMain/kotlin/data/DatabaseMigrations.kt` | Remove nftWallets table creation (migration 16→17) |
| `data/src/commonMain/kotlin/ireader/data/backend/MultiProjectBackendService.kt` | Remove nft_wallets from routing |
| `data/src/commonMain/kotlin/ireader/data/remote/MultiSupabaseClientProvider.kt` | Remove nft_wallets from docs |
| `supabase/split/MultiSupabaseClient.kt` | Remove nft_wallets from docs |
| `domain/src/commonMain/kotlin/ireader/domain/models/remote/Badge.kt` | Remove NFT_EXCLUSIVE from BadgeType enum, remove price field |
| `domain/src/commonMain/kotlin/ireader/domain/usecases/badge/GetAvailableBadgesUseCase.kt` | Remove NFT_EXCLUSIVE filter |

### Database Migration
- Create migration to drop `nftWallets` table
- Remove `nft_wallets` from Supabase Project 6

### Tests to Write
- Verify NFT repository methods return appropriate errors after removal
- Verify badge store no longer shows NFT badges

---

## Initiative 2: Leaderboard UI Redesign with Level System

### Goals
- Replace raw "hours" display with a game-like level system
- Show user achievements when tapping a leaderboard entry
- Create a sense of progression and reward
- Make the UI feel engaging and game-like

### Level System Design

```
Level = floor(total_reading_time_minutes / 60) + 1
XP = total_reading_time_minutes % 60 (progress to next level)
XP needed for next level = 60 minutes

Example:
- 0-59 min   → Level 1  (0/60 XP)
- 60-119 min → Level 2  (0/60 XP)
- 120-179 min → Level 3 (0/60 XP)
- 300 min    → Level 6  (0/60 XP)
```

### New Domain Models

```kotlin
// domain/.../models/entities/ReaderLevel.kt
data class ReaderLevel(
    val level: Int,
    val currentXp: Long,        // minutes into current level
    val xpToNextLevel: Long,     // always 60
    val totalMinutes: Long,
    val title: String           // e.g., "Novice Reader", "Bookworm", "Master Reader"
)

// domain/.../models/entities/UserAchievement.kt
data class UserAchievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val earnedAt: Long,
    val category: AchievementCategory
)

enum class AchievementCategory {
    READING_TIME, CHAPTERS, BOOKS, STREAK, SPECIAL
}
```

### Level Titles

| Level Range | Title |
|-------------|-------|
| 1-5 | Novice Reader |
| 6-15 | Curious Reader |
| 16-30 | Avid Reader |
| 31-50 | Bookworm |
| 51-100 | Master Reader |
| 101-200 | Literary Legend |
| 200+ | Reading Deity |

### Files to Create

| File | Purpose |
|------|---------|
| `domain/.../models/entities/ReaderLevel.kt` | Level data model |
| `domain/.../models/entities/UserAchievement.kt` | Achievement data model |
| `domain/.../usecases/level/CalculateLevelUseCase.kt` | Calculate level from reading time |
| `domain/.../usecases/achievement/GetUserAchievementsUseCase.kt` | Fetch user achievements |
| `presentation/.../ui/leaderboard/LevelDisplay.kt` | Level badge composable |
| `presentation/.../ui/leaderboard/UserAchievementDialog.kt` | Achievement list dialog |
| `presentation/.../ui/leaderboard/LeaderboardEntryCard.kt` | Redesigned entry card |

### Files to Modify

| File | Change |
|------|--------|
| `presentation/.../ui/leaderboard/LeaderboardScreen.kt` | Redesign with level display, achievement tap |
| `presentation/.../ui/leaderboard/LeaderboardViewModel.kt` | Add achievement loading |
| `presentation/.../ui/leaderboard/LeaderboardState.kt` | Add level and achievement state |
| `presentation/.../ui/leaderboard/ReadingLeaderboardContent.kt` | Replace hours with level display |
| `domain/.../models/entities/LeaderboardEntry.kt` | Add level field |
| `data/.../statistics/StatisticsSyncService.kt` | Add level calculation to LeaderboardEntry |

### Leaderboard UI Changes
- Show level badge instead of raw hours
- Show XP progress bar (circular or linear)
- Show level title below username
- Tap on user → show achievement dialog
- Achievement dialog shows: icon, name, description, earned date
- Color-coded levels (bronze → silver → gold → diamond)

### Tests to Write
- `CalculateLevelUseCaseTest` — verify level calculation at boundaries
- `LeaderboardViewModelTest` — verify level display in state
- `UserAchievementDialogTest` — verify achievement rendering

---

## Initiative 3: Reward-Based System (Gamification)

### Goals
- Create a unified reward system for the whole app
- Tie rewards to reading activity
- Make users feel progression and achievement
- Reuse existing badge/achievement infrastructure

### Architecture

```
┌─────────────────────────────────────────────┐
│              RewardEngine                    │
│  (calculates XP, levels, achievements)       │
├─────────────────────────────────────────────┤
│  ReadingActivity → XP → Level Up → Rewards  │
│  ChaptersRead   → XP → Achievements         │
│  BooksCompleted → XP → Special Badges       │
│  Streak         → XP → Streak Rewards       │
└─────────────────────────────────────────────┘
```

### New Domain Models

```kotlin
// domain/.../models/entities/Reward.kt
data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val type: RewardType,
    val icon: String,
    val earnedAt: Long,
    val xpValue: Int
)

enum class RewardType {
    BADGE, ACHIEVEMENT, LEVEL_UP, STREAK, MILESTONE
}

// domain/.../models/entities/XpEvent.kt
data class XpEvent(
    val source: XpSource,
    val amount: Int,
    val timestamp: Long
)

enum class XpSource {
    READING_TIME,      // 1 XP per minute
    CHAPTER_READ,      // 5 XP per chapter
    BOOK_COMPLETED,    // 50 XP per book
    STREAK_MILESTONE,  // 10 XP per streak day
    DAILY_LOGIN        // 5 XP per day
}
```

### XP Rules

| Action | XP |
|--------|-----|
| 1 minute reading | 1 XP |
| 1 chapter read | 5 XP |
| 1 book completed | 50 XP |
| 7-day streak | 100 XP |
| 30-day streak | 500 XP |

### Files to Create

| File | Purpose |
|------|---------|
| `domain/.../models/entities/Reward.kt` | Reward model |
| `domain/.../models/entities/XpEvent.kt` | XP event model |
| `domain/.../usecases/reward/RewardEngineUseCase.kt` | Core reward calculation |
| `domain/.../usecases/reward/GetUserRewardsUseCase.kt` | Fetch all user rewards |
| `domain/.../usecases/reward/CheckNewAchievementsUseCase.kt` | Check and award new achievements |
| `domain/.../data/repository/RewardRepository.kt` | Reward repository interface |
| `data/.../reward/RewardRepositoryImpl.kt` | Reward repository implementation |
| `data/.../repository/NoOpRewardRepository.kt` | NoOp reward repository |
| `presentation/.../ui/reward/RewardScreen.kt` | Reward/achievement screen |
| `presentation/.../ui/reward/RewardViewModel.kt` | Reward screen ViewModel |
| `presentation/.../ui/reward/RewardNotification.kt` | Level up / achievement popup |

### Files to Modify

| File | Change |
|------|--------|
| `domain/.../data/repository/ReadingStatisticsRepository.kt` | Add XP tracking methods |
| `domain/.../usecases/statistics/TrackReadingProgressUseCase.kt` | Add XP event emission |
| `data/.../di/repositoryInjectModule.kt` | Add RewardRepository DI binding |
| `domain/.../di/UseCaseModule.kt` | Add reward use case bindings |
| `presentation/.../core/NavigationRoutes.kt` | Add reward screen route |
| `presentation/.../core/CommonNavHost.kt` | Add reward screen composable |

### Tests to Write
- `RewardEngineUseCaseTest` — verify XP calculation and level ups
- `CheckNewAchievementsUseCaseTest` — verify achievement triggers
- `RewardRepositoryImplTest` — verify reward persistence

---

## Initiative 4: Popular Books Redesign

### Goals
- Show book image, description, source name in popular books
- Click on book → open in correct source
- If source not installed → prompt user to install it
- Differentiate between lnreader-X and ireader-source groups

### Enhanced PopularBook Model

```kotlin
data class PopularBook(
    val bookId: String,
    val title: String,
    val bookUrl: String,
    val sourceId: Long,
    val sourceName: String,        // NEW: human-readable source name
    val sourceGroup: SourceGroup,  // NEW: LNREADER or IREADER
    val readerCount: Int,
    val lastRead: Long,
    val coverUrl: String? = null,
    val description: String? = null,  // NEW: book description
    val localBookId: Long? = null,
    val isInLibrary: Boolean = false
)

enum class SourceGroup {
    LNREADER,    // lnreader-X sources
    IREADER      // ireader-source extensions
}
```

### Source Resolution Logic

```
User taps popular book
    → Check if sourceName matches any installed source
        → If yes: open book detail with that source
        → If no: show dialog "You need to install [sourceName]"
            → Dialog has "Install" button → navigate to source installer
            → Dialog has "Cancel" button → dismiss
```

### Files to Create

| File | Purpose |
|------|---------|
| `domain/.../usecases/popular/ResolvePopularBookSourceUseCase.kt` | Resolve source and navigate |
| `presentation/.../ui/popular/SourceInstallDialog.kt` | Dialog for missing source |

### Files to Modify

| File | Change |
|------|--------|
| `domain/.../models/remote/PopularBook.kt` | Add sourceName, sourceGroup, description |
| `data/.../popular/PopularBooksRepositoryImpl.kt` | Map new fields from Supabase |
| `presentation/.../ui/community/PopularBooksScreen.kt` | Redesign with image, description, source |
| `presentation/.../ui/community/PopularBooksViewModel.kt` | Add source resolution logic |
| `presentation/.../ui/community/PopularBooksState.kt` | Add source resolution state |

### UI Changes
- Book card shows: cover image, title, description (2 lines), source name badge, reader count
- Source badge is color-coded (blue for lnreader, green for ireader)
- Tap → source resolution flow
- Missing source → install dialog with source info

### Tests to Write
- `ResolvePopularBookSourceUseCaseTest` — verify source resolution
- `PopularBooksViewModelTest` — verify UI state with new fields

---

## Initiative 5: Source ID → Source Name Migration

### Goals
- Replace `sourceId: Long` with `sourceName: String` as the primary source identifier
- Differentiate source groups with prefix: `lnreader-X` vs `ireader-source`
- Make source lookup more intuitive
- Enable easier popular book source resolution

### Migration Strategy

This is the most deeply rooted change. The `sourceId: Long` appears in 68+ domain files and countless data/presentation files.

**Phase 1: Add sourceName alongside sourceId (backward compatible)**
- Add `sourceName: String` field to all models that have `sourceId`
- Keep `sourceId` for backward compatibility
- Populate `sourceName` from the source table lookup

**Phase 2: Migrate internal logic to use sourceName**
- Update all use cases to prefer sourceName
- Update repositories to query by sourceName
- Update sync logic to use sourceName

**Phase 3: Remove sourceId (after full migration)**
- Remove sourceId from models
- Update database schemas
- Update Supabase functions

### Source Name Convention

```
lnreader sources:  "lnreader-mangadex", "lnreader-novelupdates", ...
ireader sources:   "ireader-mangadex", "ireader-novelupdates", ...
```

### Key Files to Modify (Phase 1)

| File | Change |
|------|--------|
| `domain/.../models/entities/Book.kt` | Add `sourceName: String` field |
| `domain/.../models/remote/PopularBook.kt` | Add `sourceName: String` field |
| `domain/.../models/remote/SyncedBook.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/ExploreBook.kt` | Add `sourceName: String` field |
| `domain/.../models/download/Download.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/CatalogRemote.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/SourceHealth.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/SourceReport.kt` | Add `sourceName: String` field |
| `domain/.../models/backup/LibraryBackup.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/Update.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/SavedDownload.kt` | Add `sourceName: String` field |
| `domain/.../models/BookCover.kt` | Add `sourceName: String` field |
| `domain/.../models/sync/BookSyncData.kt` | Change sourceId from String to sourceName |
| `domain/.../models/sync/ChapterSyncData.kt` | Update globalId format |
| `domain/.../models/entities/JSPluginCatalog.kt` | Add `sourceName: String` field |
| `domain/.../models/entities/LibraryInsights.kt` | Already has sourceName, keep it |
| `domain/.../models/migration/MigrationModels.kt` | Already has sourceName, keep it |

### Database Changes
- Add `source_name` column to relevant SQLite tables
- Add `source_name` column to Supabase tables
- Create migration scripts

### Tests to Write
- Verify sourceName is correctly populated from sourceId lookup
- Verify backward compatibility (sourceId still works)
- Verify source group prefix convention

---

## Initiative 7: Spirit Stone System (Virtual Currency & Shop)

### Goals
- Introduce "Spirit Stone" as in-app virtual currency
- Users earn Spirit Stones by reading and engaging with the app
- Users spend Spirit Stones in a shop to buy cosmetic items
- Create a rewarding loop: read → earn → customize → feel proud → read more

### Spirit Stone Economy

```
Earning:
- 1 Spirit Stone per 30 minutes of reading
- 5 Spirit Stones per book completed
- 10 Spirit Stones per 7-day streak milestone
- 3 Spirit Stones per chapter read
- 1 Spirit Stone per daily login

Spending (Shop Items):
- Profile Badge: 50 Spirit Stones
- Profile Frame (circle around avatar): 100 Spirit Stones
- Profile Background/Banner: 150 Spirit Stones
- Profile Title: 200 Spirit Stones
- Special Badge: 300 Spirit Stones
- Animated Profile Effect: 500 Spirit Stones
```

### New Domain Models

```kotlin
// domain/.../models/entities/SpiritStone.kt
data class SpiritStoneBalance(
    val userId: String,
    val balance: Long,
    val totalEarned: Long,
    val totalSpent: Long,
    val lastUpdated: Long
)

data class SpiritStoneTransaction(
    val id: String,
    val userId: String,
    val amount: Long,           // positive = earned, negative = spent
    val type: TransactionType,
    val description: String,
    val timestamp: Long
)

enum class TransactionType {
    READING_REWARD,
    BOOK_COMPLETED,
    STREAK_MILESTONE,
    CHAPTER_READ,
    DAILY_LOGIN,
    SHOP_PURCHASE,
    ACHIEVEMENT_REWARD
}

// domain/.../models/entities/ShopItem.kt
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val type: ShopItemType,
    val price: Long,            // in Spirit Stones
    val icon: String,
    val previewUrl: String?,
    val isLimited: Boolean = false,
    val availableUntil: Long? = null
)

enum class ShopItemType {
    PROFILE_BADGE,
    PROFILE_FRAME,
    PROFILE_BACKGROUND,
    PROFILE_TITLE,
    SPECIAL_BADGE,
    ANIMATED_EFFECT
}

// domain/.../models/entities/UserInventory.kt
data class UserInventory(
    val userId: String,
    val ownedItems: List<OwnedItem>,
    val activeFrame: String?,       // currently equipped frame
    val activeBackground: String?,  // currently equipped background
    val activeTitle: String?        // currently equipped title
)

data class OwnedItem(
    val itemId: String,
    val item: ShopItem,
    val purchasedAt: Long,
    val isEquipped: Boolean = false
)
```

### Files to Create

| File | Purpose |
|------|---------|
| `domain/.../models/entities/SpiritStone.kt` | Currency and transaction models |
| `domain/.../models/entities/ShopItem.kt` | Shop item models |
| `domain/.../models/entities/UserInventory.kt` | User inventory model |
| `domain/.../data/repository/SpiritStoneRepository.kt` | Spirit Stone repository interface |
| `domain/.../data/repository/ShopRepository.kt` | Shop repository interface |
| `domain/.../data/repository/UserInventoryRepository.kt` | Inventory repository interface |
| `domain/.../usecases/spiritstone/EarnSpiritStonesUseCase.kt` | Award stones for activity |
| `domain/.../usecases/spiritstone/GetSpiritStoneBalanceUseCase.kt` | Get current balance |
| `domain/.../usecases/spiritstone/SpendSpiritStonesUseCase.kt` | Spend stones in shop |
| `domain/.../usecases/shop/GetShopItemsUseCase.kt` | Fetch available shop items |
| `domain/.../usecases/shop/PurchaseItemUseCase.kt` | Purchase a shop item |
| `domain/.../usecases/shop/EquipItemUseCase.kt` | Equip owned item |
| `domain/.../usecases/inventory/GetUserInventoryUseCase.kt` | Fetch user inventory |
| `data/.../spiritstone/SpiritStoneRepositoryImpl.kt` | Spirit Stone repository impl |
| `data/.../shop/ShopRepositoryImpl.kt` | Shop repository impl |
| `data/.../inventory/UserInventoryRepositoryImpl.kt` | Inventory repository impl |
| `data/.../repository/NoOpSpiritStoneRepository.kt` | NoOp Spirit Stone repository |
| `data/.../repository/NoOpShopRepository.kt` | NoOp Shop repository |
| `data/.../repository/NoOpUserInventoryRepository.kt` | NoOp Inventory repository |
| `presentation/.../ui/shop/ShopScreen.kt` | Shop UI |
| `presentation/.../ui/shop/ShopViewModel.kt` | Shop ViewModel |
| `presentation/.../ui/shop/ShopItemCard.kt` | Shop item card composable |
| `presentation/.../ui/shop/SpiritStoneDisplay.kt` | Spirit Stone balance display |
| `presentation/.../ui/profile/ProfileCustomizationScreen.kt` | Profile customization UI |
| `presentation/.../ui/profile/ProfileCustomizationViewModel.kt` | Profile customization ViewModel |

### Files to Modify

| File | Change |
|------|--------|
| `domain/.../usecases/statistics/TrackReadingProgressUseCase.kt` | Add Spirit Stone earning on reading |
| `domain/.../data/repository/ReadingStatisticsRepository.kt` | Add Spirit Stone tracking methods |
| `data/.../di/repositoryInjectModule.kt` | Add SpiritStone, Shop, Inventory DI bindings |
| `domain/.../di/UseCaseModule.kt` | Add Spirit Stone and shop use case bindings |
| `presentation/.../core/NavigationRoutes.kt` | Add shop and profile customization routes |
| `presentation/.../core/CommonNavHost.kt` | Add shop and profile composables |
| `presentation/.../ui/settings/CommunityHubScreen.kt` | Add shop entry point |
| `domain/.../models/entities/LeaderboardEntry.kt` | Add equipped frame/title for display |

### Shop UI Design
- Top bar shows Spirit Stone balance with icon
- Tab layout: Badges | Frames | Backgrounds | Titles | Effects
- Each item shows: preview, name, price, "Buy" or "Owned" button
- Equipped items show "Equipped" badge
- Purchase confirmation dialog
- Insufficient stones → "Earn more by reading!" message

### Profile Customization UI
- Preview of profile with current customizations
- Sections: Frame, Background, Title, Badge
- Tap section → show owned items for that category
- Tap item → equip it
- "Preview" button to see how it looks on leaderboard

### Tests to Write
- `EarnSpiritStonesUseCaseTest` — verify earning rates
- `SpendSpiritStonesUseCaseTest` — verify spending and insufficient balance
- `PurchaseItemUseCaseTest` — verify purchase flow
- `EquipItemUseCaseTest` — verify equip/unequip
- `SpiritStoneRepositoryImplTest` — verify transaction recording

---

## Initiative 8: User Title System

### Goals
- Each user can have an active title that appears on their profile and leaderboard
- Titles provide passive effects (e.g., 2x XP for limited time)
- Titles are earned through achievements or purchased from the shop
- Effects are limited-duration to encourage active engagement

### Title System Design

```
Title = cosmetic name + passive effect + duration

Example Titles:
- "Speed Reader" → 2x XP from reading for 24 hours
- "Bookworm" → 2x XP from chapters for 12 hours
- "Night Owl" → 3x XP during night hours (10PM-6AM) for 7 days
- "Streak Master" → 2x streak bonus for 48 hours
- "Completionist" → 3x XP from book completion for 24 hours
- "Early Bird" → 2x XP during morning hours (6AM-10AM) for 7 days
```

### New Domain Models

```kotlin
// domain/.../models/entities/UserTitle.kt
data class UserTitle(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val rarity: TitleRarity,
    val effect: TitleEffect,
    val isEarned: Boolean = false,
    val earnedAt: Long? = null
)

enum class TitleRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

sealed class TitleEffect {
    abstract val multiplier: Double
    abstract val durationHours: Long

    data class ReadingTimeBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 24
    ) : TitleEffect()

    data class ChapterBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 12
    ) : TitleEffect()

    data class BookCompletionBonus(
        override val multiplier: Double = 3.0,
        override val durationHours: Long = 24
    ) : TitleEffect()

    data class StreakBonus(
        override val multiplier: Double = 2.0,
        override val durationHours: Long = 48
    ) : TitleEffect()

    data class TimeRestrictedBonus(
        override val multiplier: Double = 3.0,
        override val durationHours: Long = 168, // 7 days
        val startHour: Int = 22,
        val endHour: Int = 6
    ) : TitleEffect()
}

data class ActiveTitle(
    val title: UserTitle,
    val activatedAt: Long,
    val expiresAt: Long,
    val remainingHours: Long
) {
    val isActive: Boolean get() = System.currentTimeMillis() < expiresAt
}
```

### Title Effects Integration

```
When user earns XP:
    → Check if user has an active title
    → If yes, apply title effect multiplier
    → If effect is time-restricted, check current hour
    → Apply bonus XP
    → Show floating "+XP (Title Bonus!)" indicator
```

### Files to Create

| File | Purpose |
|------|---------|
| `domain/.../models/entities/UserTitle.kt` | Title and effect models |
| `domain/.../data/repository/TitleRepository.kt` | Title repository interface |
| `domain/.../usecases/title/GetAvailableTitlesUseCase.kt` | Fetch all titles |
| `domain/.../usecases/title/GetActiveTitleUseCase.kt` | Get currently active title |
| `domain/.../usecases/title/ActivateTitleUseCase.kt` | Activate a title (start effect) |
| `domain/.../usecases/title/CheckTitleEffectsUseCase.kt` | Check and apply title effects to XP |
| `domain/.../usecases/title/EarnTitleUseCase.kt` | Award title for achievement |
| `data/.../title/TitleRepositoryImpl.kt` | Title repository implementation |
| `data/.../repository/NoOpTitleRepository.kt` | NoOp title repository |
| `presentation/.../ui/title/TitleSelectionScreen.kt` | Title selection UI |
| `presentation/.../ui/title/TitleSelectionViewModel.kt` | Title selection ViewModel |
| `presentation/.../ui/title/TitleCard.kt` | Title card composable |
| `presentation/.../ui/components/ActiveTitleIndicator.kt` | Shows active title with timer |

### Files to Modify

| File | Change |
|------|--------|
| `domain/.../usecases/spiritstone/EarnSpiritStonesUseCase.kt` | Integrate title effects on earning |
| `domain/.../usecases/statistics/TrackReadingProgressUseCase.kt` | Apply title multiplier to XP |
| `domain/.../usecases/reward/RewardEngineUseCase.kt` | Check title effects before awarding XP |
| `domain/.../usecases/level/CalculateLevelUseCase.kt` | Include title bonus in XP calculation |
| `data/.../di/repositoryInjectModule.kt` | Add TitleRepository DI binding |
| `domain/.../di/UseCaseModule.kt` | Add title use case bindings |
| `presentation/.../core/NavigationRoutes.kt` | Add title selection route |
| `presentation/.../core/CommonNavHost.kt` | Add title selection composable |
| `presentation/.../ui/leaderboard/LeaderboardScreen.kt` | Show active title on user entries |
| `presentation/.../ui/shop/ShopScreen.kt` | Add titles tab in shop |
| `domain/.../models/entities/LeaderboardEntry.kt` | Add activeTitle field |

### Title Selection UI
- Grid of title cards showing: icon, name, rarity color, effect description
- "Activate" button on earned titles
- "Locked" overlay on unearned titles with unlock condition
- Active title shows countdown timer: "Expires in 12h 34m"
- Rarity colors: Common=gray, Rare=blue, Epic=purple, Legendary=gold

### XP Bonus Flow

```
User reads for 10 minutes
    → Base XP = 10
    → Check active title: "Speed Reader" (2x reading XP, 5h remaining)
    → Bonus XP = 10 * 2 = 20
    → Total XP awarded = 20
    → Show floating indicator: "+20 XP (Speed Reader 2x!)"
```

### Tests to Write
- `ActivateTitleUseCaseTest` — verify activation and expiration
- `CheckTitleEffectsUseCaseTest` — verify multiplier application
- `TimeRestrictedBonusTest` — verify time window logic
- `EarnTitleUseCaseTest` — verify title earning from achievements
- `TitleRepositoryImplTest` — verify title persistence

---

## Initiative 9: Dead Code Removal

### Goals
- Remove all dead/unused code identified during the redesign
- Clean up unused imports, variables, and functions
- Simplify the codebase

### Known Dead Code Areas

1. **NFT code** (covered in Initiative 1)
2. **Unused badge types** (NFT_EXCLUSIVE removal)
3. **Unused leaderboard badge display code** (the `when` expressions for badge types)
4. **Unused Supabase project references** (nft_wallets in MultiProjectBackendService)

### Process
- After each initiative, run dead code analysis
- Remove unused imports
- Remove unused functions
- Remove unused variables
- Verify with compilation

---

## Execution Order

The initiatives should be executed in this order to minimize conflicts:

```
1. Remove NFT Code (clean slate)
2. Source ID → Source Name Migration Phase 1 (add sourceName alongside sourceId)
3. Popular Books Redesign (uses sourceName)
4. Leaderboard Level System (independent of source changes)
5. Reward System (builds on level system)
6. Spirit Stone System (adds currency layer)
7. User Title System (adds title effects, integrates with Spirit Stones)
8. Dead Code Removal (final cleanup)
9. Source ID → Source Name Migration Phase 2 & 3 (after everything uses sourceName)
```

## Architecture Principles

1. **Clean Architecture**: Domain → Data → Presentation layer separation
2. **Single Responsibility**: Each use case does one thing
3. **Testability**: All business logic is unit testable
4. **Reusability**: Shared components (level display, XP bar) are reusable
5. **Foolproof**: Clear naming, comprehensive documentation, defensive programming
6. **Backward Compatibility**: sourceId remains during migration phase

## Testing Strategy

- Every new use case gets unit tests
- Every new repository gets integration tests
- Every UI change gets screenshot/composable tests
- Database migrations get migration tests
- Reward calculation gets boundary tests
