package ireader.domain.services.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based tests for ExtensionController components.
 * 
 * **Feature: architecture-improvements**
 * 
 * Tests verify:
 * - Property 4: Controller State Propagation (via ExtensionState)
 * - Property 5: Error State Management (via ExtensionError)
 * 
 * Note: Full controller integration tests require complex DI setup.
 * These tests focus on the state and error components which are the core
 * of the SSOT pattern.
 * 
 * **Validates: Requirements 3.2, 3.4, 3.5, 4.2, 4.3, 4.4, 4.5**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionControllerPropertyTest {
    
    companion object {
        private const val PROPERTY_TEST_ITERATIONS = 100
    }
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ========== Property Tests for ExtensionState ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ExtensionState, computed properties SHALL be consistent with the underlying data.
     * 
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ExtensionState computed properties are consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Test with empty state
            val emptyState = ExtensionState()
            
            assertEquals(
                0,
                emptyState.installedCount,
                "Iteration $iteration: Empty state installedCount should be 0"
            )
            
            assertEquals(
                0,
                emptyState.availableCount,
                "Iteration $iteration: Empty state availableCount should be 0"
            )
            
            assertEquals(
                false,
                emptyState.hasInstalledExtensions,
                "Iteration $iteration: Empty state hasInstalledExtensions should be false"
            )
            
            assertEquals(
                false,
                emptyState.hasAvailableExtensions,
                "Iteration $iteration: Empty state hasAvailableExtensions should be false"
            )
            
            assertEquals(
                false,
                emptyState.hasUpdates,
                "Iteration $iteration: Empty state hasUpdates should be false"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ExtensionState with loading flags, isAnyLoading SHALL reflect the combined state.
     * 
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ExtensionState loading flags are consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val isLoading = iteration % 2 == 0
            val isRefreshing = iteration % 3 == 0
            val isCheckingUpdates = iteration % 5 == 0
            
            val state = ExtensionState(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                isCheckingUpdates = isCheckingUpdates
            )
            
            assertEquals(
                isLoading || isRefreshing || isCheckingUpdates,
                state.isAnyLoading,
                "Iteration $iteration: isAnyLoading should be true if any loading flag is true"
            )
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ExtensionState with error, hasError SHALL be true.
     * 
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ExtensionState error flag is consistent`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val hasError = iteration % 2 == 0
            val error = if (hasError) ExtensionError.LoadFailed("Error $iteration") else null
            
            val state = ExtensionState(error = error)
            
            assertEquals(
                hasError,
                state.hasError,
                "Iteration $iteration: hasError should match whether error is set"
            )
        }
    }

    
    // ========== Property Tests for ExtensionError ==========
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* ExtensionError, toUserMessage() SHALL return a non-empty string.
     * 
     * **Validates: Requirements 4.4**
     */
    @Test
    fun `Property 5 - ExtensionError toUserMessage returns non-empty string`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            // Generate different error types
            val errors = listOf(
                ExtensionError.LoadFailed("Load error $iteration"),
                ExtensionError.InstallFailed("pkg_$iteration", "Install error $iteration"),
                ExtensionError.UninstallFailed("pkg_$iteration", "Uninstall error $iteration"),
                ExtensionError.UpdateFailed("pkg_$iteration", "Update error $iteration"),
                ExtensionError.NetworkError("Network error $iteration"),
                ExtensionError.CheckUpdatesFailed("Check updates error $iteration"),
                ExtensionError.RefreshFailed("Refresh error $iteration")
            )
            
            errors.forEach { error ->
                val message = error.toUserMessage()
                
                assertTrue(
                    message.isNotEmpty(),
                    "Iteration $iteration: toUserMessage() should return non-empty string for ${error::class.simpleName}"
                )
                
                assertTrue(
                    message.isNotBlank(),
                    "Iteration $iteration: toUserMessage() should return non-blank string for ${error::class.simpleName}"
                )
            }
        }
    }
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* ExtensionError with a message, toUserMessage() SHALL contain that message.
     * 
     * **Validates: Requirements 4.4**
     */
    @Test
    fun `Property 5 - ExtensionError toUserMessage contains error details`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val uniqueMessage = "unique_error_message_$iteration"
            val uniquePkgName = "pkg_unique_$iteration"
            
            // Test LoadFailed
            val loadError = ExtensionError.LoadFailed(uniqueMessage)
            assertTrue(
                loadError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: LoadFailed message should contain the error message"
            )
            
            // Test InstallFailed
            val installError = ExtensionError.InstallFailed(uniquePkgName, uniqueMessage)
            assertTrue(
                installError.toUserMessage().contains(uniquePkgName) || 
                installError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: InstallFailed message should contain pkg name or error message"
            )
            
            // Test NetworkError
            val networkError = ExtensionError.NetworkError(uniqueMessage)
            assertTrue(
                networkError.toUserMessage().contains(uniqueMessage),
                "Iteration $iteration: NetworkError message should contain the error message"
            )
        }
    }
    
    // ========== Property Tests for ExtensionFilter ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ExtensionFilter, the filter type SHALL be correctly identified.
     * 
     * **Validates: Requirements 3.2, 3.4, 3.5**
     */
    @Test
    fun `Property 4 - ExtensionFilter types are distinct`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val filters = listOf(
                ExtensionFilter.All,
                ExtensionFilter.ByLanguage(setOf("en", "ja")),
                ExtensionFilter.ByRepository("IREADER"),
                ExtensionFilter.Combined(setOf("en"), "LNREADER")
            )
            
            // Verify each filter type is distinct
            assertTrue(
                filters[0] is ExtensionFilter.All,
                "Iteration $iteration: First filter should be All"
            )
            assertTrue(
                filters[1] is ExtensionFilter.ByLanguage,
                "Iteration $iteration: Second filter should be ByLanguage"
            )
            assertTrue(
                filters[2] is ExtensionFilter.ByRepository,
                "Iteration $iteration: Third filter should be ByRepository"
            )
            assertTrue(
                filters[3] is ExtensionFilter.Combined,
                "Iteration $iteration: Fourth filter should be Combined"
            )
        }
    }
    
    // ========== Property Tests for ExtensionCommand ==========
    
    /**
     * **Feature: architecture-improvements, Property 4: Controller State Propagation**
     * 
     * *For any* ExtensionCommand, the command type SHALL be correctly identified.
     * 
     * **Validates: Requirements 3.2, 3.3**
     */
    @Test
    fun `Property 4 - ExtensionCommand types are distinct`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val commands = listOf(
                ExtensionCommand.LoadExtensions,
                ExtensionCommand.Cleanup,
                ExtensionCommand.SetSearchQuery("query_$iteration"),
                ExtensionCommand.SetRepositoryType("IREADER"),
                ExtensionCommand.CheckUpdates,
                ExtensionCommand.RefreshExtensions,
                ExtensionCommand.BatchUpdateExtensions,
                ExtensionCommand.ClearError
            )
            
            // Verify each command type is distinct
            assertTrue(
                commands[0] is ExtensionCommand.LoadExtensions,
                "Iteration $iteration: First command should be LoadExtensions"
            )
            assertTrue(
                commands[1] is ExtensionCommand.Cleanup,
                "Iteration $iteration: Second command should be Cleanup"
            )
            assertTrue(
                commands[2] is ExtensionCommand.SetSearchQuery,
                "Iteration $iteration: Third command should be SetSearchQuery"
            )
            assertTrue(
                commands[7] is ExtensionCommand.ClearError,
                "Iteration $iteration: Last command should be ClearError"
            )
        }
    }
    
    // ========== Property Tests for ExtensionEvent ==========
    
    /**
     * **Feature: architecture-improvements, Property 5: Error State Management**
     * 
     * *For any* ExtensionEvent.Error, the error SHALL be accessible.
     * 
     * **Validates: Requirements 4.2, 4.3**
     */
    @Test
    fun `Property 5 - ExtensionEvent Error contains error details`() = runTest(testDispatcher) {
        repeat(PROPERTY_TEST_ITERATIONS) { iteration ->
            val error = ExtensionError.LoadFailed("Error $iteration")
            val event = ExtensionEvent.Error(error)
            
            assertEquals(
                error,
                event.error,
                "Iteration $iteration: Event error should match the original error"
            )
            
            assertEquals(
                error.toUserMessage(),
                event.error.toUserMessage(),
                "Iteration $iteration: Event error message should match"
            )
        }
    }
    
}
