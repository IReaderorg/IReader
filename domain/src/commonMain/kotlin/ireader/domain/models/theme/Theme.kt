package ireader.domain.models.theme

data class Theme(
    val id: Long,
    val materialColors: DomainColorScheme,
    val extraColors: ExtraColors,
    val isDark: Boolean = false,
)
