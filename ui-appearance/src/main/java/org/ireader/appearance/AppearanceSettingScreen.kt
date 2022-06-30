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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.components.components.Components
import org.ireader.components.components.SetupSettingComponents
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.ChoicePreference
import org.ireader.components.components.component.ColorPreference
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.Theme
import org.ireader.core_ui.theme.dark
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.light
import org.ireader.ui_appearance.R

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    saveDarkModePreference: (PreferenceValues.ThemeMode) -> Unit,
    vm: AppearanceViewModel,
    scaffoldPaddingValues: PaddingValues
) {
    val context = LocalContext.current
    val customizedColors = vm.getCustomizedColors().value
    val isLight = vm.themeMode.value == PreferenceValues.ThemeMode.Light

    val themesForCurrentMode = remember(isLight,vm.vmThemes.size) {
        if (isLight)
            vm.vmThemes.map { it.light() }
        else
            vm.vmThemes.map { it.dark() }
    }


    val items : List<Components> = remember {
        listOf<Components>(
            Components.Header(
                text = "Theme",
            ),
            Components.Dynamic {
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
            },
            Components.Header(
                text = "Preset themes",
            ),
            Components.Dynamic {
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
            },
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.primaryState,
                    title = "Color primary",
                    subtitle = "Displayed most frequently across your app",
                    unsetColor = MaterialTheme.colorScheme.primary
                )
            },
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.secondaryState,
                    title = "Color secondary",
                    subtitle = "Accents select parts of the UI",
                    unsetColor = MaterialTheme.colorScheme.secondary
                )
            },
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.barsState,
                    title = "Toolbar color",
                    unsetColor = AppColors.current.bars
                )
            },
            Components.Header(
                text = "Timestamp",
            ),
            Components.Dynamic {
                ChoicePreference(
                    preference = vm.relativeTime,
                    choices = vm.relativeTimes.associateWith { value ->
                        when (value) {
                            PreferenceValues.RelativeTime.Off -> context.getString(R.string.off)
                            PreferenceValues.RelativeTime.Day -> context.getString(R.string.pref_relative_time_short)
                            PreferenceValues.RelativeTime.Week -> context.getString(R.string.pref_relative_time_long)
                            else -> context.getString(R.string.off)
                        }
                    },
                    title = stringResource(id = R.string.pref_relative_format),
                    subtitle = null,
                )
            },
        )
    }


    SetupSettingComponents(scaffoldPadding = scaffoldPaddingValues, items = items)
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
                Text("Text", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
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
