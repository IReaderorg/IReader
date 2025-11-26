package ireader.domain.di

import ireader.domain.services.platform.PlatformType

actual fun getPlatformType(): PlatformType = PlatformType.ANDROID
