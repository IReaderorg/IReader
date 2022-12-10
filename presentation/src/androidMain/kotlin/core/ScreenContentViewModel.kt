package ireader.presentation.core

import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ScreenContentViewModel(
    private val uiPreferences: UiPreferences,
) : BaseViewModel() {

    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var showHistory = uiPreferences.showHistoryInButtonBar().asState()
    var confirmExit = uiPreferences.confirmExit().asState()
}
