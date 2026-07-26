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
 * Format:  GZIP( ProtoBuf( BackupPayload ) )   where payload.checksum = SHA-256 of the
 * unsigned proto bytes (checksum field blanked).  On deserialize the checksum is verified
 * before returning — if it doesn't match, [BackupException.ChecksumMismatch] is thrown.
 */
class BackupSerializer {

    // ── Public API ────────────────────────────────────────────────────────

    @OptIn(ExperimentalSerializationApi::class)
    fun serialize(payload: BackupPayload): ByteArray {
        // 1. Serialize with blank checksum to compute the real hash
        val unsigned = payload.copy(checksum = "")
        val unsignedBytes = ProtoBuf.encodeToByteArray(unsigned)

        // 2. Compute SHA-256 over the unsigned bytes
        val hash = sha256Hex(unsignedBytes)

        // 3. Serialize again with the checksum baked in
        val signed = payload.copy(checksum = hash)
        val signedBytes = ProtoBuf.encodeToByteArray(signed)

        // 4. GZIP compress
        return compress(signedBytes)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun deserialize(raw: ByteArray): BackupPayload {
        // 1. Decompress — strip every GZIP layer.  Platform FileSavers may add
        // (Android) or omit (Desktop) their own gzip layer, so files can arrive
        // with 0, 1, or 2 layers depending on where they were created/read.
        val decompressed = decompressFully(raw)

        // 2. Decode
        val payload = try {
            ProtoBuf.decodeFromByteArray<BackupPayload>(decompressed)
        } catch (e: Exception) {
            throw BackupException.Corrupted("ProtoBuf decode failed", e)
        }

        // 3. Version gate
        if (payload.version > BackupPayload.CURRENT_VERSION) {
            throw BackupException.UnsupportedVersion(payload.version)
        }

        // 4. Verify checksum (skip if empty — allows unsigned v1/v2 legacy files)
        if (payload.checksum.isNotEmpty()) {
            val unsigned = payload.copy(checksum = "")
            val unsignedBytes = ProtoBuf.encodeToByteArray(unsigned)
            val expected = sha256Hex(unsignedBytes)
            if (expected != payload.checksum) {
                throw BackupException.ChecksumMismatch(expected, payload.checksum)
            }
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

    /**
     * Compute SHA-256 and return as hex string.
     * Uses Okio's ByteString hashing — works on all KMP targets.
     */
    private fun sha256Hex(data: ByteArray): String {
        return data.toByteString().sha256().hex()
    }
}
