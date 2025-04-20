package ireader.presentation.ui.settings.advance


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.entities.Category
import ireader.domain.preferences.models.getDefaultFont
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.reader.SettingState


class AdvanceSettingViewModel(
    val deleteUseCase: ireader.domain.usecases.local.DeleteUseCase,
    private val prefUseCases: ReaderPrefUseCases,
    val getSimpleStorage: GetSimpleStorage,
    val importEpub: ImportEpub,
    private val readerPreferences: ReaderPreferences,
    private val androidReaderPreferences: PlatformUiPreferences,
    private val themeRepository: ThemeRepository,
    private val categoryRepository: CategoryRepository,
    private val appPreferences: AppPreferences,
    private val databaseHandler: DatabaseHandler,
    private val bookRepository: BookRepository,
    ) : BaseViewModel() {

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
            androidReaderPreferences.font()?.set(getDefaultFont())
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
    
    /**
     * Repairs database issues by recreating views and validating structure
     */
    fun repairDatabase() {
        scope.launchIO {
            try {
                databaseHandler.repairDatabase()
                showSnackBar(UiText.MStringResource(MR.strings.success))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Database repair failed: ${e.message}"))
            }
        }
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

    /**
     * Repairs the book category assignments by ensuring all books have the default category
     */
    fun repairBookCategories() {
        scope.launchIO {
            try {
                bookRepository.repairCategoryAssignments()
                showSnackBar(UiText.MStringResource(MR.strings.success))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to repair categories: ${e.message}"))
            }
        }
    }
}
