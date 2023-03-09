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
    androidx.compose.material3.Scaffold(
            modifier = Modifier.fillMaxSize().padding(top = scaffoldPadding.calculateTopPadding()),
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

