package ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.domain.repository.LocalSourceRepository
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidSizeTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.SmallTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.asString
import ir.kazemcodes.infinity.core.utils.moshi
import ir.kazemcodes.infinity.feature_library.presentation.components.TabItem
import ir.kazemcodes.infinity.feature_library.presentation.components.Tabs
import ir.kazemcodes.infinity.feature_library.presentation.components.TabsContent
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.EOFException

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionCreatorScreen(modifier: Modifier = Modifier) {
    val backStack = LocalBackstack.current
    val viewModel = rememberService<ExtensionCreatorViewModel>()
    val state = viewModel.state.value
    val pagerState = rememberPagerState()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText
                    )
                }
            }
        }

    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                MidSizeTextComposable(title = "Extension Creator")
            },
            backgroundColor = MaterialTheme.colors.background,
            navigationIcon = {
                TopAppBarBackButton(backStack = backStack)
            },
            actions = {
                SmallTextComposable(title = "Format",
                    modifier = modifier.clickable { viewModel.formatJson() })
                TopAppBarActionButton(imageVector = Icons.Default.Add,
                    title = "Adding Sources Button",
                    onClick = { viewModel.convertJsonToSource() })
            }
        )
    },
        scaffoldState = scaffoldState, snackbarHost = { ISnackBarHost(snackBarHostState = it) }) {
        val tabs = listOf<TabItem>(TabItem.ExtensionCreator(viewModel),
            TabItem.ExtensionCreatorLog(viewModel))
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            Tabs(libraryTabs = tabs, pagerState = pagerState)
            TabsContent(libraryTabs = tabs, pagerState = pagerState)
        }
    }
}

class ExtensionCreatorViewModel(private val localSourceRepository: LocalSourceRepository) :
    ScopedServices.Registered {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state =
        mutableStateOf(ExtensionCreatorState())


    val state: State<ExtensionCreatorState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    override fun onServiceRegistered() {

    }

    override fun onServiceUnregistered() {

    }

    fun onFieldStateChange(value: String) {
        _state.value = state.value.copy(extensionFieldValue = value)
    }

    fun convertJsonToSource() {
        coroutineScope.launch(Dispatchers.IO) {
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
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("Successfully Added To Extensions").asString()))

            } catch (e: JsonEncodingException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: Json Format Was Wrong"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: Json Format Was Wrong").asString()))
            } catch (e: EOFException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: The TextField is Empty."))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: The TextField is Empty.").asString()))
            } catch (e: Exception) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ${e}"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: ${e.localizedMessage}.").asString()))

            }
        }
    }

    fun formatJson() {
        coroutineScope.launch(Dispatchers.IO) {
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
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: Json Format Was Wrong").asString()))
            } catch (e: EOFException) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ERROR: The TextField is Empty."))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: The TextField is Empty.").asString()))
            } catch (e: Exception) {
                _state.value =
                    state.value.copy(errorMessage = state.value.errorMessage.plus("\n\n ${e}"))
                _eventFlow.emit(UiEvent.ShowSnackbar(UiText.DynamicString("ERROR: ${e.localizedMessage}.").asString()))

            }
        }

    }
}


    data class ExtensionCreatorState(
        val extensionFieldValue: String = "",
        val errorMessage: String = "",
    )

    @Composable
    fun ExtensionCreatorTab(viewModel: ExtensionCreatorViewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                value = viewModel.state.value.extensionFieldValue,
                onValueChange = {
                    viewModel.onFieldStateChange(it)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                }),
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                label = { Text(text = "Please Enter Your Source Here") },
                placeholder = { Text(text = "Please Enter Your Source Here...") },
            )
        }

    }

    @Composable
    fun ExtensionCreatorLogTab(viewModel: ExtensionCreatorViewModel) {
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = viewModel.state.value.errorMessage, modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .align(Alignment.TopCenter))
        }


    }