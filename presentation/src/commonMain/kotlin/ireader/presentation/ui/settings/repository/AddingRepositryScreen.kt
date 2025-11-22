package ireader.presentation.ui.settings.repository

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val focusManager = LocalFocusManager.current
    
    // Repository type selection
    var repositoryType by remember { mutableStateOf(RepositoryType.IREADER) }
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var urlError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPasswordFields by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Quick add presets
    val quickAddPresets = remember {
        listOf(
            QuickAddPreset(
                name = "IReader Official",
                url = "https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repo/index.min.json",
                owner = "IReaderorg",
                source = "https://github.com/IReaderorg/IReader-extensions",
                type = RepositoryType.IREADER
            ),
            QuickAddPreset(
                name = "LNReader Plugins",
                url = "https://raw.githubusercontent.com/kazemcodes/lnreader-plugins-unminified/refs/heads/repo/plugins/plugins.min.json",
                owner = "LNReader",
                source = "https://github.com/kazemcodes/lnreader-plugins-unminified",
                type = RepositoryType.LNREADER
            )
        )
    }
    
    var showQuickAddDialog by remember { mutableStateOf(false) }
    
    // Validation function
    fun validateFields(): Boolean {
        var isValid = true
        
        if (name.value.isBlank()) {
            nameError = "Name is required"
            isValid = false
        } else {
            nameError = null
        }
        
        if (url.value.isBlank()) {
            urlError = "URL is required"
            isValid = false
        } else if (!url.value.startsWith("http://") && !url.value.startsWith("https://")) {
            urlError = "URL must start with http:// or https://"
            isValid = false
        } else {
            urlError = null
        }
        
        return isValid
    }
    Scaffold(
            modifier = Modifier.fillMaxSize().padding(top = scaffoldPadding.calculateTopPadding()),
            topBar = {
                TopAppBar(
                    title = { Text("Add Repository") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        IconButton(
                            onClick = { showHelpDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = localizeHelper.localize(Res.string.help),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                        onClick = {
                            if (validateFields() && !isLoading) {
                                isLoading = true
                                onSave(RepositoryInfo(
                                        name = name.value.trim(),
                                        key = url.value.trim(),
                                        owner = owner.value.trim(),
                                        source = source.value.trim(),
                                        username = username.value.trim(),
                                        password = password.value,
                                        repositoryType = repositoryType.name
                                ))
                                // Reset loading state after a delay (in real app, this would be in callback)
                                isLoading = false
                            }
                        },
                        icon = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        },
                        text = { Text(if (isLoading) "Saving..." else "Save Repository") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        expanded = !isLoading
                )
            },
    ) { padding ->

        LazyColumn(
                modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Add Button
            item {
                OutlinedButton(
                    onClick = { showQuickAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Add Popular Repositories")
                }
            }
            
            // Divider with "OR"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }
            
            // Repository Type Selector
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Repository Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = repositoryType == RepositoryType.IREADER,
                            onClick = { repositoryType = RepositoryType.IREADER },
                            label = { Text("IReader") },
                            leadingIcon = if (repositoryType == RepositoryType.IREADER) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilterChip(
                            selected = repositoryType == RepositoryType.LNREADER,
                            onClick = { repositoryType = RepositoryType.LNREADER },
                            label = { Text("LNReader") },
                            leadingIcon = if (repositoryType == RepositoryType.LNREADER) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Type info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when (repositoryType) {
                                        RepositoryType.IREADER -> "IReader format repositories contain extensions and plugins"
                                        RepositoryType.LNREADER -> "LNReader format repositories are compatible with LNReader plugins"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Warning about mixing repository types
                            if (repositoryType == RepositoryType.LNREADER) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Important: Uninstall all IReader extensions before adding LNReader repositories to avoid conflicts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Required fields section
            item {
                Text(
                    text = "Required Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                EnhancedFormComponent(
                    label = localizeHelper.localize(Res.string.name),
                    icon = Icons.Default.Badge,
                    state = name,
                    error = nameError,
                    placeholder = "My Repository",
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }
            
            item {
                EnhancedFormComponent(
                    label = localizeHelper.localize(Res.string.url),
                    icon = Icons.Default.Link,
                    state = url,
                    error = urlError,
                    placeholder = "https://example.com/repo.json",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }
            
            // Optional fields section
            item {
                Text(
                    text = "Optional Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                EnhancedFormComponent(
                    label = localizeHelper.localize(Res.string.owner),
                    icon = Icons.Default.Copyright,
                    state = owner,
                    placeholder = "Repository owner",
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }
            
            item {
                EnhancedFormComponent(
                    label = localizeHelper.localize(Res.string.source),
                    icon = Icons.Default.HideSource,
                    state = source,
                    placeholder = "Source identifier",
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            if (showPasswordFields) {
                                focusManager.moveFocus(FocusDirection.Down)
                            } else {
                                focusManager.clearFocus()
                            }
                        }
                    )
                )
            }
            
            // Authentication section (collapsible)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Authentication (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { showPasswordFields = !showPasswordFields }) {
                        Icon(
                            imageVector = if (showPasswordFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showPasswordFields) "Hide" else "Show"
                        )
                    }
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = showPasswordFields,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EnhancedFormComponent(
                            label = localizeHelper.localize(Res.string.username),
                            icon = Icons.Default.Person,
                            state = username,
                            placeholder = "Username",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                        
                        EnhancedFormComponent(
                            label = localizeHelper.localize(Res.string.password),
                            icon = Icons.Default.Key,
                            state = password,
                            placeholder = "Password",
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }
                }
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Quick Add Dialog
    if (showQuickAddDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Quick Add Repository",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickAddPresets.size) { index ->
                        val preset = quickAddPresets[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                name.value = preset.name
                                url.value = preset.url
                                owner.value = preset.owner
                                source.value = preset.source
                                repositoryType = preset.type
                                showQuickAddDialog = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (preset.type == RepositoryType.IREADER) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = preset.type.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = preset.owner,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickAddDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
    
    // Help dialog with enhanced Material Design 3 styling
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = localizeHelper.localize(Res.string.what_is_repository),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.repository_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Example URL:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.repository_example_url),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHelpDialog = false }
                ) {
                    Text("Got it")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFormComponent(
        label: String,
        icon: ImageVector,
        state: MutableState<String>,
        error: String? = null,
        placeholder: String = "",
        isPassword: Boolean = false,
        passwordVisible: Boolean = false,
        onPasswordVisibilityToggle: (() -> Unit)? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        enable: Boolean = true
) {
    OutlinedTextField(
            value = state.value,
            onValueChange = { state.value = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (error != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
                {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            } else null,
            enabled = enable,
            label = {
                Text(text = label)
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            isError = error != null,
            supportingText = if (error != null) {
                {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
    )
}

@Deprecated("Use EnhancedFormComponent instead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComponent(
        name: String,
        icon: ImageVector,
        state: MutableState<String>,
        enable: Boolean = true
) {
    OutlinedTextField(
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

enum class RepositoryType {
    IREADER,
    LNREADER
}

data class QuickAddPreset(
    val name: String,
    val url: String,
    val owner: String,
    val source: String,
    val type: RepositoryType
)

