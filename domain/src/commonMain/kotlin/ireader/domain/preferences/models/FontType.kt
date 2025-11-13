package ireader.domain.preferences.models

import ireader.domain.models.common.FontFamilyModel

data class FontType(
    val name: String,
    val fontFamily: FontFamilyModel
)
fun getDefaultFont(): FontType {
    return FontType("Roboto", FontFamilyModel.Default)
}