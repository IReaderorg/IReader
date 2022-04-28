package org.ireader.appearance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_ui.theme.ThemeMode

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    saveDarkModePreference: (ThemeMode) -> Unit
) {

    val openDialog = remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text = "Appearance")
                },
                navigationIcon = {
                    TopAppBarBackButton() {
                        onPopBackStack()
                    }
                }
            )
        }
    ) { padding ->
        ListItem(
            modifier = Modifier.clickable {
                openDialog.value = true
            }
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.ModeNight,
                    contentDescription = "Night Mode",
                    tint = MaterialTheme.colors.primary
                )
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
                    Column(
                        modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val items = listOf(
                            AppearanceItems.Day,
                            AppearanceItems.Night,
                            AppearanceItems.Auto,
                        )
                        items.forEach { item ->
                            TextButton(onClick = {
                                saveDarkModePreference(item.appTheme)
                                openDialog.value = false
                            }) {
                                MidSizeTextComposable(
                                    modifier = modifier.fillMaxWidth(),
                                    text = item.text,
                                    align = TextAlign.Start
                                )
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
