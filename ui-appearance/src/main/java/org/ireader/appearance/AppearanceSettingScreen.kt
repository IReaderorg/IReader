package org.ireader.appearance

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.ChoicePreference
import org.ireader.components.components.component.ColorPreference
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.Theme
import org.ireader.core_ui.theme.dark
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.light
import org.ireader.core_ui.theme.themes
import org.ireader.ui_appearance.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    saveDarkModePreference: (PreferenceValues.ThemeMode) -> Unit,
    vm: AppearanceViewModel
) {
    val customizedColors = vm.getCustomizedColors()
    val isLight = MaterialTheme.colorScheme.isLight()
    val themesForCurrentMode = remember(isLight) {
        if (isLight)
            themes.filter { it.light().materialColors.isLight() == isLight }.map { it.light() }
        else
            themes.filter { it.light().materialColors.isLight() != isLight }.map { it.dark() }
    }


    LazyColumn(modifier =modifier) {
        item {
            ChoicePreference(
                preference = vm.themeMode,
                choices = mapOf(
                    PreferenceValues.ThemeMode.System to stringResource(id =  R.string.follow_system_settings),
                    PreferenceValues.ThemeMode.Light to stringResource(id =R.string.light),
                    PreferenceValues.ThemeMode.Dark to stringResource(id =R.string.dark)
                ),
                title = stringResource(id = R.string.theme),
                subtitle = null,
                onValue = {
                    vm.saveNightModePreferences(it)
                }
            )
        }
        item {
            Text(
                "Preset themes",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                items(items = themesForCurrentMode) { theme ->
                    ThemeItem(theme, onClick = {
                        vm.colorTheme.value = it.id
                        customizedColors.primaryState.value = it.materialColors.primary
                        customizedColors.secondaryState.value = it.materialColors.secondary
                        customizedColors.barsState.value = it.extraColors.bars
                    })
                }
            }
        }
        item {
            ColorPreference(
                preference = customizedColors.primaryState,
                title = "Color primary",
                subtitle = "Displayed most frequently across your app",
                unsetColor = MaterialTheme.colorScheme.primary
            )
        }
        item {
            ColorPreference(
                preference = customizedColors.secondaryState,
                title = "Color secondary",
                subtitle = "Accents select parts of the UI",
                unsetColor = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            ColorPreference(
                preference = customizedColors.barsState,
                title = "Toolbar color",
                unsetColor = AppColors.current.bars
            )
        }
    }
}

@Composable
private fun ThemeItem(
    theme: Theme,
    onClick: (Theme) -> Unit
) {
    val borders = MaterialTheme.shapes.small
    val borderColor = if (theme.materialColors.isLight()) {
        Color.Black.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }
    Surface(
        tonalElevation = 4.dp, color = theme.materialColors.background, shape = borders,
        modifier = Modifier
            .size(100.dp, 160.dp)
            .padding(8.dp)
            .border(1.dp, borderColor, borders)
            .clickable(onClick = { onClick(theme) })
    ) {
        Column(
            modifier = Modifier.border(1.dp, borderColor, borders)
        ) {
            Toolbar(
                modifier = Modifier.requiredHeight(24.dp), title = {},
                backgroundColor = theme.extraColors.bars
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(6.dp)
            ) {
                Text("Text", fontSize = 11.sp)
                Button(
                    onClick = {},
                    enabled = false,
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp, 20.dp),
                    content = {},
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = theme.materialColors.primary
                    )
                )
                Surface(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd),
                    shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                    color = theme.materialColors.secondary,
                    tonalElevation = 6.dp,
                    content = { }
                )
            }
            BottomAppBar(Modifier.requiredHeight(24.dp), backgroundColor = theme.extraColors.bars) {
            }
        }
    }
}
