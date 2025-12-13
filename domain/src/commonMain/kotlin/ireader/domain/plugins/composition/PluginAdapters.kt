package ireader.domain.plugins.composition

import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginManager
import ireader.plugin.api.AIPlugin
import ireader.plugin.api.AudioStream
import ireader.plugin.api.PluginType
import ireader.plugin.api.TranslationPlugin
import ireader.plugin.api.TTSPlugin
import ireader.plugin.api.VoiceConfig

/**
 * Adapters to make existing plugins composable in pipelines.
 */

/**
 * Adapter for Translation plugins.
 */
class TranslationPluginAdapter(
    private val plugin: TranslationPlugin
) : ComposablePlugin {
    
    override val pluginId: String = plugin.manifest.id
    
    override suspend fun process(input: PipelineData): PipelineResult {
        if (input.type != PipelineDataType.TEXT && 
            input.type != PipelineDataType.BOOK_CONTENT &&
            input.type != PipelineDataType.CHAPTER_CONTENT) {
            return PipelineResult.Error(
                PipelineError.TypeMismatch(PipelineDataType.TEXT, input.type)
            )
        }
        
        val text = input.textContent ?: return PipelineResult.Error(
            PipelineError.PluginError(pluginId, "No text content")
        )
        
        val targetLang = input.metadata["targetLang"] ?: "en"
        val sourceLang = input.metadata["sourceLang"] ?: "auto"
        
        return try {
            val result = plugin.translate(text, sourceLang, targetLang)
            result.fold(
                onSuccess = { translated ->
                    PipelineResult.Success(
                        PipelineData.text(
                            translated,
                            input.metadata + mapOf(
                                "translatedFrom" to sourceLang,
                                "translatedTo" to targetLang
                            )
                        )
                    )
                },
                onFailure = { error ->
                    PipelineResult.Error(
                        PipelineError.PluginError(pluginId, error.message ?: "Translation failed")
                    )
                }
            )
        } catch (e: Exception) {
            PipelineResult.Error(
                PipelineError.PluginError(pluginId, e.message ?: "Translation failed")
            )
        }
    }
    
    override fun canProcess(inputType: PipelineDataType): Boolean {
        return inputType == PipelineDataType.TEXT || 
               inputType == PipelineDataType.BOOK_CONTENT ||
               inputType == PipelineDataType.CHAPTER_CONTENT
    }
    
    override fun getOutputType(): PipelineDataType = PipelineDataType.TEXT
}

/**
 * Adapter for TTS plugins.
 */
class TTSPluginAdapter(
    private val plugin: TTSPlugin
) : ComposablePlugin {
    
    override val pluginId: String = plugin.manifest.id
    
    override suspend fun process(input: PipelineData): PipelineResult {
        if (input.type != PipelineDataType.TEXT) {
            return PipelineResult.Error(
                PipelineError.TypeMismatch(PipelineDataType.TEXT, input.type)
            )
        }
        
        val text = input.textContent ?: return PipelineResult.Error(
            PipelineError.PluginError(pluginId, "No text content")
        )
        
        val voiceId = input.metadata["voice"] ?: plugin.getAvailableVoices().firstOrNull()?.id ?: "default"
        val speed = input.metadata["speed"]?.toFloatOrNull() ?: 1.0f
        val pitch = input.metadata["pitch"]?.toFloatOrNull() ?: 1.0f
        val volume = input.metadata["volume"]?.toFloatOrNull() ?: 1.0f
        
        val voiceConfig = VoiceConfig(
            voiceId = voiceId,
            speed = speed,
            pitch = pitch,
            volume = volume
        )
        
        return try {
            val result = plugin.speak(text, voiceConfig)
            result.fold(
                onSuccess = { audioStream ->
                    // Read all audio data from stream
                    val audioData = readAudioStream(audioStream)
                    val format = plugin.getAudioFormat()
                    
                    PipelineResult.Success(
                        PipelineData.audio(
                            audioData,
                            input.metadata + mapOf(
                                "audioFormat" to format.encoding.name.lowercase(),
                                "sampleRate" to format.sampleRate.toString(),
                                "channels" to format.channels.toString(),
                                "textLength" to text.length.toString()
                            )
                        )
                    )
                },
                onFailure = { error ->
                    PipelineResult.Error(
                        PipelineError.PluginError(pluginId, error.message ?: "TTS synthesis failed")
                    )
                }
            )
        } catch (e: Exception) {
            PipelineResult.Error(
                PipelineError.PluginError(pluginId, e.message ?: "TTS synthesis failed")
            )
        }
    }
    
    private suspend fun readAudioStream(stream: AudioStream): ByteArray {
        val chunks = mutableListOf<ByteArray>()
        val buffer = ByteArray(8192)
        
        try {
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                chunks.add(buffer.copyOf(bytesRead))
            }
        } finally {
            stream.close()
        }
        
        // Combine all chunks
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(result, offset)
            offset += chunk.size
        }
        return result
    }
    
    override fun canProcess(inputType: PipelineDataType): Boolean {
        return inputType == PipelineDataType.TEXT
    }
    
    override fun getOutputType(): PipelineDataType = PipelineDataType.AUDIO
}

