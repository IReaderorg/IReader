package ireader.data.font

import data.CustomFonts
import ireader.domain.models.fonts.CustomFont

fun CustomFonts.toDomain(): CustomFont {
    return CustomFont(
        id = id,
        name = name,
        filePath = filePath,
        isSystemFont = isSystemFont,
        dateAdded = dateAdded
    )
}

fun CustomFont.toEntity(): CustomFonts {
    return CustomFonts(
        id = id,
        name = name,
        filePath = filePath,
        isSystemFont = isSystemFont,
        dateAdded = dateAdded
    )
}
