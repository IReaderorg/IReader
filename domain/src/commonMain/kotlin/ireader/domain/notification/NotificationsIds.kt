package ireader.domain.notification

object NotificationsIds {
    /**
     * Common notification channel and ids used anywhere.
     */
    const val CHANNEL_COMMON = "common_channel"
    const val ID_DOWNLOAD_IMAGE = 2

    /**
     * Notification channel and ids used by the library updater.
     */
     const val GROUP_LIBRARY = "group_library"
    const val CHANNEL_LIBRARY_PROGRESS = "library_progress_channel"
    const val ID_LIBRARY_PROGRESS = -101
    const val CHANNEL_LIBRARY_ERROR = "library_errors_channel"
    const val ID_LIBRARY_ERROR = -102

     const val GROUP_TTS = "group_text_reader"
    const val CHANNEL_TTS = "library_text_reader_channel"
    const val ID_TTS = -601
    const val CHANNEL_TTS_ERROR = "library_text_reader_error_channel"
    const val ID_TTS_ERROR = -602

    /**
     * Notification channel and ids used by the installer.
     */
    const val GROUP_INSTALLER = "group_installer"
    const val CHANNEL_INSTALLER_PROGRESS = "installer_progress_channel"
    const val ID_INSTALLER_PROGRESS = -801
    const val CHANNEL_INSTALLER_COMPLETE = "installer_complete_channel"
    const val ID_INSTALLER_COMPLETE = -803
    const val CHANNEL_INSTALLER_ERROR = "installer_error_channel"
    const val ID_INSTALLER_ERROR = -802

    /**
     * Notification channel and ids used by the downloader.
     */
    const val GROUP_DOWNLOADER = "group_downloader"
    const val CHANNEL_DOWNLOADER_PROGRESS = "downloader_progress_channel"
    const val ID_DOWNLOAD_CHAPTER_PROGRESS = -201
    const val CHANNEL_DOWNLOADER_COMPLETE = "downloader_complete_channel"
    const val ID_DOWNLOAD_CHAPTER_COMPLETE = -203
    const val CHANNEL_DOWNLOADER_ERROR = "downloader_error_channel"
    const val ID_DOWNLOAD_CHAPTER_ERROR = -202

    /**
     * Notification channel and ids used by the library updater.
     */
    const val CHANNEL_NEW_CHAPTERS = "new_chapters_channel"
    const val ID_NEW_CHAPTERS = -301
    const val GROUP_NEW_CHAPTERS = "eu.kanade.tachiyomi.NEW_CHAPTERS"

    /**
     * Notification channel and ids used by the backup/restore system.
     */
     const val GROUP_BACKUP_RESTORE = "group_backup_restore"
    const val CHANNEL_BACKUP_RESTORE_PROGRESS = "backup_restore_progress_channel"
    const val ID_BACKUP_PROGRESS = -501
    const val ID_RESTORE_PROGRESS = -503
    const val CHANNEL_BACKUP_RESTORE_COMPLETE = "backup_restore_complete_channel_v2"
    const val ID_BACKUP_COMPLETE = -502
    const val ID_RESTORE_COMPLETE = -504

    /**
     * Notification channel used for crash log file sharing.
     */
    const val CHANNEL_CRASH_LOGS = "crash_logs_channel"
    const val ID_CRASH_LOGS = -601

    /**
     * Notification channel used for Incognito Mode
     */
    const val CHANNEL_INCOGNITO_MODE = "incognito_mode_channel"
    const val ID_INCOGNITO_MODE = -701

    /**
     * Notification channel and ids used for app and extension updates.
     */
     const val GROUP_APK_UPDATES = "group_apk_updates"
    const val CHANNEL_APP_UPDATE = "app_apk_update_channel"
    const val ID_APP_UPDATER = 1
    const val CHANNEL_EXTENSIONS_UPDATE = "ext_apk_update_channel"
    const val ID_UPDATES_TO_EXTS = -401
    const val ID_EXTENSION_INSTALLER = -402

    /**
     * Notification channel and ids used for migration system.
     */
    const val GROUP_MIGRATION = "group_migration"
    const val CHANNEL_MIGRATION_PROGRESS = "migration_progress_channel"
    const val ID_MIGRATION_PROGRESS = -901
    const val CHANNEL_MIGRATION_COMPLETE = "migration_complete_channel"
    const val ID_MIGRATION_COMPLETE = -902
    const val CHANNEL_MIGRATION_ERROR = "migration_error_channel"
    const val ID_MIGRATION_ERROR = -903

    /**
     * Enhanced download notification ids for batch operations
     */
    const val ID_DOWNLOAD_BATCH_PROGRESS = -204
    const val ID_DOWNLOAD_BATCH_COMPLETE = -205
    const val ID_DOWNLOAD_QUEUE_SUMMARY = -206

    /**
     * Library update enhancement ids
     */
    const val ID_LIBRARY_UPDATE_SUMMARY = -103
    const val ID_LIBRARY_UPDATE_ERRORS = -104

    /**
     * Migration batch operation ids
     */
    const val ID_MIGRATION_BATCH_PROGRESS = -904
    const val ID_MIGRATION_BATCH_COMPLETE = -905
}