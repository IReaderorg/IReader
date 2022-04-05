package org.ireader.presentation.feature_settings.presentation.appearance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.ThemeMode
import org.ireader.domain.view_models.settings.apperance.MainViewModel
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel(),
) {

    val openDialog = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text = "Appearance")
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                navigationIcon = {
                    TopAppBarBackButton(navController = navController)
                }
            )
        }
    ) {
        ListItem(modifier = Modifier.clickable {
            openDialog.value = true
        }) {
            Row {
                Icon(imageVector = Icons.Default.ModeNight,
                    contentDescription = "Night Mode",
                    tint = MaterialTheme.colors.primary)
                Spacer(modifier = Modifier.width(16.dp))
                MidSizeTextComposable(text = "Dark Mode")
            }

        }

        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    BigSizeTextComposable(text = "Night Mode")
                },
                buttons = {
                    Column(modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        val items = listOf(
                            AppearanceItems.Day,
                            AppearanceItems.Night,
                            AppearanceItems.Auto,
                        )
                        items.forEach { item ->
                            TextButton(onClick = {
                                viewModel.saveNightModePreferences(item.appTheme)
                                openDialog.value = false
                            }) {
                                MidSizeTextComposable(modifier = modifier.fillMaxWidth(),
                                    text = item.text,
                                    align = TextAlign.Start)
                            }
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
            )
        }


    }
}

sealed class AppearanceItems(val text: String, val appTheme: ThemeMode) {
    object Day : AppearanceItems("Off", ThemeMode.Light)
    object Night : AppearanceItems("On", ThemeMode.Dark)
    object Auto : AppearanceItems("Auto", ThemeMode.System)
}