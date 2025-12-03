package ireader.domain.di

import ireader.domain.plugins.Platform
import ireader.domain.services.platform.PlatformType

actual fun getPlatformType(): PlatformType = PlatformType.IOS

internal actual fun getPlatform(): Platform = Platform.IOS
