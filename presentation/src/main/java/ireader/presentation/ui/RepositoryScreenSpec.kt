package ireader.presentation.ui


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ireader.ui.core.ui.SnackBarListener
import ireader.ui.component.Controller
import ireader.ui.component.components.TitleToolbar
import ireader.presentation.R
import ireader.ui.component.components.component.SwitchPreference
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.ui.settings.repository.SourceRepositoryViewModel
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
        TitleToolbar(
            title = stringResource(R.string.repository),
            navController = null,
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
                    SwitchPreference(title = source.visibleName(), preference = vm.default)
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
                    }) {
                        MidSizeTextComposable(text = stringResource(id = R.string.add))
                    }
                }, title = {
                    MidSizeTextComposable(text = stringResource(id = R.string.add_as_new))
                }, text = {
                    BasicTextField(value = text, onValueChange = {text = it} )
                },)
            }

        }


    }
}
