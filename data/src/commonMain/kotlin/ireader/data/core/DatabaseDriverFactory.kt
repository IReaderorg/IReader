package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.Book
import data.Catalog
import data.Chapter
import data.ChapterHealth
import data.Character
import data.Character_appearance
import data.Character_group
import data.Character_note
import data.Character_relationship
import data.Character_timeline_event
import data.Content_filter
import data.Daily_reading_stats
import data.Download
import data.Global_glossary
import data.Glossary
import data.NftWallets
import data.Plugin
import data.Plugin_analytics_event
import data.Plugin_cache
import data.Plugin_collection
import data.Plugin_crash_report
import data.Plugin_permission
import data.Plugin_pipeline
import data.Plugin_purchase
import data.Plugin_repository
import data.Plugin_review
import data.Plugin_sync_change
import data.Plugin_sync_conflict
import data.Plugin_trial
import data.User_source
import data.Reader_theme
import data.Reading_goal
import data.Reading_milestone
import data.Reading_session
import data.SourceComparison
import data.Theme
import data.Translated_chapter
import ir.kazemcodes.infinityreader.Database
import ireader.data.book.bookGenresConverter
import ireader.data.book.booleanIntAdapter
import ireader.data.book.floatDoubleColumnAdapter
import ireader.data.book.intLongColumnAdapter
import ireader.data.book.longConverter
import ireader.data.chapter.chapterContentConvertor

