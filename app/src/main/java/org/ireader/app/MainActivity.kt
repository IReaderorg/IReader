package org.ireader.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.anggrayudi.storage.file.StorageId.PRIMARY
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.presentation.core.ScreenContent
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity(), SecureActivityDelegate by SecureActivityDelegateImpl() {
    private val getSimpleStorage: GetSimpleStorage = get()
    private val uiPreferences: UiPreferences by inject()
    private val automaticBackup: AutomaticBackup =
        get(parameters = { org.koin.core.parameter.parametersOf(this@MainActivity) })
    val initializers: AppInitializers = get<AppInitializers>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this, uiPreferences)
        getSimpleStorage.provideActivity(this, null)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        setContent {
            AppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,

                    ) {
                    ScreenContent()
                    GetPermissions(this,getSimpleStorage)
                }
            }
        }
    }
}

@Composable
fun GetPermissions(context: Context, getSimpleStorage: GetSimpleStorage) {

    var isPermissionGranted by remember {
        mutableStateOf(getSimpleStorage.storage.isStorageAccessGranted(PRIMARY))
    }


    if (!isPermissionGranted) {
        AlertDialog(onDismissRequest = { }, title = {
            BigSizeTextComposable(text = context.getString(R.string.permissions))
        }, text = {
            Column {
                MidSizeTextComposable(
                    text = "In order to get sources from your phone," +
                            " the app need full access to storage."
                )
                MidSizeTextComposable(
                    text = "Do you want to open the permission screen?"
                )
            }

        }, confirmButton = {
            TextButton(onClick = {
                getSimpleStorage.checkPermission().let { granted ->
                    isPermissionGranted = granted
                }
            }) {
                MidSizeTextComposable(text = context.getString(R.string.check_permissions))
            }
        })
    }

}

interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences)
}
