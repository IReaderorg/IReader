package org.ireader.domain.models.entities.model

enum class InstallStep {
    Downloading, Installing, Completed, Error;

    fun isFinished(): Boolean {
        return this == Completed || this == Error
    }
}
