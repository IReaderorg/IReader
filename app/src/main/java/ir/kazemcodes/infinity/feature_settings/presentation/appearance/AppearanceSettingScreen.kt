package ir.kazemcodes.infinity.feature_settings.presentation.appearance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.NightMode
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidSizeTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_settings.presentation.setting.SettingViewModel
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppearanceSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
    navController: NavController,
) {


    val openDialog = viewModel.state.value.dialogState
    val pref = get<PreferencesUseCase>()
    val context = LocalContext.current


    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(title = "Appearance")
                },
                modifier = Modifier.fillMaxWidth(),
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
            viewModel.toggleDialog(true)
        }) {
            Row() {
                Icon(imageVector = Icons.Default.ModeNight, contentDescription = "Night Mode", tint = MaterialTheme.colors.primary)
                Spacer(modifier = Modifier.width(16.dp))
                MidSizeTextComposable(title = "Dark Mode")
            }

        }

        if (openDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.toggleDialog(false)
                },
                title = {
                    TopAppBarTitle(title = "Night Mode")
                },
                buttons = {
                    Column(modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        TextButton(onClick = {
                            pref.saveNightModePreferences(NightMode.Enable)
                            viewModel.toggleDialog(false)
                            context.toast("Setting was Applied. return to main screen to apply this setting to the app")

                        }) {
                            MidSizeTextComposable(modifier = modifier.fillMaxWidth(),
                                title = "On",
                                align = TextAlign.Start)

                        }
                        TextButton(onClick = {
                            pref.saveNightModePreferences(NightMode.Disable)
                            viewModel.toggleDialog(false)
                            context.toast("Setting was Applied. return to main screen to apply this setting to the app")
                        }) {
                            MidSizeTextComposable(modifier = modifier.fillMaxWidth(),
                                title = "Off",
                                align = TextAlign.Start)
                        }
                        TextButton(onClick = {
                            pref.saveNightModePreferences(NightMode.FollowSystem)
                            viewModel.toggleDialog(false)
                            context.toast("Setting was Applied. return to main screen to apply this setting to the app")
                        }) {
                            MidSizeTextComposable(modifier = modifier.fillMaxWidth(),
                                title = "Follow System",
                                align = TextAlign.Start)
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
            )
        }



    }
}