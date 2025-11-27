package ireader.domain.models.theme

import ireader.domain.models.common.DomainColor

/**
 * The extra colors of the application which are not included in Material3 ColorScheme.
 * 
 * This uses DomainColor instead of Compose Color to maintain clean architecture.
 */
data class ExtraColors(
    val bars: DomainColor = DomainColor.Unspecified,
    val onBars: DomainColor = DomainColor.Unspecified,
) {
    // Calculate isBarLight dynamically based on current bars color
    val isBarLight: Boolean
        get() = bars.isLight()
}
