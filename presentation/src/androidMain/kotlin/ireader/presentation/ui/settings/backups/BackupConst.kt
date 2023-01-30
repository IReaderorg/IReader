package ireader.presentation.ui.settings.backups

object BackupConst {

    private const val NAME = "BackupRestoreServices"
    const val APPLICATION_ID = "\"ir.kazemcodes.infinityreader\""
    const val EXTRA_URI = "$APPLICATION_ID.$NAME.EXTRA_URI"
    const val EXTRA_FLAGS = "$APPLICATION_ID.$NAME.EXTRA_FLAGS"
    const val EXTRA_MODE = "$APPLICATION_ID.$NAME.EXTRA_MODE"

    const val BACKUP_TYPE_LEGACY = 0
    const val BACKUP_TYPE_FULL = 1

    // Filter options
    internal const val BACKUP_CATEGORY = 0x1
    internal const val BACKUP_CATEGORY_MASK = 0x1
    internal const val BACKUP_CHAPTER = 0x2
    internal const val BACKUP_CHAPTER_MASK = 0x2
    internal const val BACKUP_HISTORY = 0x4
    internal const val BACKUP_HISTORY_MASK = 0x4
    internal const val BACKUP_TRACK = 0x8
    internal const val BACKUP_TRACK_MASK = 0x8
    internal const val BACKUP_ALL = 0xF
}
