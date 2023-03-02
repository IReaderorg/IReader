package ireader.domain.preferences.models.prefs

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.core.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IReaderVoicePreferences @OptIn(ExperimentalTextApi::class) constructor(
    private val preference: Preference<String>,
) : Preference<IReaderVoice> {

    override fun key(): String {
        return preference.key()
    }

    @OptIn(ExperimentalTextApi::class)
    override fun get(): IReaderVoice {
        return if (isSet()) {
            getPrefs()
        } else {
            this.defaultValue()
        }
    }

    @OptIn(ExperimentalTextApi::class)
    fun getPrefs(): IReaderVoice {
        val voice = preference.get()
        return try {
            return Json.decodeFromString<IReaderVoice?>(voice) ?: this.defaultValue()
        } catch (e: Exception) {
            return IReaderVoice(
                "",
                "",
                "",
                ""
            )
        }
    }

    override suspend fun read(): IReaderVoice {
        return if (isSet()) {
            getPrefs()
        } else {
            this.defaultValue()
        }
    }

    override fun set(value: IReaderVoice) {
        if (value != this.defaultValue()) {
            kotlin.runCatching {
                preference.set(Json.encodeToString<IReaderVoice>(value))
            }
        } else {
            preference.delete()
        }
    }

    override fun isSet(): Boolean {
        return preference.isSet()
    }

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): IReaderVoice {
        return IReaderVoice("", "", "", "")
    }

    override fun changes(): Flow<IReaderVoice> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<IReaderVoice> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

fun Preference<String>.asVoice(): IReaderVoicePreferences {
    return IReaderVoicePreferences(this)
}

@Serializable
data class IReaderVoice(
    val name: String,
    val language: String,
    val country: String,
    val localDisplayName: String
)
