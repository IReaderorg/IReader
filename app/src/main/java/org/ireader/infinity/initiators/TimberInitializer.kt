package org.ireader.infinity.initiators

import org.ireader.infinity.BuildConfig
import timber.log.Timber

class TimberInitializer {
    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}