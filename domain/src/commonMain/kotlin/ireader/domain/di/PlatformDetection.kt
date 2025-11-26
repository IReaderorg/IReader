package ireader.domain.di

import ireader.domain.services.platform.PlatformType

/**
 * Platform detection for dependency injection
 */
expect fun getPlatformType(): PlatformType
