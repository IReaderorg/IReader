package ireader.presentation.ui.settings.repository

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.remember.rememberMutableString
import ireader.presentation.ui.component.reusable_composable.SmallTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddingRepositoryScreen(
        scaffoldPadding: PaddingValues,
        onSave: (RepositoryInfo) -> Unit,
) {
    val name = rememberMutableString()
    val url = rememberMutableString()
    val owner = rememberMutableString()
    val source = rememberMutableString()
    val username = rememberMutableString()
    val password = rememberMutableString()
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    var showHelpDialog by remember { mutableStateOf(false) }
    androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize().padding(top = scaffoldPadding.calculateTopPadding()),
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = { androidx.compose.material3.Text("Add Repository") },
                    actions = {
                        androidx.compose.material3.IconButton(
                            onClick = { showHelpDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = localizeHelper.localize(MR.strings.help)
                            )
                        }
                    }
                )
            },
            floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
            floatingActionButton = {
                androidx.compose.material3.FloatingActionButton(
                        onClick = {
                            onSave(RepositoryInfo(
                                    name = name.value,
                                    key = url.value,
                                    owner = owner.value,
                                    source = source.value,
                                    username = username.value,
                                    password = password.value
                            ))
                        },
                        content = {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        },
                        shape = CircleShape
                )
            },
    ) { padding ->

        LazyColumn(
                modifier = Modifier
                        .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                FormComponent(localizeHelper.localize(MR.strings.name), Icons.Default.Badge, name)
            }
            item {
                FormComponent(localizeHelper.localize(MR.strings.url), Icons.Default.Link, url)
            }
            item {
                FormComponent(localizeHelper.localize(MR.strings.owner), Icons.Default.Copyright, owner)
            }
            item {
                FormComponent(localizeHelper.localize(MR.strings.source), Icons.Default.HideSource, source)
            }
            item {
                FormComponent(localizeHelper.localize(MR.strings.username), Icons.Default.Person, username, false)
            }
            item {
                FormComponent(localizeHelper.localize(MR.strings.password), Icons.Default.Key, password, false)
            }
        }
    }
    
    // Help dialog
    if (showHelpDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            },
            title = {
                androidx.compose.material3.Text(
                    text = localizeHelper.localize(MR.strings.what_is_repository),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = localizeHelper.localize(MR.strings.repository_explanation),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.material3.MaterialTheme.shapes.small,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            androidx.compose.material3.Text(
                                text = "Example URL:",
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.Text(
                                text = localizeHelper.localize(MR.strings.repository_example_url),
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showHelpDialog = false }
                ) {
                    androidx.compose.material3.Text("Got it")
                }
            }
        )
    }
}

internal data class RepositoryInfo(
        val name: String,
        val key: String,
        val owner: String,
        val source: String,
        val username: String,
        val password: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComponent(
        name: String,
        icon: ImageVector,
        state: MutableState<String>,
        enable: Boolean = true
) {
    androidx.compose.material3.OutlinedTextField(
            value = state.value,
            onValueChange = { state.value = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(imageVector = icon, contentDescription = name)
            },
            enabled = enable,
            label = {
                SmallTextComposable(text = name)
            }
    )
    Spacer(modifier = Modifier.height(16.dp))
}


@Composable
private fun AddingRepositoryScreenPrev() {
    AddingRepositoryScreen(
            scaffoldPadding = PaddingValues(0.dp),
            onSave = {},
    )
}


@Composable
private fun FormComponentPreview() {
    val state = remember {
        mutableStateOf("")
    }
    FormComponent("Name", Icons.Default.AccountBalance, state)
}

