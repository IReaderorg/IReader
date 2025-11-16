/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import ireader.core.BuildConfig
import ireader.core.log.Log
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    val expectedAuthority = BuildConfig.LIBRARY_PACKAGE_NAME + ".provider"
    
    // Verify FileProvider configuration
    verifyFileProviderConfiguration(context, expectedAuthority)
    
    return FileProvider.getUriForFile(context, expectedAuthority, this)
  } else {
    return Uri.fromFile(this)
  }
}

/**
 * Verifies that the FileProvider is correctly configured in AndroidManifest.xml
 * 
 * @param context context of application
 * @param expectedAuthority the expected provider authority
 * @throws IllegalStateException if the provider configuration is invalid
 */
private fun verifyFileProviderConfiguration(context: Context, expectedAuthority: String) {
  try {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(
      context.packageName,
      PackageManager.GET_PROVIDERS
    )
    
    val providers = packageInfo.providers
    if (providers == null) {
      throw IllegalStateException(
        "FileProvider configuration error: No providers found in AndroidManifest.xml.\n" +
        "Fix: Add FileProvider to AndroidManifest.xml with authority '$expectedAuthority'"
      )
    }
    
    // Find the FileProvider
    val fileProvider = providers.find { provider ->
      provider.name == "androidx.core.content.FileProvider"
    }
    
    if (fileProvider == null) {
      throw IllegalStateException(
        "FileProvider configuration error: androidx.core.content.FileProvider not found in AndroidManifest.xml.\n" +
        "Fix: Add the following to your AndroidManifest.xml:\n" +
        "<provider\n" +
        "    android:name=\"androidx.core.content.FileProvider\"\n" +
        "    android:authorities=\"$expectedAuthority\"\n" +
        "    android:exported=\"false\"\n" +
        "    android:grantUriPermissions=\"true\" />"
      )
    }
    
    val actualAuthority = fileProvider.authority
    
    if (actualAuthority != expectedAuthority) {
      throw IllegalStateException(
        "FileProvider authority mismatch!\n" +
        "Expected: $expectedAuthority\n" +
        "Actual: $actualAuthority\n" +
        "Fix: Update provider authority in AndroidManifest.xml to match '$expectedAuthority'\n" +
        "Change android:authorities=\"$actualAuthority\" to android:authorities=\"$expectedAuthority\""
      )
    }
    
    // Log successful verification
    Log.debug("FileProvider configuration verified successfully: authority=$actualAuthority")
    
  } catch (e: PackageManager.NameNotFoundException) {
    Log.error(e, "Failed to verify FileProvider configuration: package not found")
    throw IllegalStateException(
      "FileProvider configuration error: Unable to read package information.\n" +
      "This should not happen in normal circumstances.",
      e
    )
  } catch (e: IllegalStateException) {
    // Re-throw IllegalStateException with our custom messages
    Log.error(e.message ?: "FileProvider configuration error")
    throw e
  } catch (e: Exception) {
    // Catch any other unexpected errors
    Log.error(e, "Unexpected error during FileProvider verification")
    throw IllegalStateException(
      "FileProvider configuration error: Unexpected error during verification.\n" +
      "Error: ${e.message}",
      e
    )
  }
}
fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
}
