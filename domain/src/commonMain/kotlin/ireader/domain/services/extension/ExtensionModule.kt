package ireader.domain.services.extension

import org.koin.dsl.module

/**
 * Koin module for extension-related dependencies.
 *
 * ExtensionController was removed — ExtensionViewModel talks directly
 * to CatalogStore and use cases. This module is kept as a placeholder
 * for any future extension-specific singletons.
 */
val extensionModule = module {
    // No singletons needed — ViewModel owns state directly.
}
