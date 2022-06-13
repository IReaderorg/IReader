package org.ireader.settings.setting

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.common_extensions.launchIO
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.theme.Roboto
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.epub.importer.ImportEpub
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.image_loader.coil.cache.CoverCache
import javax.inject.Inject

@HiltViewModel
class AdvanceSettingViewModel @Inject constructor(
    val deleteUseCase: DeleteUseCase,
    private val prefUseCases: ReaderPrefUseCases,
    val coverCache: CoverCache,
    val importEpub: ImportEpub,
    private val readerPreferences: ReaderPreferences
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
            prefUseCases.selectedFontStateUseCase.saveFont(Roboto)
            prefUseCases.fontHeightUseCase.save(25)
            prefUseCases.fontSizeStateUseCase.save(18)
            prefUseCases.paragraphDistanceUseCase.save(2)
            readerPreferences.orientation().set(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            prefUseCases.paragraphIndentUseCase.save(8)
            prefUseCases.scrollModeUseCase.save(true)
            prefUseCases.scrollIndicatorUseCase.savePadding(0)
            readerPreferences.textWeight().set(400)
        }
    }
    fun onEpubImportRequested(onStart: (Intent) -> Unit) {
        val mimeTypes = arrayOf("application/epub+zip")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/epub+zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        onStart(intent)
    }

}
