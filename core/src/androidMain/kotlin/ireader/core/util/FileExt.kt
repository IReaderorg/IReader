/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import ireader.core.BuildConfig
import java.io.File

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
  // TODO check the application id matches the provider
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    FileProvider.getUriForFile(context, BuildConfig.LIBRARY_PACKAGE_NAME + ".provider", this)
  else Uri.fromFile(this)
}
fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
}
