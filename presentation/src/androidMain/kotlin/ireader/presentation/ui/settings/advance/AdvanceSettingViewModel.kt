package ireader.presentation.ui.settings.advance

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.entities.Category
import ireader.i18n.UiText
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.image.cache.CoverCache
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.preferences.AndroidReaderPrefUseCases
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.utils.extensions.launchIO

import ireader.i18n.resources.MR
import ireader.presentation.ui.settings.reader.SettingState


class AdvanceSettingViewModel(
        val deleteUseCase: ireader.domain.usecases.local.DeleteUseCase,
        private val prefUseCases: ReaderPrefUseCases,
        val coverCache: CoverCache,
        val importEpub: ImportEpub,
        private val readerPreferences: ReaderPreferences,
        private val androidReaderPreferences: PlatformUiPreferences,
        private val themeRepository: ThemeRepository,
        private val categoryRepository: CategoryRepository,
        private val androidUiPreferences: AndroidReaderPrefUseCases,
        private val appPreferences: AppPreferences,

        ) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    private val _state = mutableStateOf(SettingState())
    val state: State<SettingState> = _state

    fun deleteAllDatabase() {
        scope.launchIO {
            deleteUseCase.deleteAllBook()
            deleteUseCase.deleteAllChapters()
        }
    }

    fun deleteAllChapters() {
        scope.launchIO {
            deleteUseCase.deleteAllChapters()
        }
    }

    fun deleteDefaultSettings() {
        scope.launchIO {
            androidUiPreferences.selectedFontStateUseCase.saveFont(getDefaultFont())
            prefUseCases.fontHeightUseCase.save(25)
            prefUseCases.fontSizeStateUseCase.save(18)
            prefUseCases.paragraphDistanceUseCase.save(2)
            appPreferences.orientation().set(AppPreferences.PreferenceKeys.Orientation.Unspecified.ordinal)
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
        scope.launchIO {
            categoryRepository.deleteAll()
            categoryRepository.insert(Category.baseCategories)
            showSnackBar(UiText.MStringResource(MR.strings.success))
        }
    }

    fun resetThemes() {
        scope.launchIO {
            themeRepository.deleteAll()
            showSnackBar(UiText.MStringResource(MR.strings.success))
        }
    }
}
