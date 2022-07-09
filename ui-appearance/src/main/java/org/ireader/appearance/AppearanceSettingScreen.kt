package org.ireader.appearance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.common_models.theme.Theme
import org.ireader.components.components.Components
import org.ireader.components.components.SetupSettingComponents
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.ChoicePreference
import org.ireader.components.components.component.ColorPreference
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.dark
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.light
import org.ireader.domain.use_cases.theme.toCustomTheme
import org.ireader.ui_appearance.R

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

    val scope = rememberCoroutineScope()
    val themesForCurrentMode = remember(isLight, vm.vmThemes.value.size) {
        if (isLight)
            vm.vmThemes.value.map { it.light() }
        else
            vm.vmThemes.value.map { it.dark() }
    }
    val themeItem: Components =
        remember(vm.vmThemes.value.size, vm.themeMode.value, vm.themeEditMode) {
            Components.Dynamic {
                LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(items = themesForCurrentMode) { theme ->
                        ThemeItem(
                            theme,
                            onClick = {
                                vm.colorTheme.value = it.id
                                customizedColors.primaryState.value = it.materialColors.primary
                                customizedColors.secondaryState.value = it.materialColors.secondary
                                customizedColors.barsState.value = it.extraColors.bars
                            },
                            isSelected = vm.colorTheme.value == theme.id,
                            onLongClick = { vm.themeEditMode = true }, editMode = vm.themeEditMode,
                            onDelete = {
                                scope.launch {
                                    vm.vmThemes.value.find { it.id == theme.id }?.toCustomTheme()
                                        ?.let { vm.themeRepository.delete(it) }
                                }
                            }
                        )
                    }
                }
            }
        }

    val items: State<List<Components>> = derivedStateOf {
        listOf<Components>(
            Components.Header(
                text = "Theme",
            ),
            Components.Dynamic {
                ChoicePreference(
                    preference = vm.themeMode,
                    choices = mapOf(
                        PreferenceValues.ThemeMode.System to stringResource(id = R.string.follow_system_settings),
                        PreferenceValues.ThemeMode.Light to stringResource(id = R.string.light),
                        PreferenceValues.ThemeMode.Dark to stringResource(id = R.string.dark)
                    ),
                    title = stringResource(id = R.string.theme),
                    subtitle = null,
                    onValue = {
                        vm.saveNightModePreferences(it)
                        vm.state.value.isSavable = false
                    }
                )
            },
            Components.Header(
                text = "Preset themes",
            ),
            themeItem,
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.primaryState,
                    title = "Color primary",
                    subtitle = "Displayed most frequently across your app",
                    unsetColor = MaterialTheme.colorScheme.primary,
                    onChangeColor = {
                        vm.state.value.isSavable = true
                    },
                    onRestToDefault = {
                        vm.state.value.isSavable = false

                    }
                )
            },
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.secondaryState,
                    title = "Color secondary",
                    subtitle = "Accents select parts of the UI",
                    unsetColor = MaterialTheme.colorScheme.secondary,
                    onChangeColor = {
                        vm.state.value.isSavable = true
                    },
                    onRestToDefault = {
                        vm.state.value.isSavable = false

                    }
                )
            },
            Components.Dynamic {
                ColorPreference(
                    preference = customizedColors.barsState,
                    title = "Toolbar color",
                    unsetColor = AppColors.current.bars,
                    onChangeColor = {
                        vm.state.value.isSavable = true
                    },
                    onRestToDefault = {
                        vm.state.value.isSavable = false

                    }
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

    SetupSettingComponents(scaffoldPadding = scaffoldPaddingValues, items = items.value)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeItem(
    theme: Theme,
    onClick: (Theme) -> Unit,
    onLongClick: (Theme) -> Unit,
    isSelected: Boolean = false,
    editMode: Boolean = false,
    onDelete: (Theme) -> Unit = {},
) {
    val borders = MaterialTheme.shapes.small
    val borderColor = remember {
        if (theme.materialColors.isLight()) {
            Color.Black.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.15f)
        }
    }
    Surface(
        tonalElevation = 4.dp, color = theme.materialColors.background, shape = borders,
        modifier = Modifier
            .size(100.dp, 160.dp)
            .padding(8.dp)
            .border(1.dp, borderColor, borders)
            .combinedClickable(onClick = { onClick(theme) }, onLongClick = { onLongClick(theme) })
    ) {
        Box {
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
                BottomAppBar(
                    Modifier.requiredHeight(24.dp),
                    backgroundColor = theme.extraColors.bars
                ) {
                }
            }
            if (isSelected) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .padding(2.dp),
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "theme is selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (!theme.default && editMode) {
                AppIconButton(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp),
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "delete theme",
                    onClick = { onDelete(theme) }
                )
            }
        }
    }
}
