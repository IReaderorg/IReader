package ir.kazemcodes.infinity.feature_updater

sealed class AppUpdateResult {
    class NewUpdate(val release: GithubRelease) : AppUpdateResult()
    object NoNewUpdate : AppUpdateResult()
}