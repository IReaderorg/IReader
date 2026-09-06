package ireader.domain.services.tts_service.local

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.v2.EngineEvent
import ireader.domain.services.tts_service.v2.TTSEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Adapter that exposes [LocalTTSEngine] as a v2 [TTSEngine].
 */
class LocalTTSEngineV2(
    private val engine: LocalTTSEngine
) : TTSEngine {

    companion object {
        private const val TAG = "LocalTTSEngineV2"
    }

    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    override val events: Flow<EngineEvent> = _events
    override val name: String = engine.getEngineName()

    init {
        engine.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {
                Log.debug { "$TAG: onStart($utteranceId)" }
                _events.tryEmit(EngineEvent.Started(utteranceId))
            }

            override fun onDone(utteranceId: String) {
                Log.debug { "$TAG: onDone($utteranceId)" }
                _events.tryEmit(EngineEvent.Completed(utteranceId))
            }

            override fun onError(utteranceId: String, error: String) {
                Log.error { "$TAG: onError($utteranceId, $error)" }
                _events.tryEmit(EngineEvent.Error(utteranceId, error))
            }

            override fun onReady() {
                Log.debug { "$TAG: onReady()" }
                _events.tryEmit(EngineEvent.Ready)
            }
        })
    }

    override suspend fun speak(text: String, utteranceId: String) {
        engine.speak(text, utteranceId)
    }

    override fun stop() {
        engine.stop()
    }

    override fun pause() {
        engine.pause()
    }

    override fun resume() {
        engine.resume()
    }

    override fun setSpeed(speed: Float) {
        engine.setSpeed(speed)
    }

    override fun setPitch(pitch: Float) {
        engine.setPitch(pitch)
    }

    override fun isReady(): Boolean = engine.isReady()

    override fun release() {
        engine.cleanup()
    }

    override suspend fun generateAudioForText(text: String): ByteArray? {
        return engine.generateAudio(text)
    }

    override fun precacheNext(items: List<Pair<String, String>>) {
        engine.precacheNext(items)
    }

    override suspend fun isTextCached(text: String): Boolean {
        return engine.isTextCached(text)
    }

    override suspend fun getCachedIndices(texts: List<String>): Set<Int> {
        return texts.indices.filter { isTextCached(texts[it]) }.toSet()
    }
}

/**
 * Extension helper to convert a [LocalTTSEngine] to a v2 [TTSEngine].
 */
fun LocalTTSEngine.asV2(): TTSEngine = LocalTTSEngineV2(this)
