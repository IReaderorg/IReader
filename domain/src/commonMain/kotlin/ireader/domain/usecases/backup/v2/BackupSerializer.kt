package ireader.domain.usecases.backup.v2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import okio.Buffer
import okio.ByteString.Companion.toByteString
import okio.GzipSink
import okio.GzipSource
import okio.use

/**
 * Single authority for backup serialization, compression, and integrity.
 *
 * Format: GZIP( ProtoBuf( BackupPayload ) )
 * Streamlined single-pass encoding and decoding for minimal memory allocation.
 */
class BackupSerializer {

    // ── Public API ────────────────────────────────────────────────────────

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize(payload: BackupPayload): ByteArray {
        val protoBytes = ProtoBuf.encodeToByteArray(payload)
        return compress(protoBytes)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun deserialize(raw: ByteArray): BackupPayload {
        val decompressed = decompressFully(raw)
        val payload = try {
            ProtoBuf.decodeFromByteArray<BackupPayload>(decompressed)
        } catch (e: Exception) {
            throw BackupException.Corrupted("ProtoBuf decode failed", e)
        }

        if (payload.version > BackupPayload.CURRENT_VERSION) {
            throw BackupException.UnsupportedVersion(payload.version)
        }

        return payload
    }

    /**
     * Try to detect the format version without full decode.
     * Returns -1 if the bytes are not valid protobuf at all.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun detectVersion(raw: ByteArray): Int {
        return try {
            val decompressed = decompressFully(raw)
            val payload = ProtoBuf.decodeFromByteArray<BackupPayload>(decompressed)
            payload.version
        } catch (_: Exception) {
            -1
        }
    }

    // ── Compression ───────────────────────────────────────────────────────

    fun compress(data: ByteArray): ByteArray {
        val buffer = Buffer()
        GzipSink(buffer).use { sink ->
            sink.write(Buffer().write(data), data.size.toLong())
        }
        return buffer.readByteArray()
    }

    fun decompress(data: ByteArray): ByteArray {
        return try {
            val source = Buffer().write(data)
            val gzipSource = GzipSource(source)
            val output = Buffer()
            output.writeAll(gzipSource)
            output.readByteArray()
        } catch (e: Exception) {
            throw BackupException.Corrupted("GZIP decompression failed", e)
        }
    }

    /**
     * Decompress repeatedly until the bytes are no longer GZIP (magic 0x1f 0x8b).
     * Handles files with 0, 1, or 2 gzip layers (platform FileSavers may add one).
     */
    fun decompressFully(data: ByteArray): ByteArray {
        var current = data
        while (isGzip(current)) {
            current = decompress(current)
        }
        return current
    }

    private fun isGzip(data: ByteArray): Boolean =
        data.size > 2 && data[0] == 0x1f.toByte() && data[1] == 0x8b.toByte()

    // ── Hashing ───────────────────────────────────────────────────────────

    fun sha256Hex(data: ByteArray): String {
        return data.toByteString().sha256().hex()
    }
}
