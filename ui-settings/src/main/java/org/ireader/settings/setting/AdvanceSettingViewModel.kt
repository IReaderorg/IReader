package org.ireader.settings.setting

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.common_data.repository.CategoryRepository
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.Category
import org.ireader.common_resources.UiText
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.theme.getDefaultFont
import org.ireader.core_ui.theme.themes
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.epub.importer.ImportEpub
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.ReaderPrefUseCases
import org.ireader.domain.use_cases.theme.toCustomTheme
import org.ireader.image_loader.coil.cache.CoverCache
import org.ireader.ui_settings.R
import javax.inject.Inject

@HiltViewModel
class AdvanceSettingViewModel @Inject constructor(
    val deleteUseCase: DeleteUseCase,
    private val prefUseCases: ReaderPrefUseCases,
    val coverCache: CoverCache,
    val importEpub: ImportEpub,
    private val readerPreferences: ReaderPreferences,
    private val themeRepository: ThemeRepository,
    private val categoryRepository: CategoryRepository,

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
            prefUseCases.selectedFontStateUseCase.saveFont(getDefaultFont())
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

    fun resetCategories() {
        viewModelScope.launchIO {
            categoryRepository.deleteAll()
            categoryRepository.insertOrUpdate(Category.baseCategories)
            showSnackBar(UiText.StringResource(R.string.success))
        }
    }

    fun resetThemes() {
        viewModelScope.launchIO {
            themeRepository.deleteAll()
            themeRepository.insert(themes.map { it.toCustomTheme() })
            showSnackBar(UiText.StringResource(R.string.success))
        }
    }
}
