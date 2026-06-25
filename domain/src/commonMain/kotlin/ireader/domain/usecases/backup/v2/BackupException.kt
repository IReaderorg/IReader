package ireader.domain.usecases.backup.v2

/**
 * All backup/restore errors funnel through this hierarchy.
 * Every subclass carries enough context for the UI to show a human-readable message
 * and for logs to pinpoint the failure.
 */
sealed class BackupException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    /** Could not read the backup file from disk / content resolver. */
    class ReadFailed(val path: String, cause: Throwable) :
        BackupException("Cannot read backup at $path", cause)

    /** Could not write the backup file to disk / content resolver. */
    class WriteFailed(val path: String, cause: Throwable) :
        BackupException("Cannot write backup to $path", cause)

    /** Serialized bytes failed the SHA-256 integrity check. */
    class ChecksumMismatch(val expected: String, val actual: String) :
        BackupException("Backup integrity check failed (expected=$expected, actual=$actual)")

    /** The backup file is truncated, zero-filled, or not valid ProtoBuf. */
    class Corrupted(reason: String, cause: Throwable? = null) :
        BackupException("Backup corrupted: $reason", cause)

    /** The backup version is newer than this build understands. */
    class UnsupportedVersion(val detectedVersion: Int) :
        BackupException("Backup version $detectedVersion is not supported by this app version")

    /** Not enough free space to write the backup. */
    class InsufficientSpace(val requiredBytes: Long, val availableBytes: Long) :
        BackupException("Insufficient disk space: need ${requiredBytes}B, have ${availableBytes}B")

    /** Post-write verification (read-back + checksum) failed. */
    class VerificationFailed(reason: String) :
        BackupException("Backup verification failed: $reason")

    /** A specific book/chapter/category failed to restore but the rest continued. */
    class PartialRestore(val itemName: String, val itemType: String, cause: Throwable) :
        BackupException("Failed to restore $itemType '$itemName'", cause)
}
