/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.os


import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.widget.Toast
import ireader.core.log.Log
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PackageInstaller(
  private val context: Application,
  private val localizeHelper: LocalizeHelper
) {
  private val packageInstaller = context.packageManager.packageInstaller

  @Suppress("BlockingMethodInNonBlockingContext")
  suspend fun install(file: File, packageName: String): InstallStep {
    return withInstallReceiver { sender ->
      withContext(Dispatchers.IO) {
        try {
          // Make sure the file is readable and has appropriate attributes
          if (!file.exists() || !file.canRead()) {
            val errorMsg = localizeHelper.localize(Res.string.file_not_found_error)
            // Show a toast to make the error more visible
            withContext(Dispatchers.Main) {
              Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
            Log.error("File does not exist or can't be read: ${file.absolutePath}")
            InstallStep.Error(errorMsg)
            return@withContext
          }
          
          // Log file details to aid debugging
          Log.info("Preparing to install: ${file.absolutePath}")
          Log.info("File size: ${file.length()} bytes")
          Log.info("File can read: ${file.canRead()}")
          Log.info("File can write: ${file.canWrite()}")
          Log.info("Package name: $packageName")
          
          // Create the installer params
          val installParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
          
          // Set user action requirement based on Android version
          if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            // On Android 12+, always require user action for security
            installParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
          }
          
          // Set package info
          val fileSize = file.length()
          installParams.setAppPackageName(packageName)
          installParams.setSize(fileSize)
          
          try {
            // Create and use the installation session
            Log.info("Creating installation session")
            val sessionId = packageInstaller.createSession(installParams)
            Log.info("Created session ID: $sessionId")
            
            packageInstaller.openSession(sessionId).use { session ->
              try {
                Log.info("Opening write stream")
                val output = session.openWrite(packageName, 0, fileSize)
                output.use {
                  Log.info("Writing APK data to session")
                  file.inputStream().use { input ->
                    val buffer = ByteArray(65536)
                    var bytesRead: Int
                    var totalBytesWritten = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } > 0) {
                      output.write(buffer, 0, bytesRead)
                      totalBytesWritten += bytesRead
                      
                      // Log progress periodically
                      if (totalBytesWritten % 1048576 == 0L) { // Log every 1MB
                        Log.info("Written $totalBytesWritten bytes (${(totalBytesWritten * 100 / fileSize)}%)")
                      }
                    }
                  }
                  
                  Log.info("Fsyncing data")
                  session.fsync(output)
                  Log.info("Fsync completed")
                }
                
                try {
                  Log.info("Starting to commit session")
                  withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Committing installation session...", Toast.LENGTH_SHORT).show()
                  }
                  
                  // Check file permissions before committing
                  if (!file.canRead()) {
                    Log.error("File is not readable before commit")
                  }
                  
                  // Commit with a try-catch to handle specific exceptions
                  try {
                    session.commit(sender)
                    Log.info("Session committed successfully")
                  } catch (e: SecurityException) {
                    Log.error("Security exception during commit: ${e.message}", e)
                    val errorMsg = "Security exception: ${e.message}"
                    withContext(Dispatchers.Main) {
                      Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                    InstallStep.Error(errorMsg)
                    return@withContext
                  } catch (e: IllegalArgumentException) {
                    val errorMsg = "Invalid argument: ${e.message}"
                    Log.error("Invalid argument during commit: ${e.message}", e)
                    // For Android 14, add extra information about the specific error
                    if (Build.VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                      Log.error("This error often occurs on Android 14+ if the APK file has issues or the PendingIntent configuration is incorrect")
                      // Try manually starting the installation
                      withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Attempting alternative installation method...", Toast.LENGTH_SHORT).show()
                      }
                      try {
                        // Copy file to a location that is accessible by FileProvider
                        val downloadDir = File(context.getExternalFilesDir(null), "downloads")
                        downloadDir.mkdirs()
                        
                        val copyFile = File(downloadDir, "${file.name}")
                        Log.info("Copying APK to: ${copyFile.absolutePath}")
                        
                        file.inputStream().use { input ->
                          copyFile.outputStream().use { output ->
                            input.copyTo(output)
                          }
                        }
                        
                        // Create a FileProvider URI for the copied file
                        val authority = "${context.packageName}.provider"
                        val fileUri = androidx.core.content.FileProvider.getUriForFile(context, authority, copyFile)
                        
                        Log.info("Generated URI: $fileUri")
                        
                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                          setDataAndType(fileUri, "application/vnd.android.package-archive")
                          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        Log.info("Starting installation activity with intent: $installIntent")
                        context.startActivity(installIntent)
                        
                        InstallStep.Downloading // We've started a manual installation
                        return@withContext
                      } catch (fallbackException: Exception) {
                        Log.error("Fallback installation method also failed: ${fallbackException.message}", fallbackException)
                      }
                    }
                    
                    withContext(Dispatchers.Main) {
                      Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                    InstallStep.Error(errorMsg)
                    return@withContext
                  }
                } catch (e: Exception) {
                  Log.error("Failed to commit session", e.message)
                  val errorMsg = "Session commit failed: ${e.message}"
                  withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                  }
                  InstallStep.Error(errorMsg)
                  return@withContext
                }
              } catch (e: Exception) {
                Log.error("Session operation failed", e)
                val errorMsg = "Session operation failed: ${e.message}"
                withContext(Dispatchers.Main) {
                  Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
                InstallStep.Error(errorMsg)
                return@withContext
              }
            }
          } catch (e: Exception) {
            Log.error("Failed to create or use session", e.message)
            val errorMsg = "Failed to create or use session: ${e.message}"
            withContext(Dispatchers.Main) {
              Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
            InstallStep.Error(errorMsg)
            return@withContext
          }
          
          // If we reach here, we've initiated the installation process
          Log.info("Installation process initiated, waiting for user confirmation")
          InstallStep.Downloading
        } catch (e: Exception) {
          Log.error("Failed to install package", e.message)
          val errorMsg = localizeHelper.localize(Res.string.installation_error) + ": ${e.message}"
          
          withContext(Dispatchers.Main) {
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
          }
          
          InstallStep.Error(errorMsg)
        }
      }
    }
  }

  suspend fun uninstall(packageName: String): InstallStep {
    return withInstallReceiver { sender ->
      packageInstaller.uninstall(packageName, sender)
    }
  }

  private suspend fun withInstallReceiver(block: suspend (IntentSender) -> Unit): InstallStep {
    val deferred = CompletableDeferred<InstallStep>()
    val receiver = InstallResultReceiver(context, deferred, localizeHelper)
    val uid = SystemClock.elapsedRealtime()
    val action = "core-api.INSTALL_APK_$uid"
    
    // Create an explicit intent targeting our own app
    val intent = Intent(action).apply {
      setPackage(context.packageName)
    }
    
    // For Android 14 (UPSIDE_DOWN_CAKE), we need FLAG_MUTABLE for commit() to work
    val flags = if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
      PendingIntent.FLAG_MUTABLE
    } else {
     0
    }
    
    val broadcast = PendingIntent.getBroadcast(
      context,
      0,
      intent,
      flags
    )
    
    val packageInstallerCallBack = object : PackageInstaller.SessionCallback() {
      override fun onCreated(p0: Int) {
        Log.info("Session created: $p0")
      }

      override fun onBadgingChanged(p0: Int) {}

      override fun onActiveChanged(p0: Int, p1: Boolean) {
        Log.info("Session active changed: $p0, active: $p1")
      }

      override fun onProgressChanged(p0: Int, p1: Float) {
        Log.info("Session progress: $p0, progress: $p1")
      }

      override fun onFinished(p0: Int, result: Boolean) {
        Log.info("Session finished: $p0, result: $result")
        when (result) {
          true -> deferred.complete(InstallStep.Success)
          else -> deferred.complete(InstallStep.Idle)
        }
      }
    }
    
    packageInstaller.registerSessionCallback(packageInstallerCallBack)
    
    // Register receiver with appropriate flags for Android 14+ compatibility
    if (Build.VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
      context.registerReceiver(receiver, IntentFilter(action), Context.RECEIVER_NOT_EXPORTED)
    } else {
      context.registerReceiver(receiver, IntentFilter(action))
    }
    
    return try {
      block(broadcast.intentSender)
      deferred.await()
    } finally {
      try {
        context.unregisterReceiver(receiver)
      } catch (e: Exception) {
        Log.warn("Failed to unregister receiver", e)
      }
      broadcast.cancel()
      packageInstaller.unregisterSessionCallback(packageInstallerCallBack)
    }
  }

  private class InstallResultReceiver(
    val context: Application,
    val deferred: CompletableDeferred<InstallStep>,
    private val localizeHelper: LocalizeHelper
  ) : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
      intent ?: return
      val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
      val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Unknown status"
      
      Log.info("Received installation status: $status, message: $statusMessage")
      
      when (status) {
        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
          val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
          if (confirmationIntent == null) {
            Log.warn { "Fatal error for $intent - no confirmation intent" }
            val errorMessage = localizeHelper.localize(Res.string.fatal_error)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            deferred.complete(InstallStep.Error(errorMessage))
            return
          }
          
          // Log the confirmation intent details for debugging
          Log.info("Confirmation intent: $confirmationIntent")
          Log.info("Confirmation intent action: ${confirmationIntent.action}")
          Log.info("Confirmation intent component: ${confirmationIntent.component}")
          Log.info("Confirmation intent categories: ${confirmationIntent.categories}")
          
          // Add necessary flags for the confirmation intent
          confirmationIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or 
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_ACTIVITY_CLEAR_TOP
          )
          
          try {
            Log.info("Starting installation confirmation activity")
            
            // On Android 14+ (API 34+), we need to make sure the intent is explicit
            if (Build.VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
              // If component is null, we need to make this intent explicit
              if (confirmationIntent.component == null) {
                // Try to resolve the intent to find a matching activity
                val resolveInfo = context.packageManager.resolveActivity(confirmationIntent, 0)
                if (resolveInfo != null && resolveInfo.activityInfo != null) {
                  val componentName = ComponentName(
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name
                  )
                  Log.info("Setting explicit component: $componentName")
                  confirmationIntent.component = componentName
                }
              }
            }
            
            // Start the activity
            context.startActivity(confirmationIntent)
            
            // We don't complete the deferred here because the user still needs to confirm
            // The final result will come in a separate broadcast
          } catch (e: Throwable) {
            Log.warn("Error while showing installation dialog", e)
            val errorMessage = localizeHelper.localize(Res.string.installation_error) + ": " + e.message
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            deferred.complete(InstallStep.Error(errorMessage))
          }
        }
        PackageInstaller.STATUS_SUCCESS -> {
          Log.info("Installation successful")
          Toast.makeText(
            context, 
            localizeHelper.localize(Res.string.installation_successful), 
            Toast.LENGTH_SHORT
          ).show()
          deferred.complete(InstallStep.Success)
        }
        PackageInstaller.STATUS_FAILURE_ABORTED -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_aborted)
          Log.warn("Installation aborted: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        PackageInstaller.STATUS_FAILURE_BLOCKED -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_blocked)
          Log.warn("Installation blocked: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        PackageInstaller.STATUS_FAILURE_CONFLICT -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_conflicted)
          Log.warn("Installation conflict: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_incompatible)
          Log.warn("Installation incompatible: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        PackageInstaller.STATUS_FAILURE_STORAGE -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_Storage)
          Log.warn("Installation storage error: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        PackageInstaller.STATUS_FAILURE_INVALID -> {
          val errorMessage = localizeHelper.localize(Res.string.installation_error_invalid)
          Log.warn("Installation invalid: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
        else -> {
          val errorMessage = localizeHelper.localize(Res.string.package_installer_failed_to_install_packages) + " Status: $status, Message: $statusMessage"
          Log.error("Package installer failed: $errorMessage")
          Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
          deferred.complete(InstallStep.Error(errorMessage))
        }
      }
    }
  }
}