fun createDatabase(driver: SqlDriver): Database {
    // Create the database instance with appropriate adapters
    val database = Database(
        driver = driver,
        bookAdapter = Book.Adapter(
            genreAdapter = bookGenresConverter,
            cover_last_modifiedAdapter = longConverter,
            date_addedAdapter = longConverter
        ),
        chapterAdapter = Chapter.Adapter(
            contentAdapter = chapterContentConvertor,
            date_fetchAdapter = longConverter,
            date_uploadAdapter = longConverter,
            typeAdapter = longConverter,
            chapter_numberAdapter = floatDoubleColumnAdapter
        ),
        reader_themeAdapter = Reader_theme.Adapter(
            background_colorAdapter = intLongColumnAdapter,
            on_textcolorAdapter = intLongColumnAdapter
        ),
        catalogAdapter = Catalog.Adapter(versionCodeAdapter = intLongColumnAdapter),
        themeAdapter = Theme.Adapter(
            errorContainerAdapter = intLongColumnAdapter,
            inversePrimaryAdapter = intLongColumnAdapter,
            inverseSurfaceAdapter = intLongColumnAdapter,
            outlineVariantAdapter = intLongColumnAdapter,
            surfaceVariantAdapter = intLongColumnAdapter,
            onBackgroundAdapter = intLongColumnAdapter,
            onPrimaryAdapter = intLongColumnAdapter,
            onSurfaceAdapter = intLongColumnAdapter,
            secondaryAdapter = intLongColumnAdapter,
            backgroundAdapter = intLongColumnAdapter,
            onTertiaryAdapter = intLongColumnAdapter,
            onSecondaryAdapter = intLongColumnAdapter,
            surfaceTintAdapter = intLongColumnAdapter,
            surfaceAdapter = intLongColumnAdapter,
            onErrorAdapter = intLongColumnAdapter,
            outlineAdapter = intLongColumnAdapter,
            primaryAdapter = intLongColumnAdapter,
            errorAdapter = intLongColumnAdapter,
            scrimAdapter = intLongColumnAdapter,
            onBarsAdapter = intLongColumnAdapter,
            barsAdapter = intLongColumnAdapter,
            tertiaryAdapter = intLongColumnAdapter,
            onErrorContainerAdapter = intLongColumnAdapter,
            primaryContainerAdapter = intLongColumnAdapter,
            inverseOnSurfaceAdapter = intLongColumnAdapter,
            onSurfaceVariantAdapter = intLongColumnAdapter,
            tertiaryContainerAdapter = intLongColumnAdapter,
            onTertiaryContainerAdapter = intLongColumnAdapter,
            onPrimaryContainerAdapter = intLongColumnAdapter,
            secondaryContainerAdapter = intLongColumnAdapter,
            onSecondaryContainerAdapter = intLongColumnAdapter
        ),
        downloadAdapter = Download.Adapter(
            priorityAdapter = intLongColumnAdapter
        ),
        glossaryAdapter = Glossary.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        translated_chapterAdapter = Translated_chapter.Adapter(
            translated_contentAdapter = chapterContentConvertor,
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter,
        ),
        chapterHealthAdapter = ChapterHealth.Adapter(
            longConverter
        ),
        sourceComparisonAdapter = SourceComparison.Adapter(
            longConverter,
            longConverter
        ),
        nftWalletsAdapter = NftWallets.Adapter(
            longConverter,
        ),
        pluginAdapter = Plugin.Adapter(
            install_dateAdapter = longConverter
        ),
        plugin_purchaseAdapter = Plugin_purchase.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_reviewAdapter = Plugin_review.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_trialAdapter = Plugin_trial.Adapter(
            start_dateAdapter = longConverter,
            expiration_dateAdapter = longConverter
        ),
        global_glossaryAdapter = Global_glossary.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        plugin_permissionAdapter = Plugin_permission.Adapter(
            granted_atAdapter = longConverter
        ),
        plugin_repositoryAdapter = Plugin_repository.Adapter(
            last_updatedAdapter = longConverter,
            created_atAdapter = longConverter
        ),
        // Character tables
        characterAdapter = Character.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        character_appearanceAdapter = Character_appearance.Adapter(
            book_idAdapter = longConverter,
            chapter_idAdapter = longConverter,
            timestampAdapter = longConverter
        ),
        character_groupAdapter = Character_group.Adapter(
            created_atAdapter = longConverter
        ),
        character_noteAdapter = Character_note.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        character_relationshipAdapter = Character_relationship.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        character_timeline_eventAdapter = Character_timeline_event.Adapter(
            book_idAdapter = longConverter,
            chapter_idAdapter = longConverter,
            timestampAdapter = longConverter
        ),
        // Reading analytics tables
        daily_reading_statsAdapter = Daily_reading_stats.Adapter(
            total_reading_time_msAdapter = longConverter,
            longest_session_msAdapter = longConverter
        ),
        reading_goalAdapter = Reading_goal.Adapter(
            start_dateAdapter = longConverter
        ),
        reading_milestoneAdapter = Reading_milestone.Adapter(
            value_Adapter = longConverter,
            reached_dateAdapter = longConverter
        ),
        reading_sessionAdapter = Reading_session.Adapter(
            book_idAdapter = longConverter,
            start_timeAdapter = longConverter,
            start_chapter_idAdapter = longConverter,
            pause_duration_msAdapter = longConverter
        ),
        // Plugin analytics tables
        plugin_analytics_eventAdapter = Plugin_analytics_event.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_crash_reportAdapter = Plugin_crash_report.Adapter(
            timestampAdapter = longConverter
        ),
        // Plugin cache table
        plugin_cacheAdapter = Plugin_cache.Adapter(
            cached_atAdapter = longConverter,
            file_sizeAdapter = longConverter
        ),
        // Plugin collection table
        plugin_collectionAdapter = Plugin_collection.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        // Plugin pipeline table
        plugin_pipelineAdapter = Plugin_pipeline.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        // Plugin sync tables
        plugin_sync_changeAdapter = Plugin_sync_change.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_sync_conflictAdapter = Plugin_sync_conflict.Adapter(
            local_timestampAdapter = longConverter,
            remote_timestampAdapter = longConverter
        ),
        // User source table
        user_sourceAdapter = User_source.Adapter(
            source_typeAdapter = intLongColumnAdapter,
            custom_orderAdapter = intLongColumnAdapter
        ),
        // Content filter table
        content_filterAdapter = Content_filter.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
    )

    return database
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
