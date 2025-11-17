package ireader.core.feature

import ireader.core.prefs.PreferenceStore

/**
 * Feature flags for gradual migration to new architecture patterns.
 *
 * This system allows for:
 * - Gradual rollout of new features
 * - A/B testing
 * - Quick rollback in case of issues
 * - Per-user or per-screen feature enablement
 *
 * Usage:
 * ```kotlin
 * if (FeatureFlags.useNewRepositories) {
 *     // Use new repository implementation
 * } else {
 *     // Use legacy implementation
 * }
 * ```
 */
object FeatureFlags {
    
    private var preferenceStore: PreferenceStore? = null
    
    /**
     * Initializes feature flags with preference store.
     *
     * @param store Preference store for persisting flag values
     */
    fun initialize(store: PreferenceStore) {
        preferenceStore = store
    }
    
    // ========== REPOSITORY LAYER FLAGS ==========
    
    /**
     * Enable new consolidated repository pattern.
     *
     * When enabled:
     * - Uses new focused repository interfaces (8 vs 30+)
     * - Implements Update classes for partial updates
     * - Uses DatabaseHandler pattern with proper error handling
     * - Supports Flow-based reactive queries
     *
     * Default: false (use legacy repositories)
     */
    var useNewRepositories: Boolean
        get() = preferenceStore?.getBoolean("feature_new_repositories", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_new_repositories", false)?.set(value)
        }
    
    /**
     * Enable new repository error handling.
     *
     * When enabled:
     * - Uses comprehensive logging with IReaderLog
     * - Implements proper exception handling
     * - Provides user-friendly error messages
     *
     * Default: false
     */
    var useNewErrorHandling: Boolean
        get() = preferenceStore?.getBoolean("feature_new_error_handling", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_new_error_handling", false)?.set(value)
        }
    
    // ========== STATE MANAGEMENT FLAGS ==========
    
    /**
     * Enable StateScreenModel pattern for state management.
     *
     * When enabled:
     * - Uses StateScreenModel instead of ViewModel
     * - Implements sealed state classes
     * - Provides predictable state transitions
     * - Better testability
     *
     * Default: false (use legacy ViewModels)
     */
    var useStateScreenModel: Boolean
        get() = preferenceStore?.getBoolean("feature_state_screen_model", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_state_screen_model", false)?.set(value)
        }
    
    /**
     * Enable use case/interactor layer.
     *
     * When enabled:
     * - Separates business logic from repositories
     * - Provides clean architecture boundaries
     * - Improves testability
     *
     * Default: false
     */
    var useUseCaseLayer: Boolean
        get() = preferenceStore?.getBoolean("feature_use_case_layer", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_use_case_layer", false)?.set(value)
        }
    
    // ========== UI COMPONENT FLAGS ==========
    
    /**
     * Enable new Material Design 3 UI components.
     *
     * When enabled:
     * - Uses IReaderErrorScreen instead of ErrorScreen
     * - Uses IReaderLoadingScreen instead of LoadingScreen
     * - Uses IReaderScaffold instead of IScaffold
     * - Implements consistent Material Design 3 theming
     *
     * Default: false (use legacy components)
     */
    var useNewUIComponents: Boolean
        get() = preferenceStore?.getBoolean("feature_new_ui_components", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_new_ui_components", false)?.set(value)
        }
    
    /**
     * Enable responsive design with TwoPanelBox.
     *
     * When enabled:
     * - Uses TwoPanelBox for tablet layouts
     * - Implements WindowSizeClass detection
     * - Provides adaptive UI components
     *
     * Default: false (use single-panel layouts)
     */
    var useResponsiveDesign: Boolean
        get() = preferenceStore?.getBoolean("feature_responsive_design", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_responsive_design", false)?.set(value)
        }
    
    /**
     * Enable FastScrollLazyColumn for list performance.
     *
     * When enabled:
     * - Uses FastScrollLazyColumn instead of LazyColumn
     * - Implements vertical fast scroller
     * - Optimizes key and contentType parameters
     *
     * Default: false (use standard LazyColumn)
     */
    var useFastScrollLists: Boolean
        get() = preferenceStore?.getBoolean("feature_fast_scroll_lists", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_fast_scroll_lists", false)?.set(value)
        }
    
    /**
     * Enable new theme system with Material Design 3.
     *
     * When enabled:
     * - Uses Material Design 3 color schemes
     * - Supports dynamic colors (Monet) on Android 12+
     * - Implements AMOLED theme support
     *
     * Default: false
     */
    var useNewThemeSystem: Boolean
        get() = preferenceStore?.getBoolean("feature_new_theme_system", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_new_theme_system", false)?.set(value)
        }
    
    // ========== PERFORMANCE FLAGS ==========
    
