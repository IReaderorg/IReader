# Community Source

The Community Source is a feature that allows users to share and read community-translated novels. It connects to a Supabase backend where users can contribute their translations and browse content translated by others.

## Features

- **Browse Community Content**: Discover novels translated by the community
- **Multi-language Support**: 20+ languages supported including English, Spanish, French, German, Japanese, Korean, Chinese, and more
- **Search & Filter**: Search by title, author, language, genre, and status
- **Translation Sharing**: Share your translations with the community after completing them
- **Rating System**: Rate translations to help others find quality content
- **Contributor Recognition**: Display contributor badges and track contributions

## Setup

### 1. Database Setup

Run the migration script in your Supabase SQL Editor:

```sql
-- Run this file in Supabase SQL Editor
supabase/migration_community_source.sql
```

This creates the following tables:
- `community_books` - Stores book metadata
- `community_chapters` - Stores translated chapter content
- `chapter_reports` - Stores reports for problematic content
- `chapter_ratings` - Stores user ratings for translations

### 2. App Configuration

1. Go to **Settings > Supabase Configuration**
2. Configure the Community Source URL and API key:
   - You can use the same Supabase project as other features
   - Or configure a separate project for community content
3. Enable the Community Source in **Settings > Community Source**

### 3. Optional: Custom Supabase Instance

If you want to host your own community content:

1. Create a new Supabase project
2. Run the migration script
3. Configure the custom URL in the app settings

## Usage

### Browsing Content

1. Open the **Sources** tab
2. Select **Community Source**
3. Browse by:
   - **Latest**: Recently added books
   - **Popular**: Most viewed books
   - **Recently Translated**: Books with recent translations

### Searching

Use the search bar to find books by:
- Title
- Author

Use filters to narrow results by:
- Language
- Genre
- Status (Ongoing, Completed, Hiatus, Dropped)

### Contributing Translations

1. Enable **Auto-share Translations** in Community Source settings
2. Set your **Contributor Name**
3. When you complete a translation, it will be automatically shared

### Rating Translations

1. Open a chapter from the Community Source
2. After reading, rate the translation (1-5 stars)
3. Your rating helps others find quality translations

## API Reference

### Endpoints

The Community Source uses Supabase REST API:

```
GET /rest/v1/community_books
GET /rest/v1/community_chapters
POST /rest/v1/community_books
POST /rest/v1/community_chapters
POST /rest/v1/rpc/rate_chapter
```

### Data Models

#### CommunityBook
```kotlin
data class CommunityBook(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val cover: String,
    val genres: List<String>,
    val status: String,
    val originalLanguage: String,
    val availableLanguages: List<String>,
    val contributorId: String,
    val contributorName: String,
    val viewCount: Long,
    val chapterCount: Int,
    val lastUpdated: Long,
    val createdAt: Long
)
```

#### CommunityChapter
```kotlin
data class CommunityChapter(
    val id: String,
    val bookId: String,
    val name: String,
    val number: Float,
    val content: String,
    val language: String,
    val translatorId: String,
    val translatorName: String,
    val originalChapterKey: String,
    val rating: Float,
    val ratingCount: Int,
    val viewCount: Long,
    val createdAt: Long,
    val updatedAt: Long
)
```

## Supported Languages

| Code | Language |
|------|----------|
| en | English |
| es | Spanish |
| pt | Portuguese |
| fr | French |
| de | German |
| it | Italian |
| ru | Russian |
| ja | Japanese |
| ko | Korean |
| zh | Chinese |
| ar | Arabic |
| hi | Hindi |
| id | Indonesian |
| th | Thai |
| vi | Vietnamese |
| tr | Turkish |
| pl | Polish |
| nl | Dutch |
| fil | Filipino |

## Architecture

```
domain/src/commonMain/kotlin/ireader/domain/community/
├── CommunitySource.kt          # CatalogSource implementation
├── CommunityRepository.kt      # Repository interface
├── CommunityRepositoryImpl.kt  # Supabase implementation
├── CommunityPreferences.kt     # User preferences
└── SubmitTranslationUseCase.kt # Use case for submissions

domain/src/commonMain/kotlin/ireader/domain/models/entities/
└── CommunityCatalog.kt         # Catalog entry for Community Source

presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/community/
├── CommunitySourceConfigScreen.kt    # Configuration UI
└── CommunitySourceConfigViewModel.kt # ViewModel

supabase/
└── migration_community_source.sql    # Database migration
```

## Security

- Row Level Security (RLS) is enabled on all tables
- Users can only modify their own contributions
- Content moderation through reporting system
- Admin approval workflow for sensitive content

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew :domain:test`
5. Submit a pull request

## License

This feature is part of IReader and is licensed under the same terms.
