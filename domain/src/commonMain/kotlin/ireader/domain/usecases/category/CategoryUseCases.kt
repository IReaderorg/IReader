package ireader.domain.usecases.category

/**
 * Aggregate class for all category-related use cases
 * Provides a single point of access for category operations
 */
data class CategoryUseCases(
    val getCategories: GetCategoriesUseCase,
    val getCategoryById: GetCategoryByIdUseCase,
    val createCategory: CreateCategoryUseCase,
    val updateCategory: UpdateCategoryUseCase,
    val deleteCategory: DeleteCategoryUseCase,
    val reorderCategories: ReorderCategory,
    val assignBookToCategory: AssignBookToCategoryUseCase,
    val removeBookFromCategory: RemoveBookFromCategoryUseCase,
    val autoCategorizeBook: AutoCategorizeBookUseCase,
    val manageAutoRules: ManageCategoryAutoRulesUseCase,
)
