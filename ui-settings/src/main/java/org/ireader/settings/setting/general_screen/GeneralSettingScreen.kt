package org.ireader.settings.setting.general_screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.components.components.Components
import org.ireader.components.components.SetupUiComponent
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.ui_settings.R
import javax.inject.Inject

@Composable
fun GeneralSettingScreen(
    scaffoldPadding: PaddingValues,
    vm : GeneralSettingScreenViewModel
) {
    val context = LocalContext.current
    val items = remember {
        listOf<Components>(
            Components.Switch(
                preference = vm.defaultImageLoader,
                title = context.getString(R.string.use_default_image_loader)
            ),
        )
    }

    LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
    ) {
        SetupUiComponent(items)
    }
}

@HiltViewModel
class GeneralSettingScreenViewModel @Inject constructor(private val appPreferences: AppPreferences) : BaseViewModel() {

    val defaultImageLoader = appPreferences.defaultImageLoader().asState()



}


