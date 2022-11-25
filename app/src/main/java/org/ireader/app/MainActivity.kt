package org.ireader.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.ScreenContent
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.core.theme.LocaleHelper
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.util.*


class MainActivity : ComponentActivity(), SecureActivityDelegate by SecureActivityDelegateImpl() {
    private val getSimpleStorage: GetSimpleStorage = get()
    private val uiPreferences: UiPreferences by inject()
    val initializers: AppInitializers = get<AppInitializers>()
    private val automaticBackup: AutomaticBackup = get()
    private val localeHelper: LocaleHelper = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this, uiPreferences)
        getSimpleStorage.provideActivity(this, null)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launchIO {
            automaticBackup.initialize()
        }
        localeHelper.setLocaleLang( this)
        installSplashScreen()
        setContent {
            AppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,

                    ) {
                    ScreenContent()
                    GetPermissions(this,getSimpleStorage,uiPreferences)
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        getSimpleStorage.simpleStorageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        getSimpleStorage.simpleStorageHelper.onRestoreInstanceState(savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Mandatory for Activity, but not for Fragment & ComponentActivity
        getSimpleStorage.simpleStorageHelper.storage.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Mandatory for Activity, but not for Fragment & ComponentActivity
        getSimpleStorage.simpleStorageHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissions(context: Context, getSimpleStorage: GetSimpleStorage,uiPreferences: UiPreferences) {

    val useLocalCache = remember {
        uiPreferences.savedLocalCatalogLocation().get()
    }
    val readStoragePermission = rememberPermissionState(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    ) {
        uiPreferences.savedLocalCatalogLocation().set(!it)
    }
    val writeStoragePermission = rememberPermissionState(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) {
        uiPreferences.savedLocalCatalogLocation().set(!it)
    }
    LaunchedEffect(key1 = true) {
        if (!useLocalCache) {
            readStoragePermission.launchPermissionRequest()
            writeStoragePermission.launchPermissionRequest()
        }
    }
}

interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences)
}
