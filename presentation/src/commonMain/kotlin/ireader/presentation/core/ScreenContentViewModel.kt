package ireader.presentation.core

import ireader.domain.preferences.prefs.UiPreferences



class ScreenContentViewModel(
    private val uiPreferences: UiPreferences,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var showHistory = uiPreferences.showHistoryInButtonBar().asState()
    var confirmExit = uiPreferences.confirmExit().asState()
}
