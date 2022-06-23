package org.ireader.core_ui.theme.prefs

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.ireader.core_api.log.Log
import org.ireader.core_api.prefs.Preference
import org.ireader.core_ui.theme.BaseTheme
import org.ireader.core_ui.theme.ExtraColors

@OptIn(ExperimentalSerializationApi::class)
class CustomThemePreferences(
    private val preference: Preference<String>
) : Preference<List<BaseTheme>> {

    override fun key(): String {
        return preference.key()
    }

    override fun get(): List<BaseTheme> {
        return if (isSet()) {
            ProtoBuf.decodeFromHexString<CustomThemes>(preference.get()).themes.map { it.toBaseTheme() }
        } else {
            emptyList()
        }
    }

    override suspend fun read(): List<BaseTheme> {
        return if (isSet()) {
            ProtoBuf.decodeFromHexString<CustomThemes>(preference.get()).themes.map { it.toBaseTheme() }
        } else {
            emptyList()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun setTheme(value: BaseTheme) {
        kotlin.runCatching {
            val themes =  ProtoBuf.decodeFromHexString<CustomThemes>(preference.get()).themes.map { it.toBaseTheme() }.toMutableList()
            themes.add(0,value)
            themes.removeLast()
            val json = ProtoBuf.encodeToHexString(themes.map { it.toCustomTheme() })
                preference.set(json)
        }.getOrElse {
            Log.error(it,"Custom Theme was corrupted")
//            val json = Json.encodeToString(emptyList<CustomThemes>())
//            preference.set(json)
            preference.delete()
        }
    }

    override fun set(value: List<BaseTheme>) {
        kotlin.runCatching {
            preference.set(ProtoBuf.encodeToHexString(value))
        }.getOrElse {
            it
            preference.delete()
        }
    }

    override fun isSet(): Boolean {
        return preference.isSet()
    }

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): List<BaseTheme> {
        return emptyList()
    }

    override fun changes(): Flow<List<BaseTheme>> {
        return preference.changes()
            .map { get() }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<List<BaseTheme>> {
        return preference.changes().map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }
}

fun Preference<String>.asCustomTheme(): CustomThemePreferences {
    return CustomThemePreferences(this)
}

fun CustomTheme.toBaseTheme(): BaseTheme {
    return BaseTheme(
        id = this.id,
        lightColor = this.lightColor.toColorScheme(),
        darkColor = this.darkColor.toColorScheme(),
        darkExtraColors = this.darkExtraColors.toExtraColor(),
        lightExtraColors = this.lightExtraColors.toExtraColor()
    )
}

fun BaseTheme.toCustomTheme(): CustomTheme {
    return CustomTheme(
        id = this.id,
        lightColor = this.lightColor.toCustomColorScheme(),
        darkColor = this.darkColor.toCustomColorScheme(),
        lightExtraColors = this.lightExtraColors.toCustomExtraColors(),
        darkExtraColors = this.darkExtraColors.toCustomExtraColors()
    )
}

@Serializable
data class CustomThemes(
    val themes: List<CustomTheme> = emptyList()
)

@Serializable
data class CustomTheme(
    val id: Int,
    val lightColor: CustomColorScheme,
    val darkColor: CustomColorScheme,
    val lightExtraColors: CustomExtraColors,
    val darkExtraColors: CustomExtraColors,
)

@Serializable
data class CustomColorScheme(
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val onPrimaryContainer: Int,
    val inversePrimary: Int,
    val secondary: Int,
    val onSecondary: Int,
    val secondaryContainer: Int,
    val onSecondaryContainer: Int,
    val tertiary: Int,
    val onTertiary: Int,
    val tertiaryContainer: Int,
    val onTertiaryContainer: Int,
    val background: Int,
    val onBackground: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val surfaceTint: Int,
    val inverseSurface: Int,
    val inverseOnSurface: Int,
    val error: Int,
    val onError: Int,
    val errorContainer: Int,
    val onErrorContainer: Int,
    val outline: Int,
)

private fun Int.toArgColor(): Color {
    return Color(this)
}

fun CustomColorScheme.toColorScheme(): ColorScheme {
    return ColorScheme(
        primary = this.primary.toArgColor(),
        primaryContainer = this.primaryContainer.toArgColor(),
        onPrimary = this.onPrimary.toArgColor(),
        secondary = this.secondary.toArgColor(),
        onSecondary = this.onSecondary.toArgColor(),
        background = this.background.toArgColor(),
        surface = this.surface.toArgColor(),
        onBackground = this.onBackground.toArgColor(),
        onSurface = this.onSurface.toArgColor(),
        error = this.error.toArgColor(),
        onError = this.onError.toArgColor(),
        surfaceTint = this.surfaceTint.toArgColor(),
        secondaryContainer = this.secondaryContainer.toArgColor(),
        errorContainer = this.errorContainer.toArgColor(),
        inverseOnSurface = this.inverseOnSurface.toArgColor(),
        inversePrimary = this.inversePrimary.toArgColor(),
        inverseSurface = this.inverseSurface.toArgColor(),
        onErrorContainer = this.onErrorContainer.toArgColor(),
        onPrimaryContainer = this.onPrimaryContainer.toArgColor(),
        onSecondaryContainer = this.onSecondaryContainer.toArgColor(),
        onSurfaceVariant = this.onSurfaceVariant.toArgColor(),
        onTertiary = this.onTertiary.toArgColor(),
        onTertiaryContainer = this.onTertiaryContainer.toArgColor(),
        outline = this.outline.toArgColor(),
        surfaceVariant = this.surfaceVariant.toArgColor(),
        tertiary = this.tertiary.toArgColor(),
        tertiaryContainer = this.tertiaryContainer.toArgColor(),
    )
}

fun ColorScheme.toCustomColorScheme(): CustomColorScheme {
    return CustomColorScheme(
        primary = this.primary.toArgb(),
        primaryContainer = this.primaryContainer.toArgb(),
        onPrimary = this.onPrimary.toArgb(),
        secondary = this.secondary.toArgb(),
        onSecondary = this.onSecondary.toArgb(),
        background = this.background.toArgb(),
        surface = this.surface.toArgb(),
        onBackground = this.onBackground.toArgb(),
        onSurface = this.onSurface.toArgb(),
        error = this.error.toArgb(),
        onError = this.onError.toArgb(),
        surfaceTint = this.surfaceTint.toArgb(),
        secondaryContainer = this.secondaryContainer.toArgb(),
        errorContainer = this.errorContainer.toArgb(),
        inverseOnSurface = this.inverseOnSurface.toArgb(),
        inversePrimary = this.inversePrimary.toArgb(),
        inverseSurface = this.inverseSurface.toArgb(),
        onErrorContainer = this.onErrorContainer.toArgb(),
        onPrimaryContainer = this.onPrimaryContainer.toArgb(),
        onSecondaryContainer = this.onSecondaryContainer.toArgb(),
        onSurfaceVariant = this.onSurfaceVariant.toArgb(),
        onTertiary = this.onTertiary.toArgb(),
        onTertiaryContainer = this.onTertiaryContainer.toArgb(),
        outline = this.outline.toArgb(),
        surfaceVariant = this.surfaceVariant.toArgb(),
        tertiary = this.tertiary.toArgb(),
        tertiaryContainer = this.tertiaryContainer.toArgb()
    )
}

fun ExtraColors.toCustomExtraColors(): CustomExtraColors {
    return CustomExtraColors(
        bars = this.bars.toArgb(),
        onBars = this.onBars.toArgb(),
        isBarLight = this.isBarLight
    )
}

fun CustomExtraColors.toExtraColor(): ExtraColors {
    return ExtraColors(
        bars = this.bars.toArgColor(),
        onBars = this.onBars.toArgColor(),
        isBarLight = this.isBarLight
    )
}

@Serializable
data class CustomExtraColors(
    val bars: Int = Color.Unspecified.toArgb(),
    val onBars: Int = Color.Unspecified.toArgb(),
    val isBarLight: Boolean = Color(bars).luminance() > 0.5,
)