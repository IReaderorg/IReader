package org.ireader.data.catalog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import org.ireader.core_api.log.Log
import org.ireader.core_api.os.PackageInstaller
import java.io.File
import javax.inject.Inject

/**
 * Activity used to install extensions, because we can only receive the result of the installation
 * with [startActivityForResult], which we need to update the UI.
 * I Used ComponentActivity instead of Activity because hilt doesn't support Activity
 */
@AndroidEntryPoint
class ExtensionInstallActivity : ComponentActivity() {

    @Inject lateinit var androidCatalogInstallationChanges: AndroidCatalogInstallationChanges
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            try {
                intent?.getStringExtra(PackageInstaller.EXTRA_PKG_NAME)
                    ?.let {
                        androidCatalogInstallationChanges.notifyAppInstall(it)
                    }
                intent?.getStringExtra(PackageInstaller.EXTRA_FILE_PATH)?.let {
                    File(it).deleteRecursively()
                }
            } catch (e: Exception) {
                Log.error(e, "Legacy Package Installer throws an error")
            }

            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            .setDataAndType(intent.data, intent.type)
            .putExtra(Intent.EXTRA_RETURN_RESULT, true)
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            resultLauncher.launch(installIntent)
        } catch (error: Exception) {
            kotlin.runCatching {
                intent.getStringExtra(PackageInstaller.FILE_SCHEME)?.let {
                    File(it).deleteRecursively()
                }
            }

            Log.error(error,"Legacy installer failed to start package manager")
            finish()
        }
    }
}

