package ireader.domain.preferences.models

import ireader.domain.models.common.ColorModel

data class ReaderColors(
    val id: Long,
    val backgroundColor: ColorModel,
    val onTextColor: ColorModel,
    val isDefault: Boolean = false
)