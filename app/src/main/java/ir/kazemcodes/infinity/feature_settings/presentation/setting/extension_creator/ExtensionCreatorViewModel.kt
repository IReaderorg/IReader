package ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.repository.LocalSourceRepository
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.moshi
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.EOFException
import javax.inject.Inject

@HiltViewModel
class ExtensionCreatorViewModel @Inject constructor(private val localSourceRepository: LocalSourceRepository) : ViewModel() {
   
    private val _state =
        mutableStateOf(ExtensionCreatorState())
    val state: State<ExtensionCreatorState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun onFieldStateChange(value: String) {
        _state.value = state.value.copy(extensionFieldValue = value)
    }

    fun convertJsonToSource() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val json = state.value.extensionFieldValue
                val moshi: Moshi = moshi
                val jsonAdapter: JsonAdapter<SourceTower> =
                    moshi.adapter<SourceTower>(SourceTower::class.java)
                val source = jsonAdapter.fromJson(json)
                val formatedJson = json
                    .replace(" ", "")
                    .replace("\n", "")
                    .replace("[", "[\n")
                    .replace("]", "\n}")
                    .replace("{", "\n{")
                    .replace("}", "\n}")
                    .replace(",", ",\n")
                _state.value = state.value.copy(extensionFieldValue = formatedJson)
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n SUCCESS: ${formatedJson} have correct format"))

                if (source != null) {
                    localSourceRepository.addSource(source)
                }
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Successfully Added To Extensions")))

            } catch (e: JsonEncodingException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: Json Format Was Wrong"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: Json Format Was Wrong")))
            } catch (e: EOFException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: The TextField is Empty."))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: The TextField is Empty.")))
            } catch (e: Exception) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ${e}"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: ${e.localizedMessage}.")))

            }
        }
    }

    fun formatJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val json = state.value.extensionFieldValue
                val moshi: Moshi = moshi
                val jsonAdapter: JsonAdapter<SourceTower> =
                    moshi.adapter<SourceTower>(SourceTower::class.java)
                val source = jsonAdapter.fromJson(json)
                val formatedJson = json
                    .replace(" ", "")
                    .replace("\n", "")
                    .replace("[", "[\n")
                    .replace("]", "\n}")
                    .replace("{", "\n{")
                    .replace("}", "\n}")
                    .replace(",", ",\n")
                _state.value = state.value.copy(extensionFieldValue = formatedJson)
            } catch (e: JsonEncodingException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: Json Format Was Wrong"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: Json Format Was Wrong")))
            } catch (e: EOFException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: The TextField is Empty."))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: The TextField is Empty.")))
            } catch (e: Exception) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ${e}"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: ${e.localizedMessage}.")))

            }
        }

    }
}