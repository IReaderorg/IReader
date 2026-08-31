package ireader.domain.services.tts_service

import ireader.plugin.api.PluginAuthor
import ireader.plugin.api.PluginManifest
import ireader.plugin.api.PluginType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Tests for [GradioTTSPluginLoader.convertFromManifestMetadata].
 *
 * Covers the metadata key formats supported for `GRADIO_TTS`/`TTS` plugins:
 * both the legacy `gradio.*`-prefixed keys and the bare keys (`spaceUrl`,
 * `apiName`, `params`, ...), plus handling of missing/invalid metadata.
 */
class GradioTTSPluginLoaderTest {

    private fun manifest(
        id: String,
        type: PluginType,
        metadata: Map<String, String>? = null,
    ): PluginManifest = PluginManifest(
        id = id,
        name = "Plugin $id",
        version = "1.0.0",
        versionCode = 1,
        description = "desc",
        author = PluginAuthor(name = "author"),
        type = type,
        permissions = emptyList(),
        minIReaderVersion = "1.0.0",
        platforms = emptyList(),
        metadata = metadata,
    )

    // ── Legacy prefixed keys ────────────────────────────────────────────

    @Test
    fun `converts legacy gradio-prefixed metadata`() {
        val m = manifest("legacy", PluginType.GRADIO_TTS, metadata = mapOf(
            "gradio.spaceUrl" to "https://legacy.hf.space",
            "gradio.apiName" to "/predict",
            "gradio.apiType" to "GRADIO_API_CALL",
            "gradio.audioOutputIndex" to "1",
            "gradio.params" to """[{"type":"text","name":"text"}]""",
        ))

        val config = GradioTTSPluginLoader.convertFromManifestMetadata(m)

        assertNotNull(config, "Legacy prefixed metadata should convert")
        config.let {
            assertEquals("plugin_legacy", it.id)
            assertEquals("Plugin legacy", it.name)
            assertEquals("https://legacy.hf.space", it.spaceUrl)
            assertEquals("/predict", it.apiName)
            assertEquals(GradioApiType.GRADIO_API_CALL, it.apiType)
            assertEquals(1, it.audioOutputIndex)
            assertEquals(1, it.parameters.size)
        }
    }

    @Test
    fun `uses defaults when optional legacy keys are missing`() {
        val m = manifest("minimal", PluginType.GRADIO_TTS, metadata = mapOf(
            "gradio.spaceUrl" to "https://minimal.hf.space",
        ))

        val config = GradioTTSPluginLoader.convertFromManifestMetadata(m)

        assertNotNull(config)
        config.let {
            assertEquals("/predict", it.apiName)
            assertEquals(GradioApiType.AUTO, it.apiType)
            assertEquals(0, it.audioOutputIndex)
            assertEquals(1, it.parameters.size, "Defaults to a single text param")
        }
    }

    // ── Bare key formats (new in multi-format support) ─────────────────

    @Test
    fun `converts bare spaceUrl key`() {
        val m = manifest("bare", PluginType.GRADIO_TTS, metadata = mapOf(
            "spaceUrl" to "https://bare.hf.space",
        ))

        val config = GradioTTSPluginLoader.convertFromManifestMetadata(m)

        assertEquals("https://bare.hf.space", config?.spaceUrl)
    }

    @Test
    fun `falls back across url key variants`() {
        fun converted(urlKey: String, urlValue: String): String? {
            val m = manifest("v", PluginType.TTS, metadata = mapOf(urlKey to urlValue))
            return GradioTTSPluginLoader.convertFromManifestMetadata(m)?.spaceUrl
        }

        assertEquals("https://a.hf.space", converted("url", "https://a.hf.space"))
        assertEquals("https://b.hf.space", converted("gradio_url", "https://b.hf.space"))
        assertEquals("https://c.hf.space", converted("space_url", "https://c.hf.space"))
    }

    @Test
    fun `parses modern root keys correctly`() {
        val m = manifest("modern", PluginType.GRADIO_TTS, metadata = mapOf(
            "spaceUrl" to "https://modern.hf.space",
            "apiName" to "/tts",
            "params" to """[{"type":"speed","name":"speed"}]""",
        ))

        val config = GradioTTSPluginLoader.convertFromManifestMetadata(m)

        assertNotNull(config)
        config.let {
            assertEquals("/tts", it.apiName)
            assertEquals(1, it.parameters.size)
            assertTrue(it.parameters[0].isSpeedInput)
        }
    }

    // ── Parameter parsing ──────────────────────────────────────────────

    @Test
    fun `parses speed float and choice params`() {
        val m = manifest("p", PluginType.GRADIO_TTS, metadata = mapOf(
            "spaceUrl" to "https://p.hf.space",
            "gradio.params" to """[
                {"type":"speed","name":"speed","default":1.5,"min":0.5,"max":2.0},
                {"type":"choice","name":"voice","choices":["a","b"],"default":"a"}
            ]""",
        ))

        val params = GradioTTSPluginLoader.convertFromManifestMetadata(m)?.parameters

        assertNotNull(params)
        params.let {
            assertEquals(2, it.size)
            assertEquals(1.5f, it[0].defaultValue?.toFloat())
            assertEquals("a", it[1].defaultValue)
            assertEquals(listOf("a", "b"), it[1].choices)
        }
    }

    @Test
    fun `falls back to a single text param when params json is malformed`() {
        val m = manifest("bad", PluginType.GRADIO_TTS, metadata = mapOf(
            "spaceUrl" to "https://bad.hf.space",
            "gradio.params" to "not-json",
        ))

        val config = GradioTTSPluginLoader.convertFromManifestMetadata(m)

        assertNotNull(config)
        assertEquals(1, config.parameters.size)
    }

    // ── Missing / invalid metadata ─────────────────────────────────────

    @Test
    fun `returns null when metadata is absent`() {
        val m = manifest("none", PluginType.GRADIO_TTS, metadata = null)
        assertNull(GradioTTSPluginLoader.convertFromManifestMetadata(m))
    }

    @Test
    fun `returns null when no spaceUrl key is present`() {
        val m = manifest("no-url", PluginType.GRADIO_TTS, metadata = mapOf(
            "apiName" to "/predict",
        ))
        assertNull(GradioTTSPluginLoader.convertFromManifestMetadata(m))
    }
}
