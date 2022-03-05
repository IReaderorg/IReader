package org.ireader.infinity.initiators

import org.ireader.infinity.BuildConfig
import timber.log.Timber
import javax.inject.Inject

class TimberInitializer @Inject constructor() {
    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}