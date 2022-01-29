package ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
    fun ExtensionCreatorTab(viewModel: ExtensionCreatorViewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                value = viewModel.state.value.extensionFieldValue,
                onValueChange = {
                    viewModel.onFieldStateChange(it)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                }),
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                label = { Text(text = "Please Enter Your Source Here") },
                placeholder = { Text(text = "Please Enter Your Source Here...") },
            )
        }

    }

    @Composable
    fun ExtensionCreatorLogTab(viewModel: ExtensionCreatorViewModel) {
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = viewModel.state.value.errorMessage, modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .align(Alignment.TopCenter))
        }


    }