//package ireader.presentation.core.ui
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import cafe.adriel.voyager.core.screen.Screen
//import cafe.adriel.voyager.navigator.LocalNavigator
//import cafe.adriel.voyager.navigator.currentOrThrow
//import ireader.domain.usecases.translate.WebscrapingTranslateEngine
//import ireader.i18n.localize
//import ireader.i18n.resources.Res
import ireader.i18n.resources.*
//import ireader.presentation.ui.component.IScaffold
//import ireader.presentation.ui.component.components.Components
//import ireader.presentation.ui.core.theme.LocalLocalizeHelper
//import org.koin.compose.koinInject
//
///**
// * Screen for configuring Gemini API Key
// */
//class GeminiApiSettingsScreenSpec : Screen {
//
//    override val key: String = "gemini_api_settings_screen"
//
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        val engine = koinInject<WebscrapingTranslateEngine>()
//        val localizeHelper = LocalLocalizeHelper.currentOrThrow
//
//        // Set the current AI service to Gemini
//        engine.setAIService(WebscrapingTranslateEngine.AI_SERVICE.GEMINI)
//
//        // Get the current API key
//        val currentApiKey = engine.getCookies()
//        var apiKey by rememberSaveable { mutableStateOf(currentApiKey) }
//
//        // UI state
//        var saveSuccess by remember { mutableStateOf(false) }
//        var isSaving by remember { mutableStateOf(false) }
//
//        IScaffold(
//            topBar = { scrollBehavior ->
//                ireader.presentation.ui.component.ScrollableAppBar(
//                    title = localizeHelper.localize(Res.string.gemini_api_key),
//                    navigateBack = {
//                        navigator.pop()
//                    },
//                    scrollBehavior = scrollBehavior,
//                )
//            }
//        ) { padding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding)
//                    .verticalScroll(rememberScrollState())
//                    .padding(16.dp)
//            ) {
//                // Instructions
//                Text(
//                    text = "Enter your Google AI Studio API Key for Gemini. You can obtain a key from https://aistudio.google.com/app/apikey",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // API Key input
//                OutlinedTextField(
//                    value = apiKey,
//                    onValueChange = { apiKey = it },
//                    label = { Text("Gemini API Key") },
//                    modifier = Modifier.fillMaxWidth(),
//                    visualTransformation = PasswordVisualTransformation(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Save button
//                Components.Button(
//                    onClick = {
//                        isSaving = true
//                        // Save the API key
//                        engine.saveCookies(apiKey)
//                        saveSuccess = true
//                        isSaving = false
//                    },
//                    text = localizeHelper.localize(Res.string.save),
//                    isLoading = isSaving
//                ).Build()
//
//                // Success message
//                if (saveSuccess) {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "API key saved successfully!",
//                        color = MaterialTheme.colorScheme.primary,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Test button if API key is set
//                if (apiKey.isNotEmpty()) {
//                    Components.Button(
//                        onClick = {
//                            // Implement a test functionality if needed
//                        },
//                        text = "Test API Key",
//                    ).Build()
//                }
//            }
//        }
//    }
//}