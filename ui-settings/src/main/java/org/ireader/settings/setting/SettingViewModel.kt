package org.ireader.settings.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.common_extensions.launchIO
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.image_loader.LibraryCovers
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val deleteUseCase: DeleteUseCase,
    private val libraryCovers: LibraryCovers,
    private val booksUseCasa: LocalGetBookUseCases,
    private val chapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val prefUseCases: ReaderPrefUseCases,

) : BaseViewModel() {
    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    fun deleteAllDatabase() {
        viewModelScope.launchIO {
            deleteUseCase.deleteAllBook()
            deleteUseCase.deleteAllChapters()
            deleteUseCase.deleteAllRemoteKeys()
            deleteUseCase.deleteAllExploreBook()
        }
    }

    fun deleteAllChapters() {
        viewModelScope.launchIO {
            deleteUseCase.deleteAllChapters()
        }
    }

    fun deleteDefaultSettings() {
        viewModelScope.launchIO {
            prefUseCases.selectedFontStateUseCase.saveFont(0)
            prefUseCases.fontHeightUseCase.save(25)
            prefUseCases.fontSizeStateUseCase.save(18)
            prefUseCases.paragraphDistanceUseCase.save(2)
            prefUseCases.orientationUseCase.save(OrientationMode.Portrait)
            prefUseCases.paragraphIndentUseCase.save(8)
            prefUseCases.scrollModeUseCase.save(true)
            prefUseCases.scrollIndicatorUseCase.savePadding(0)
            prefUseCases.scrollIndicatorUseCase.saveWidth(0)
        }
    }
}


