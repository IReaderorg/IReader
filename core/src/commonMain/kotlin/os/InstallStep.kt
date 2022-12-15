package ireader.core.os

private const val INSTALL_ACTION = "PackageInstallerInstaller.INSTALL_ACTION"

sealed class InstallStep(error: String? = null) {
  object Success : InstallStep(null)
  object Downloading : InstallStep(null)
  object Idle : InstallStep()
  data class Error(val error: String) : InstallStep( error)

  fun isFinished(): Boolean {
    return this is Idle || this is Error || this is Success
  }

  fun isLoading(): Boolean {
    return this is Downloading
  }
}
