package ireader.domain.usecases.backup.v2

import ireader.domain.usecases.backup.backup.Backup
import ireader.domain.usecases.backup.backup.dunmpStableBackup
import ireader.domain.usecases.backup.backup.legecy.nineteensep.dumpNineteenSepLegacyBackup
import ireader.domain.usecases.backup.backup.legecy.twnetysep.dumpTwentySepLegacyBackup

/**
 * Wraps the three existing protobuf parsers (stable, Sep-2024 legacy, Sep-2019 legacy)
 * behind a single entry point.  Returns a modern [BackupPayload] regardless of which
 * format the bytes actually are.
 *
 * This class exists so [BackupOrchestrator] never has to know about legacy formats —
 * it just calls [migrate] and gets a [BackupPayload] back, or throws.
 */
class LegacyMigrator {

    /**
     * Attempt to decode [raw] (already decompressed) bytes into a [BackupPayload].
     *
     * Tries the formats newest-first so we pick the most specific parser:
     *   1. Current stable ProtoBuf  →  BackupPayload  (v3 direct)
     *   2. Sep-2024 legacy           →  Backup → BackupPayload
     *   3. Sep-2019 legacy           →  Backup → BackupPayload
     *
     * @throws BackupException.Corrupted if none of the parsers succeed.
     */
    fun migrate(raw: ByteArray): BackupPayload {
        // Try 1: current stable format — may already be v3 BackupPayload
        val fromStable = tryStable(raw)
        if (fromStable != null) return fromStable

        // Try 2: Sep-2024 legacy (List<Page> chapter content)
        val from2024 = tryTwentyFourSep(raw)
        if (from2024 != null) return from2024

        // Try 3: Sep-2019 legacy (List<String> chapter content, Int types)
        val from2019 = tryNineteenSep(raw)
        if (from2019 != null) return from2019

        throw BackupException.Corrupted(
            "Unable to parse backup: tried stable, Sep-2024, and Sep-2019 formats"
        )
    }

    // ── Individual format attempts ─────────────────────────────────────────

    private fun tryStable(raw: ByteArray): BackupPayload? {
        return try {
            val backup = raw.dunmpStableBackup()
            backup.toPayload()
        } catch (_: Exception) {
            null
        }
    }

    private fun tryTwentyFourSep(raw: ByteArray): BackupPayload? {
        return try {
            val backup = raw.dumpTwentySepLegacyBackup()
            backup.toPayload()
        } catch (_: Exception) {
            null
        }
    }

    private fun tryNineteenSep(raw: ByteArray): BackupPayload? {
        return try {
            val backup = raw.dumpNineteenSepLegacyBackup()
            backup.toPayload()
        } catch (_: Exception) {
            null
        }
    }

    // ── Conversion from legacy Backup → BackupPayload ─────────────────────

    private fun Backup.toPayload(): BackupPayload {
        val bookSnapshots = library.map { bookProto ->
            BookSnapshot(
                sourceId = bookProto.sourceId,
                key = bookProto.key,
                title = bookProto.title,
                author = bookProto.author,
                description = bookProto.description,
                genres = bookProto.genres,
                status = bookProto.status,
                cover = bookProto.cover,
                customCover = bookProto.customCover,
                lastUpdate = bookProto.lastUpdate,
                initialized = bookProto.initialized,
                dateAdded = bookProto.dateAdded,
                viewer = bookProto.viewer,
                flags = bookProto.flags,
                chapters = bookProto.chapters.map { ch ->
                    ChapterSnapshot(
                        key = ch.key,
                        name = ch.name,
                        translator = ch.translator,
                        read = ch.read,
                        bookmark = ch.bookmark,
                        dateFetch = ch.dateFetch,
                        dateUpload = ch.dateUpload,
                        number = ch.number,
                        sourceOrder = ch.sourceOrder,
                        content = ch.content,
                        type = ch.type,
                        lastPageRead = ch.lastPageRead,
                    )
                },
                categoryOrders = bookProto.categories,
            )
        }

        val categorySnapshots = categories.map { cat ->
            CategorySnapshot(
                name = cat.name,
                order = cat.order,
                flags = cat.flags,
            )
        }

        return BackupPayload(
            version = BackupPayload.CURRENT_VERSION,
            books = bookSnapshots,
            categories = categorySnapshots,
            metadata = BackupMetadata(
                bookCount = bookSnapshots.size,
                chapterCount = bookSnapshots.sumOf { it.chapters.size },
            ),
        )
    }
}
