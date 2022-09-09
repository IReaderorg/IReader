package ireader.ui.appearance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.common.models.theme.Theme
import ireader.core.ui.preferences.PreferenceValues
import ireader.core.ui.theme.AppColors
import ireader.core.ui.theme.isLight
import ireader.domain.use_cases.theme.toCustomTheme
import ireader.ui.component.components.Components
import ireader.ui.component.components.SetupSettingComponents
import ireader.ui.component.components.Toolbar
import ireader.ui.component.components.component.ChoicePreference
import ireader.ui.component.components.component.ColorPreference
import ireader.ui.component.reusable_composable.AppIconButton
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    saveDarkModePreference: (PreferenceValues.ThemeMode) -> Unit,
    vm: AppearanceViewModel,
    scaffoldPaddingValues: PaddingValues
) {
    val context = LocalContext.current
    val customizedColors = vm.getCustomizedColors()
    val systemTheme = isSystemInDarkTheme()
    val isLight = remember(vm.themeMode.value) {
        if(vm.themeMode.value == PreferenceValues.ThemeMode.System) {
            !systemTheme
        } else  {
            vm.themeMode.value == PreferenceValues.ThemeMode.Light
        }
    }

    val scope = rememberCoroutineScope()
    val themesForCurrentMode = remember(vm.themeMode.value, vm.vmThemes.size,isLight) {
        if (isLight)
            vm.vmThemes.filter { !it.isDark }
        else
            vm.vmThemes.filter { it.isDark }
    }
    val themeItem: Components =
        remember(vm.vmThemes.size, vm.themeMode.value, vm.themeEditMode,themesForCurrentMode) {
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
                                vm.isSavable = false
                            },
                            isSelected = vm.colorTheme.value == theme.id,
                            onLongClick = { vm.themeEditMode = true }, editMode = vm.themeEditMode,
                            onDelete = {
                                scope.launch {
                                    vm.vmThemes.find { it.id == theme.id }?.toCustomTheme()
                                        ?.let { vm.themeRepository.delete(it) }
                                }
                            }
                        )
                    }
                }
            }
        }

    val items: State<List<Components>> = remember {
        derivedStateOf {
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
                            vm.isSavable = true
                        },
                        onRestToDefault = {
                            vm.isSavable = false
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
                            vm.isSavable = true
                        },
                        onRestToDefault = {
                            vm.isSavable = false
                        }
                    )
                },
                Components.Dynamic {
                    ColorPreference(
                        preference = customizedColors.barsState,
                        title = "Toolbar color",
                        unsetColor = AppColors.current.bars,
                        onChangeColor = {
                            vm.isSavable = true
                        },
                        onRestToDefault = {
                            vm.isSavable = false
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
    }


    SetupSettingComponents(scaffoldPadding = scaffoldPaddingValues, items = items.value)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
                    Text("Text", fontSize = 11.sp, color = theme.materialColors.onBackground)
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
            if ((theme.id > 0) && editMode) {
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