/**
 * Adapter for AI plugins.
 */
class AIPluginAdapter(
    private val plugin: AIPlugin
) : ComposablePlugin {
    
    override val pluginId: String = plugin.manifest.id
    
    override suspend fun process(input: PipelineData): PipelineResult {
        val text = input.textContent ?: return PipelineResult.Error(
            PipelineError.PluginError(pluginId, "No text content")
        )
        
        val operation = input.metadata["aiOperation"] ?: "summarize"
        
        return try {
            val result = when (operation) {
                "summarize" -> {
                    val summary = plugin.summarize(text)
                    summary.getOrNull() ?: throw Exception("Summarization failed")
                }
                "analyze_characters" -> {
                    val characters = plugin.analyzeCharacters(text)
                    characters.getOrNull()?.joinToString("\n") { 
                        "${it.name}: ${it.description}" 
                    } ?: throw Exception("Character analysis failed")
                }
                "answer" -> {
                    val question = input.metadata["question"] 
                        ?: throw Exception("No question provided")
                    val answer = plugin.answerQuestion(text, question)
                    answer.getOrNull()?.answer ?: throw Exception("Q&A failed")
                }
                "generate" -> {
                    val generated = plugin.generateText(text)
                    generated.getOrNull() ?: throw Exception("Generation failed")
                }
                else -> throw Exception("Unknown AI operation: $operation")
            }
            
            PipelineResult.Success(
                PipelineData(
                    type = PipelineDataType.AI_RESPONSE,
                    textContent = result,
                    metadata = input.metadata + mapOf("aiOperation" to operation)
                )
            )
        } catch (e: Exception) {
            PipelineResult.Error(
                PipelineError.PluginError(pluginId, e.message ?: "AI operation failed")
            )
        }
    }
    
    override fun canProcess(inputType: PipelineDataType): Boolean {
        return inputType == PipelineDataType.TEXT ||
               inputType == PipelineDataType.BOOK_CONTENT ||
               inputType == PipelineDataType.CHAPTER_CONTENT
    }
    
    override fun getOutputType(): PipelineDataType = PipelineDataType.AI_RESPONSE
}

/**
 * Plugin resolver that wraps the PluginManager.
 */
class PluginManagerResolver(
    private val pluginManager: PluginManager
) : PluginResolver {
    
    private val adapters = mutableMapOf<String, ComposablePlugin>()
    
    override fun resolve(pluginId: String): ComposablePlugin? {
        // Check cache first
        adapters[pluginId]?.let { return it }
        
        // Get plugin from manager
        val plugin = pluginManager.getPlugin(pluginId) ?: return null
        
        // Create appropriate adapter
        val adapter = when (plugin.manifest.type) {
            PluginType.TRANSLATION -> {
                (plugin as? TranslationPlugin)?.let { TranslationPluginAdapter(it) }
            }
            PluginType.TTS -> {
                (plugin as? TTSPlugin)?.let { TTSPluginAdapter(it) }
            }
            PluginType.AI -> {
                (plugin as? AIPlugin)?.let { AIPluginAdapter(it) }
            }
            else -> null
        }
        
        adapter?.let { adapters[pluginId] = it }
        return adapter
    }
    
    override fun getAvailablePlugins(): List<ComposablePlugin> {
        val composableTypes = setOf(PluginType.TRANSLATION, PluginType.TTS, PluginType.AI)
        
        return pluginManager.getEnabledPlugins()
            .filter { it.manifest.type in composableTypes }
            .mapNotNull { resolve(it.manifest.id) }
    }
    
    /**
     * Clear the adapter cache.
     */
    fun clearCache() {
        adapters.clear()
    }
}
