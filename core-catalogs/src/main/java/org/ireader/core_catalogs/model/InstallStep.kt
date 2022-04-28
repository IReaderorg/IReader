

package org.ireader.core_catalogs.model

enum class InstallStep {
    Downloading, Installing, Completed, Error;

    fun isFinished(): Boolean {
        return this == Completed || this == Error
    }
}
