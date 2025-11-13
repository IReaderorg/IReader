package ireader.domain.models.common

/**
 * Domain representation of a font family.
 * This is independent of any UI framework.
 */
sealed class FontFamilyModel {
    object Default : FontFamilyModel()
    object SansSerif : FontFamilyModel()
    object Serif : FontFamilyModel()
    object Monospace : FontFamilyModel()
    object Cursive : FontFamilyModel()
    data class Custom(val name: String, val path: String? = null) : FontFamilyModel()
}
