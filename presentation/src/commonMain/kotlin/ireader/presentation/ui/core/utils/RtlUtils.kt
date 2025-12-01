package ireader.presentation.ui.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * RTL (Right-to-Left) support utilities for the app.
 * 
 * This file provides utilities for handling RTL locales like Arabic, Hebrew, Persian, etc.
 * 
 * Key features:
 * - Layout direction detection
 * - Mirroring helpers for icons and gestures
 * - Swipe direction reversal for RTL
 */

/**
 * CompositionLocal to track if the current locale is RTL.
 * This is automatically set based on the system locale.
 */
val LocalIsRtl = compositionLocalOf { false }

/**
 * Check if the current layout direction is RTL.
 */
@Composable
@ReadOnlyComposable
fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl

/**
 * Get the current layout direction.
 */
@Composable
@ReadOnlyComposable
fun layoutDirection(): LayoutDirection = LocalLayoutDirection.current

/**
 * Returns the appropriate horizontal direction multiplier for RTL support.
 * Returns 1 for LTR and -1 for RTL.
 * 
 * Use this to flip horizontal animations, translations, or swipe directions.
 */
@Composable
@ReadOnlyComposable
fun rtlMultiplier(): Int = if (isRtl()) -1 else 1

/**
 * Returns the appropriate horizontal direction multiplier as Float.
 */
@Composable
@ReadOnlyComposable
fun rtlMultiplierFloat(): Float = if (isRtl()) -1f else 1f

/**
 * Reverses a horizontal value for RTL layouts.
 * In RTL, positive becomes negative and vice versa.
 */
@Composable
@ReadOnlyComposable
fun Float.reverseForRtl(): Float = this * rtlMultiplierFloat()

/**
 * Reverses an Int value for RTL layouts.
 */
@Composable
@ReadOnlyComposable
fun Int.reverseForRtl(): Int = this * rtlMultiplier()

/**
 * Swaps start/end values for RTL layouts.
 * In LTR: returns (start, end)
 * In RTL: returns (end, start)
 */
@Composable
@ReadOnlyComposable
fun <T> swapForRtl(start: T, end: T): Pair<T, T> = 
    if (isRtl()) Pair(end, start) else Pair(start, end)

/**
 * Returns the "start" side based on layout direction.
 * LTR: left side (true for rightSide = false)
 * RTL: right side (true for rightSide = true)
 */
@Composable
@ReadOnlyComposable
fun isStartSide(rightSide: Boolean): Boolean = 
    if (isRtl()) rightSide else !rightSide

/**
 * Converts a "rightSide" boolean to respect RTL.
 * If rightSide is true in LTR, it should be false in RTL (and vice versa).
 */
@Composable
@ReadOnlyComposable
fun Boolean.adjustForRtl(): Boolean = 
    if (isRtl()) !this else this

/**
 * List of RTL language codes.
 */
val RTL_LANGUAGES = setOf(
    "ar",  // Arabic
    "arc", // Aramaic
    "dv",  // Divehi
    "fa",  // Persian (Farsi)
    "ha",  // Hausa
    "he",  // Hebrew
    "iw",  // Hebrew (old code)
    "khw", // Khowar
    "ks",  // Kashmiri
    "ku",  // Kurdish
    "ps",  // Pashto
    "sd",  // Sindhi
    "ur",  // Urdu
    "yi",  // Yiddish
)

/**
 * Check if a language code represents an RTL language.
 */
fun isRtlLanguage(languageCode: String): Boolean {
    val code = languageCode.lowercase().split("-", "_").firstOrNull() ?: return false
    return code in RTL_LANGUAGES
}

/**
 * Determines the layout direction for a given language code.
 */
fun getLayoutDirectionForLanguage(languageCode: String): LayoutDirection {
    return if (isRtlLanguage(languageCode)) LayoutDirection.Rtl else LayoutDirection.Ltr
}

/**
 * Modifier extension to mirror content horizontally in RTL layouts.
 * Useful for icons or graphics that should be mirrored.
 */
@Composable
fun androidx.compose.ui.Modifier.mirrorForRtl(): androidx.compose.ui.Modifier {
    return if (isRtl()) {
        this.then(androidx.compose.ui.Modifier.graphicsLayer { scaleX = -1f })
    } else {
        this
    }
}

/**
 * Helper to get the appropriate alignment for "start" position.
 * In LTR: Start = Left
 * In RTL: Start = Right
 */
@Composable
@ReadOnlyComposable
fun startAlignment(): androidx.compose.ui.Alignment.Horizontal {
    return if (isRtl()) androidx.compose.ui.Alignment.End else androidx.compose.ui.Alignment.Start
}

/**
 * Helper to get the appropriate alignment for "end" position.
 * In LTR: End = Right
 * In RTL: End = Left
 */
@Composable
@ReadOnlyComposable
fun endAlignment(): androidx.compose.ui.Alignment.Horizontal {
    return if (isRtl()) androidx.compose.ui.Alignment.Start else androidx.compose.ui.Alignment.End
}