    /**
     * Enable performance optimizations.
     *
     * When enabled:
     * - Uses optimized image loading
     * - Implements efficient database queries
     * - Provides memory leak prevention
     *
     * Default: false
     */
    var usePerformanceOptimizations: Boolean
        get() = preferenceStore?.getBoolean("feature_performance_optimizations", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_performance_optimizations", false)?.set(value)
        }
    
    /**
     * Enable performance monitoring.
     *
     * When enabled:
     * - Tracks performance metrics
     * - Logs slow operations
     * - Provides performance reports
     *
     * Default: false
     */
    var usePerformanceMonitoring: Boolean
        get() = preferenceStore?.getBoolean("feature_performance_monitoring", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_performance_monitoring", false)?.set(value)
        }
    
    // ========== ACCESSIBILITY FLAGS ==========
    
    /**
     * Enable enhanced accessibility features.
     *
     * When enabled:
     * - Adds proper contentDescription to all components
     * - Implements screen reader compatibility
     * - Ensures proper touch targets (48dp minimum)
     *
     * Default: false
     */
    var useEnhancedAccessibility: Boolean
        get() = preferenceStore?.getBoolean("feature_enhanced_accessibility", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_enhanced_accessibility", false)?.set(value)
        }
    
    // ========== TESTING FLAGS ==========
    
    /**
     * Enable test mode.
     *
     * When enabled:
     * - Uses mock data
     * - Disables analytics
     * - Enables debug logging
     *
     * Default: false
     */
    var testMode: Boolean
        get() = preferenceStore?.getBoolean("feature_test_mode", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_test_mode", false)?.set(value)
        }
    
    /**
     * Enable debug logging.
     *
     * When enabled:
     * - Logs all operations
     * - Provides detailed error messages
     * - Tracks performance metrics
     *
     * Default: false
     */
    var debugLogging: Boolean
        get() = preferenceStore?.getBoolean("feature_debug_logging", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_debug_logging", false)?.set(value)
        }
    
    // ========== MIGRATION FLAGS ==========
    
    /**
     * Enable automatic migration.
     *
     * When enabled:
     * - Automatically migrates data on app start
     * - Runs migration scripts
     * - Updates database schema
     *
     * Default: false
     */
    var autoMigration: Boolean
        get() = preferenceStore?.getBoolean("feature_auto_migration", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("feature_auto_migration", false)?.set(value)
        }
    
    /**
     * Migration completed flag.
     *
     * Tracks whether migration has been completed.
     */
    var migrationCompleted: Boolean
        get() = preferenceStore?.getBoolean("migration_completed", false)?.get() ?: false
        set(value) {
            preferenceStore?.getBoolean("migration_completed", false)?.set(value)
        }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Enables all new features.
     *
     * Use with caution - for testing purposes only.
     */
    fun enableAllFeatures() {
        useNewRepositories = true
        useNewErrorHandling = true
        useStateScreenModel = true
        useUseCaseLayer = true
        useNewUIComponents = true
        useResponsiveDesign = true
        useFastScrollLists = true
        useNewThemeSystem = true
        usePerformanceOptimizations = true
        useEnhancedAccessibility = true
    }
    
    /**
     * Disables all new features.
     *
     * Reverts to legacy implementations.
     */
    fun disableAllFeatures() {
        useNewRepositories = false
        useNewErrorHandling = false
        useStateScreenModel = false
        useUseCaseLayer = false
        useNewUIComponents = false
        useResponsiveDesign = false
        useFastScrollLists = false
        useNewThemeSystem = false
        usePerformanceOptimizations = false
        useEnhancedAccessibility = false
    }
    
    /**
     * Resets all feature flags to default values.
     */
    fun resetToDefaults() {
        disableAllFeatures()
        testMode = false
        debugLogging = false
        autoMigration = false
    }
    
    /**
     * Gets a summary of all feature flag states.
     *
     * @return Map of feature flag names to their current values
     */
    fun getFeatureFlagSummary(): Map<String, Boolean> {
        return mapOf(
            "useNewRepositories" to useNewRepositories,
            "useNewErrorHandling" to useNewErrorHandling,
            "useStateScreenModel" to useStateScreenModel,
            "useUseCaseLayer" to useUseCaseLayer,
            "useNewUIComponents" to useNewUIComponents,
            "useResponsiveDesign" to useResponsiveDesign,
            "useFastScrollLists" to useFastScrollLists,
            "useNewThemeSystem" to useNewThemeSystem,
            "usePerformanceOptimizations" to usePerformanceOptimizations,
            "usePerformanceMonitoring" to usePerformanceMonitoring,
            "useEnhancedAccessibility" to useEnhancedAccessibility,
            "testMode" to testMode,
            "debugLogging" to debugLogging,
            "autoMigration" to autoMigration,
            "migrationCompleted" to migrationCompleted
        )
    }
}
