/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.os

import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.*
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class PackageInstaller @Inject constructor(
  private val context: Application
) {

  private val packageInstaller = context.packageManager.packageInstaller

  @Suppress("BlockingMethodInNonBlockingContext")
  suspend fun install(file: File, packageName: String): Boolean {
    return withInstallReceiver { sender ->
      withContext(Dispatchers.IO) {
        val params = PackageInstaller.SessionParams(MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)
        val sessionId = packageInstaller.createSession(params)
        packageInstaller.openSession(sessionId).use { session ->
          val output = session.openWrite(packageName, 0, -1)
          output.use {
            file.inputStream().use { input ->
              input.copyTo(output)
            }
            session.fsync(output)
          }
          session.commit(sender)
        }
      }
    }
  }

  suspend fun uninstall(packageName: String): Boolean {
    return withInstallReceiver { sender ->
      packageInstaller.uninstall(packageName, sender)
    }
  }

  private suspend fun withInstallReceiver(block: suspend (IntentSender) -> Unit): Boolean {
    val deferred = CompletableDeferred<Boolean>()
    val receiver = InstallResultReceiver(context, deferred)
    val uid = SystemClock.elapsedRealtime()
    val action = "core-api.INSTALL_APK_$uid"
    val broadcast = PendingIntent.getBroadcast(context, 0, Intent(action), FLAG_MUTABLE)

    context.registerReceiver(receiver, IntentFilter(action))
    return try {
      block(broadcast.intentSender)
      deferred.await()
    } finally {
      context.unregisterReceiver(receiver)
      broadcast.cancel()
    }
  }

  private class InstallResultReceiver(
    val context: Application,
    val deferred: CompletableDeferred<Boolean>
  ) : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
      intent ?: return
      val status = intent
        .getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)

      when (status) {
        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
          val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
          confirmationIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          try {
            context.startActivity(confirmationIntent)

            // Mark installation as completed even if it's not finished because we don't always
            // receive the result callback
            deferred.complete(true)
          } catch (e: Throwable) {
            Log.w("Error while (un)installing package",e)
            deferred.complete(false)
          }
        }
        PackageInstaller.STATUS_SUCCESS -> {
          deferred.complete(true)
        }
        else -> {
          deferred.complete(false)
        }
      }
    }
  }

}
