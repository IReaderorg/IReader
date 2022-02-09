package org.ireader.infinity.feature_services

import android.app.PendingIntent
import android.os.Build

val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
else
    PendingIntent.FLAG_UPDATE_CURRENT