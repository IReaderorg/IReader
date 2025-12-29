package ireader.presentation.ui.settings.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.CategoryAutoRule
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CategoryUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch



@OptIn(ExperimentalCoroutinesApi::class)
class CategoryScreenViewModel(
    val categoriesUseCase: CategoriesUseCases,
    val reorderCategory: ReorderCategory,
    val createCategoryWithName: CreateCategoryWithName,
    private val libraryPreferences: ireader.domain.preferences.prefs.LibraryPreferences,
    // NEW: Clean architecture use cases
    private val categoryUseCases: CategoryUseCases,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    var categories: SnapshotStateList<CategoryWithCount> = mutableStateListOf()
    var showDialog by mutableStateOf(false)
    
    val showEmptyCategories = libraryPreferences.showEmptyCategories().asState()
    
    // Auto-categorization rules state
    var autoRulesByCategory = mutableStateMapOf<Long, List<CategoryAutoRule>>()
        private set
    var showAutoRulesDialog by mutableStateOf(false)
    var selectedCategoryForRules by mutableStateOf<CategoryWithCount?>(null)
    var showAddRuleDialog by mutableStateOf(false)
    var selectedRuleType by mutableStateOf(CategoryAutoRule.RuleType.GENRE)
    
    init {
        libraryPreferences.showEmptyCategories().stateIn(scope)
            .flatMapLatest { showEmpty ->
                categoriesUseCase.subscribe(false, showEmpty,scope)
            }
            .onEach { list ->
                categories.clear()
                categories.addAll(list)
            }.launchIn(scope)
        
        // Subscribe to all auto-rules
        categoryUseCases.manageAutoRules.subscribeAllRules()
            .onEach { rules ->
                autoRulesByCategory.clear()
                rules.groupBy { it.categoryId }.forEach { (categoryId, categoryRules) ->
                    autoRulesByCategory[categoryId] = categoryRules
                }
            }.launchIn(scope)
    }
    
    /**
     * Rename a category using the new use case layer
     * Includes validation and error handling
     */
    suspend fun renameCategory(categoryId: Long, newName: String) {
        scope.launch {
            val result = categoryUseCases.updateCategory.rename(categoryId, newName)
            
            result.onSuccess {
                showSnackBar(ireader.i18n.UiText.DynamicString("Category renamed: $newName"))
            }
            
            result.onFailure { error ->
                when {
                    error.message?.contains("blank") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category name cannot be empty"))
                    error.message?.contains("already exists") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category already exists: $newName"))
                    error.message?.contains("not found") == true ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Category not found"))
                    else ->
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to rename: ${error.message ?: "Unknown error"}"))
                }
            }
        }
    }
    
    /**
     * Add a genre-based auto-categorization rule
     */
    fun addGenreRule(categoryId: Long, genre: String) {
        scope.launch {
            val result = categoryUseCases.manageAutoRules.addGenreRule(categoryId, genre)
            result.onSuccess {
                showSnackBar(ireader.i18n.UiText.DynamicString("Added genre rule: $genre"))
            }
            result.onFailure { error ->
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to add rule: ${error.message}"))
            }
        }
    }
    
    /**
     * Add a source-based auto-categorization rule
     */
    fun addSourceRule(categoryId: Long, sourceId: Long, sourceName: String? = null) {
        scope.launch {
            val result = categoryUseCases.manageAutoRules.addSourceRule(categoryId, sourceId, sourceName)
            result.onSuccess {
                showSnackBar(ireader.i18n.UiText.DynamicString("Added source rule: ${sourceName ?: sourceId}"))
            }
            result.onFailure { error ->
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to add rule: ${error.message}"))
            }
        }
    }
    
    /**
     * Toggle a rule's enabled state
     */
    fun toggleRule(ruleId: Long, enabled: Boolean) {
        scope.launch {
            categoryUseCases.manageAutoRules.toggleRule(ruleId, enabled)
        }
    }
    
    /**
     * Delete a rule
     */
    fun deleteRule(ruleId: Long) {
        scope.launch {
            categoryUseCases.manageAutoRules.deleteRule(ruleId)
        }
    }
    
    /**
     * Get rules for a specific category
     */
    fun getRulesForCategory(categoryId: Long): List<CategoryAutoRule> {
        return autoRulesByCategory[categoryId] ?: emptyList()
    }
    
    //  val categories by categoriesUseCase.subscribe(false).asState(emptyList())
}
