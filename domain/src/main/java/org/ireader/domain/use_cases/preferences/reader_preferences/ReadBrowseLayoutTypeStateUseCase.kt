package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.layouts
import org.ireader.domain.repository.Repository

class ReadBrowseLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): DisplayMode {
        return layouts[repository.preferencesHelper.browseLayoutTypeStateKey.get()]
    }
}