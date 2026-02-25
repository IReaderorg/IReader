package ireader.domain.config

/**
 * Configuration for Discord webhook integration
 */
data class DiscordConfig(
    val characterArtWebhookUrl: String,
    val enabled: Boolean = true
) {
    companion object {
        fun fromPlatformConfig(config: PlatformConfig): DiscordConfig {
            return DiscordConfig(
                characterArtWebhookUrl = config.getDiscordCharacterArtWebhookUrl(),
                enabled = config.getDiscordCharacterArtWebhookUrl().isNotBlank()
            )
        }
    }
}
