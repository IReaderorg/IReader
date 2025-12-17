package ireader.presentation.ui.settings.repository

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.remember.rememberMutableString
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    val focusManager = LocalFocusManager.current

    var repositoryType by remember { mutableStateOf(RepositoryType.IREADER) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }
    var showPasswordFields by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showJSPluginPrompt by remember { mutableStateOf(false) }

    val quickAddPresets = remember {
        listOf(
            QuickAddPreset("IReader Official",
                "https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repov2/index.min.json",
                "IReaderorg", "https://github.com/IReaderorg/IReader-extensions", RepositoryType.IREADER),
            QuickAddPreset("LNReader Plugins",
                "https://raw.githubusercontent.com/kazemcodes/lnreader-plugins-unminified/refs/heads/repo/plugins/plugins.min.json",
                "LNReader", "https://github.com/kazemcodes/lnreader-plugins-unminified", RepositoryType.LNREADER)
        )
    }

    fun validateFields(): Boolean {
        nameError = if (name.value.isBlank()) "Name is required" else null
        urlError = when {
            url.value.isBlank() -> "URL is required"
            !url.value.startsWith("http://") && !url.value.startsWith("https://") -> "URL must start with http:// or https://"
            else -> null
        }
        return nameError == null && urlError == null
    }

    Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Add Card
            item {
                Card(
                    onClick = { showQuickAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Bolt, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(localizeHelper.localize(Res.string.quick_add_popular_repositories),
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Add popular repositories with one tap",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Divider
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f))
                    Text(localizeHelper.localize(Res.string.or), Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(Modifier.weight(1f))
                }
            }

            // Repository Type
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(localizeHelper.localize(Res.string.repository_type),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RepositoryTypeChip(repositoryType == RepositoryType.IREADER,
                            { repositoryType = RepositoryType.IREADER }, localizeHelper.localize(Res.string.website), Modifier.weight(1f))
                        RepositoryTypeChip(repositoryType == RepositoryType.LNREADER,
                            { repositoryType = RepositoryType.LNREADER; showJSPluginPrompt = true },
                            localizeHelper.localize(Res.string.lnreader), Modifier.weight(1f))
                    }
                    if (repositoryType == RepositoryType.LNREADER) {
                        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, null, Modifier.size(18.dp), MaterialTheme.colorScheme.error)
                                Text(localizeHelper.localize(Res.string.important_you_need_to_enable),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Required Fields Section
            item {
                Text(localizeHelper.localize(Res.string.required_information),
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary)
            }

            item {
                ModernTextField(name.value, { name.value = it }, localizeHelper.localize(Res.string.name),
                    Icons.Default.Badge, nameError, "My Repository",
                    KeyboardOptions(imeAction = ImeAction.Next), KeyboardActions { focusManager.moveFocus(FocusDirection.Down) })
            }

            item {
                ModernTextField(url.value, { url.value = it }, localizeHelper.localize(Res.string.url),
                    Icons.Default.Link, urlError, "https://example.com/repo.json",
                    KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    KeyboardActions { focusManager.moveFocus(FocusDirection.Down) })
            }

            // Optional Fields Section
            item {
                Text(localizeHelper.localize(Res.string.optional_information),
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            item {
                ModernTextField(owner.value, { owner.value = it }, localizeHelper.localize(Res.string.owner),
                    Icons.Default.Person, placeholder = "Repository owner",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Down) })
            }

            item {
                ModernTextField(source.value, { source.value = it }, localizeHelper.localize(Res.string.source),
                    Icons.Default.Code, placeholder = "Source URL",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions { focusManager.clearFocus() })
            }

            // Authentication Section
            item {
                Card(
                    onClick = { showPasswordFields = !showPasswordFields },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(localizeHelper.localize(Res.string.authentication_optional),
                            Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Icon(if (showPasswordFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                }
            }

            item {
                AnimatedVisibility(showPasswordFields, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModernTextField(username.value, { username.value = it }, localizeHelper.localize(Res.string.username),
                            Icons.Default.Person, placeholder = "Username",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Down) })
                        ModernTextField(password.value, { password.value = it }, localizeHelper.localize(Res.string.password),
                            Icons.Default.Key, placeholder = "Password", isPassword = true, passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions { focusManager.clearFocus() })
                    }
                }
            }

            // Save Button
            item {
                Button(onClick = {
                    if (validateFields()) {
                        onSave(RepositoryInfo(name.value.trim(), url.value.trim(), owner.value.trim(),
                            source.value.trim(), username.value.trim(), password.value, repositoryType.name))
                    }
                }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Repository", style = MaterialTheme.typography.titleMedium)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Quick Add Dialog
    if (showQuickAddDialog) {
        AlertDialog(onDismissRequest = { showQuickAddDialog = false },
            icon = { Icon(Icons.Outlined.Bolt, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(localizeHelper.localize(Res.string.quick_add_repository)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAddPresets.forEach { preset ->
                        Card(onClick = {
                            name.value = preset.name; url.value = preset.url
                            owner.value = preset.owner; source.value = preset.source
                            repositoryType = preset.type; showQuickAddDialog = false
                        }, Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(preset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(4.dp),
                                        color = if (preset.type == RepositoryType.IREADER) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(preset.type.name, Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Text(preset.owner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton({ showQuickAddDialog = false }) { Text(localizeHelper.localize(Res.string.cancel)) } })
    }

    // JS Plugin Prompt
    if (showJSPluginPrompt) {
        AlertDialog(onDismissRequest = { showJSPluginPrompt = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(localizeHelper.localize(Res.string.enable_javascript_plugins)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(localizeHelper.localize(Res.string.lnreader_repositories_require_javascript_plugins),
                        style = MaterialTheme.typography.bodyMedium)
                    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(localizeHelper.localize(Res.string.to_enable_js_plugins),
                                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(localizeHelper.localize(Res.string.go_to_settings_generaln2_toggle),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton({ showJSPluginPrompt = false }) { Text(localizeHelper.localize(Res.string.got_it)) } })
    }
}


@Composable
private fun RepositoryTypeChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
        } else null,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String? = null,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(icon, label, tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = if (isPassword && onPasswordToggle != null) {
                { IconButton(onClick = onPasswordToggle) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        if (passwordVisible) "Hide" else "Show")
                } }
            } else null,
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

internal data class RepositoryInfo(
    val name: String,
    val key: String,
    val owner: String,
    val source: String,
    val username: String,
    val password: String,
    val repositoryType: String = "IREADER"
)

internal data class QuickAddPreset(
    val name: String,
    val url: String,
    val owner: String,
    val source: String,
    val type: RepositoryType
)

enum class RepositoryType {
    IREADER,
    LNREADER
}
