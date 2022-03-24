package org.ireader.presentation.feature_updates.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.entities.Update
import org.ireader.domain.repository.UpdatesRepository
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateStateImpl: UpdateStateImpl,
    private val updatesRepository: UpdatesRepository,
) : BaseViewModel(), UpdateState by updateStateImpl {


    val getUpdates = updatesRepository.subscribeAllUpdates().asState(emptyList()) {
        updates = it
    }

    fun addUpdate(update: Update) {
        if (update.id in selection) {
            selection.remove(update.id)
        } else {
            selection.add(update.id)
        }
    }


}