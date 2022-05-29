package org.ireader.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ScreenContentViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
) : BaseViewModel(){

    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var showHistory = uiPreferences.showHistoryInButtonBar().asState()




}
