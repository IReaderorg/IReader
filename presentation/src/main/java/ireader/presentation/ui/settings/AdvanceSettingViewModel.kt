package ireader.presentation.ui.settings

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import ireader.common.models.entities.Category
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.epub.importer.ImportEpub
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.R
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AdvanceSettingViewModel(
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
            categoryRepository.insert(Category.baseCategories)
            showSnackBar(UiText.StringResource(R.string.success))
        }
    }

    fun resetThemes() {
        viewModelScope.launchIO {
            themeRepository.deleteAll()
            showSnackBar(UiText.StringResource(R.string.success))
        }
    }
}
