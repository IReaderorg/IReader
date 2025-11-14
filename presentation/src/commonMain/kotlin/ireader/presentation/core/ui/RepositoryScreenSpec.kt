package ireader.presentation.core.ui


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.ktor.http.*
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
        val vm : SourceRepositoryViewModel =  getIViewModel()
        val host = SnackBarListener(vm = vm)
        val navigator = LocalNavigator.currentOrThrow

        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    title ={
                        MidSizeTextComposable(text =  localize(Res.string.repository))
                    },
                    scrollBehavior = scrollBehavior,
            navigationIcon = { TopAppBarBackButton(onClick = { popBackStack(navigator) }) },
                )
            },
            snackbarHostState = host,
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        MidSizeTextComposable(
                            text = localize(Res.string.add),
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
            if (vm.sources.value.isEmpty()) {
                // Empty state
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        
                        Text(
                            text = localize(Res.string.no_repositories_added),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = localize(Res.string.no_repositories_added_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                navigator.push(RepositoryAddScreenSpec())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                            Text(text = localize(Res.string.add_repository))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding)
                ) {
                    items(vm.sources.value) { source ->
                        PreferenceRow(title = source.visibleName(), subtitle = source.key , action = {
                            Row {
                                if(source.id >= 0) {
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
}
