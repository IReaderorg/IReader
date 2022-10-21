package ireader.presentation.core.ui


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.navigation.NamedNavArgument
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.components.component.PreferenceRow
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.CaptionTextComposable
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

object RepositoryScreenSpec : ScreenSpec {

    override val navHostRoute: String = "repository-source"

    override val arguments: List<NamedNavArgument> = listOf(

    )
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        Toolbar(
            title ={
                   MidSizeTextComposable(text =  stringResource(R.string.repository))
            },
            scrollBehavior = controller.scrollBehavior
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        var showDialog by remember {
            mutableStateOf(false)
        }
        var text by remember {
            mutableStateOf("")
        }
        val vm : SourceRepositoryViewModel =  getViewModel(owner = controller.navBackStackEntry)
        SnackBarListener(vm = vm, host = controller.snackBarHostState)
        androidx.compose.material3.Scaffold(
            modifier = Modifier.padding(top = controller.scaffoldPadding.calculateTopPadding()),
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
            floatingActionButton = {
                androidx.compose.material.ExtendedFloatingActionButton(
                    text = {
                        MidSizeTextComposable(
                            text = stringResource(R.string.add),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    onClick = {
                        showDialog = true
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            "",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                )
            },
        ) { padding ->

            LazyColumn {
                items(vm.sources.value) { source ->
                    PreferenceRow(title = source.visibleName(), subtitle = source.key , action = {
                        Row {
                            if(source.id >= 0) {
                                AppIconButton(onClick = {
                                    vm.viewModelScope.launch {
                                        vm.catalogSourceRepository.delete(source)
                                    }
                                }, imageVector = Icons.Default.DeleteForever)
                            }
                            Switch(checked = vm.default.value == source.id, onCheckedChange = {
                                vm.default.value = source.id
                            })
                        }
                    })

                }

            }
            if (showDialog) {
                androidx.compose.material3.AlertDialog(onDismissRequest = { showDialog = false}, confirmButton = {
                    TextButton(onClick = {
                        vm.viewModelScope.launch {
                            try {
                            vm.catalogSourceRepository.insert(vm.parseUrl(text))

                            }catch (e:Exception) {
                                vm.showSnackBar(UiText.StringResource(R.string.url_is_invalid))
                            }
                        }
                        showDialog = false
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
