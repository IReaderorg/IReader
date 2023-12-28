package ireader.presentation.core.ui


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.http.*
import ireader.i18n.localize

import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.settings.repository.SourceRepositoryViewModel
import kotlinx.coroutines.launch


class RepositoryScreenSpec : VoyagerScreen() {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: SourceRepositoryViewModel = getIViewModel()
        val host = SnackBarListener(vm = vm)
        val navigator = LocalNavigator.currentOrThrow

        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    title = {
                        MidSizeTextComposable(text = localize { xml -> xml.repository })
                    },
                    scrollBehavior = scrollBehavior,
            navigationIcon = { TopAppBarBackButton(onClick = { popBackStack(navigator) }) },
                )
            },
            snackbarHostState = host,
            floatingActionButtonPosition = ireader.presentation.ui.component.FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        MidSizeTextComposable(
                            text = localize { xml ->
                                xml.add
                            },
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    onClick = {
                        navigator.push(RepositoryAddScreenSpec())
                        //showDialog = true
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            "",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    },
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(vm.sources.value) { source ->
                    PreferenceRow(title = source.visibleName(), subtitle = source.key, action = {
                        Row {
                            if (source.id >= 0) {
                                AppIconButton(onClick = {
                                    vm.scope.launch {
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

        }
    }
}
