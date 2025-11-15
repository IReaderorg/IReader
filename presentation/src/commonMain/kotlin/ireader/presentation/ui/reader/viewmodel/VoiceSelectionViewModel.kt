package ireader.presentation.ui.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ireader.domain.services.tts.AudioStreamHandler
import ireader.domain.services.tts.PluginTTSManager
import ireader.domain.services.tts.TTSErrorHandler
import ireader.domain.services.tts.TTSOutput
import ireader.domain.services.tts.VoiceConfiguration
import ireader.domain.services.tts.VoiceFilter
import ireader.domain.services.tts.VoiceSelectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing voice selection and configuration
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
class VoiceSelectionViewModel(
    private val pluginTTSManager: PluginTTSManager,
    private val audioStreamHandler: AudioStreamHandler,
    private val errorHandler: TTSErrorHandler
) : ViewModel() {
    
    private val _state = MutableStateFlow(VoiceSelectionState())
    val state: StateFlow<VoiceSelectionState> = _state.asStateFlow()
    
    init {
        loadVoices()
        observePluginChanges()
    }
    
    /**
     * Load all available voices
     * Requirements: 5.1, 5.2
     */
    fun loadVoices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val voices = pluginTTSManager.getAvailableVoices()
                _state.update {
                    it.copy(
                        availableVoices = voices,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load voices: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Select a voice
     * Requirements: 5.2
     */
    fun selectVoice(voiceId: String) {
        _state.update { it.copy(selectedVoiceId = voiceId) }
    }
    
    /**
     * Update voice configuration
     * Requirements: 5.3, 5.5
     */
    fun updateConfiguration(configuration: VoiceConfiguration) {
        _state.update { it.copy(configuration = configuration.validate()) }
    }
    
    /**
     * Update filter
     * Requirements: 5.1
     */
    fun updateFilter(filter: VoiceFilter) {
        _state.update { it.copy(filter = filter) }
    }
    
    /**
     * Preview a voice
     * Requirements: 5.3
     */
    fun previewVoice(voiceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPreviewing = true, error = null) }
            
            try {
                val result = pluginTTSManager.previewVoice(voiceId)
                
                result.onSuccess { output ->
                    when (output) {
                        is TTSOutput.Plugin -> {
                            // Handle plugin TTS output
                            if (output.supportsStreaming) {
                                audioStreamHandler.streamAudio(
                                    audioStream = output.audioStream,
                                    onChunk = { buffer, bytesRead ->
                                        // Play audio chunk
                                    },
                                    onComplete = {
                                        _state.update { it.copy(isPreviewing = false) }
                                    },
                                    onError = { error ->
                                        _state.update {
                                            it.copy(
                                                isPreviewing = false,
                                                error = "Preview failed: ${error.message}"
                                            )
                                        }
                                    }
                                )
                            } else {
                                // Read full stream
                                val audioData = audioStreamHandler.readFullStream(output.audioStream)
                                audioData.onSuccess {
                                    // Play audio data
                                    _state.update { it.copy(isPreviewing = false) }
                                }.onFailure { error ->
                                    _state.update {
                                        it.copy(
                                            isPreviewing = false,
                                            error = "Preview failed: ${error.message}"
                                        )
                                    }
                                }
                            }
                        }
                        is TTSOutput.BuiltIn -> {
                            // Handle built-in TTS output
                            _state.update { it.copy(isPreviewing = false) }
                        }
                    }
                }.onFailure { error ->
                    val ttsError = errorHandler.exceptionToError(error as Exception, voiceId)
                    val errorResult = errorHandler.handleError(ttsError)
                    
                    _state.update {
                        it.copy(
                            isPreviewing = false,
                            error = errorHandler.getUserMessage(ttsError)
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isPreviewing = false,
                        error = "Preview failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Stop preview
     */
    fun stopPreview() {
        _state.update { it.copy(isPreviewing = false) }
    }
    
    /**
     * Speak text with selected voice
     * Requirements: 5.2, 5.3, 5.4
     */
    fun speak(text: String) {
        val selectedVoiceId = _state.value.selectedVoiceId
        if (selectedVoiceId == null) {
            _state.update { it.copy(error = "No voice selected") }
            return
        }
        
        viewModelScope.launch {
            try {
                val config = _state.value.configuration
                val result = pluginTTSManager.speak(
                    text = text,
                    voiceId = selectedVoiceId,
                    speed = config.speed,
                    pitch = config.pitch,
                    volume = config.volume
                )
                
                result.onSuccess { output ->
                    when (output) {
                        is TTSOutput.Plugin -> {
                            // Handle plugin TTS output
                            if (output.supportsStreaming && config.enableStreaming) {
                                audioStreamHandler.streamAudio(
                                    audioStream = output.audioStream,
                                    onChunk = { buffer, bytesRead ->
                                        // Play audio chunk
                                    },
                                    onComplete = {
                                        // Playback complete
                                    },
                                    onError = { error ->
                                        _state.update {
                                            it.copy(error = "Playback failed: ${error.message}")
                                        }
                                    }
                                )
                            } else {
                                // Read full stream
                                val audioData = audioStreamHandler.readFullStream(output.audioStream)
                                audioData.onSuccess {
                                    // Play audio data
                                }.onFailure { error ->
                                    _state.update {
                                        it.copy(error = "Playback failed: ${error.message}")
                                    }
                                }
                            }
                        }
                        is TTSOutput.BuiltIn -> {
                            // Handle built-in TTS output
                        }
                    }
                }.onFailure { error ->
                    val ttsError = errorHandler.exceptionToError(error as Exception, selectedVoiceId)
                    val errorResult = errorHandler.handleError(ttsError)
                    
                    _state.update {
                        it.copy(error = errorHandler.getUserMessage(ttsError))
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Speech failed: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Observe plugin changes and reload voices
     * Requirements: 5.2
     */
    private fun observePluginChanges() {
        viewModelScope.launch {
            pluginTTSManager.observePlugins().collect {
                // Reload voices when plugins change
                loadVoices()
            }
        }
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Reset configuration to defaults
     */
    fun resetConfiguration() {
        _state.update { it.copy(configuration = VoiceConfiguration.DEFAULT) }
    }
}
