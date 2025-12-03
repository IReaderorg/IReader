package ireader.domain.di

import ireader.domain.plugins.Platform
import ireader.domain.services.platform.PlatformType

actual fun getPlatformType(): PlatformType = PlatformType.ANDROID

internal actual fun getPlatform(): Platform = Platform.ANDROID
