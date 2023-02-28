package ireader.presentation.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.entities.ExtensionSource
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.CaptionTextComposable
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.repository.AddingRepositoryScreen
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import kotlinx.coroutines.launch


class RepositoryAddScreenSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var text by remember {
            mutableStateOf("")
        }
        val vm : SourceRepositoryViewModel =  getIViewModel()
        val host = SnackBarListener(vm = vm)
        val navigator = LocalNavigator.currentOrThrow

        val showDialog = vm.showAutomaticSourceDialog
        IScaffold(
            snackbarHostState = host,
            topBar = { scrollBehavior ->
                    Toolbar(
                        title = {
                            MidSizeTextComposable(text = stringResource(R.string.repository_adding_a_new))
                        },
                        scrollBehavior = scrollBehavior,
                        actions = {
                            AppIconButton(
                                imageVector = Icons.Default.ContentPasteSearch,
                                onClick = {
                                    vm.showAutomaticSourceDialog.value = true
                                }

                            )
                        },
                    )
            }
        ) {scaffoldPadding ->
            AddingRepositoryScreen(scaffoldPadding, onSave = {
                scope.launch {
                    vm.catalogSourceRepository.insert(ExtensionSource(
                        name = it.name,
                        key = it.key,
                        owner = it.owner,
                        source = it.source,
                        username = it.username,
                        password = it.password,
                        id = 0,
                    ))
                }
                popBackStack(navigator)
            }
            )
            if (showDialog.value) {
                androidx.compose.material3.AlertDialog(onDismissRequest = { showDialog.value = false}, confirmButton = {
                    TextButton(onClick = {
                        vm.scope.launch {
                            try {
                                vm.catalogSourceRepository.insert(vm.parseUrl(text))

                            }catch (e:Exception) {
                                vm.showSnackBar(UiText.StringResource(R.string.url_is_invalid))
                            }
                        }
                        showDialog.value = false
                    }) {
                        MidSizeTextComposable(text = stringResource(id = R.string.add))
                    }
                }, title = {
                    MidSizeTextComposable(text = stringResource(id = R.string.add_as_new))
                }, text = {
                    androidx.compose.material3.OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = text,
                        onValueChange = {
                            text = it
                        },
                        label = {
                            CaptionTextComposable(text = "please enter a valid repository URL")
                        },
                        maxLines = 5,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    )
                },)
            }
        }

    }
}
