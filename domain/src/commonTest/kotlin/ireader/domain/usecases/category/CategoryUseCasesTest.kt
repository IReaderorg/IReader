package ireader.domain.usecases.category

import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for category management use cases
 */
class CategoryUseCasesTest {
    
    private lateinit var createCategoryUseCase: CreateCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var categoryRepository: CategoryRepository
    
    @BeforeTest
    fun setup() {
        categoryRepository = mockk()
        createCategoryUseCase = CreateCategoryUseCase(categoryRepository)
        deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `createCategory should create new category successfully`() = runTest {
        // Given
        val categoryName = "Action"
        val expectedCategory = Category(
            id = 1L,
            name = categoryName,
            order = 0,
            flags = 0
        )
        coEvery { categoryRepository.insert(any()) } returns expectedCategory.id
        
        // When
        val result = createCategoryUseCase(categoryName)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { categoryRepository.insert(match { it.name == categoryName }) }
    }
    
    @Test
    fun `createCategory should fail with empty name`() = runTest {
        // Given
        val categoryName = ""
        
        // When
        val result = createCategoryUseCase(categoryName)
        
        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { categoryRepository.insert(any()) }
    }
    
    @Test
    fun `createCategory should fail with duplicate name`() = runTest {
        // Given
        val categoryName = "Action"
        coEvery { categoryRepository.insert(any()) } throws Exception("Category already exists")
        
        // When
        val result = createCategoryUseCase(categoryName)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `deleteCategory should remove category successfully`() = runTest {
        // Given
        val categoryId = 1L
        coEvery { categoryRepository.delete(categoryId) } just Runs
        
        // When
        val result = deleteCategoryUseCase(categoryId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { categoryRepository.delete(categoryId) }
    }
    
    @Test
    fun `deleteCategory should handle non-existent category`() = runTest {
        // Given
        val categoryId = 999L
        coEvery { categoryRepository.delete(categoryId) } throws Exception("Category not found")
        
        // When
        val result = deleteCategoryUseCase(categoryId)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `getCategories should return all categories`() = runTest {
        // Given
        val categories = listOf(
            Category(id = 1L, name = "Action", order = 0, flags = 0),
            Category(id = 2L, name = "Romance", order = 1, flags = 0),
            Category(id = 3L, name = "Fantasy", order = 2, flags = 0)
        )
        coEvery { categoryRepository.findAll() } returns categories.map { 
            ireader.domain.models.entities.CategoryWithCount(it, 0) 
        }
        
        // When
        val result = getCategoriesUseCase()
        
        // Then
        assertEquals(3, result.size)
        coVerify { categoryRepository.findAll() }
    }
    
    @Test
    fun `getCategories should return empty list when no categories exist`() = runTest {
        // Given
        coEvery { categoryRepository.findAll() } returns emptyList()
        
        // When
        val result = getCategoriesUseCase()
        
        // Then
        assertTrue(result.isEmpty())
    }
}
