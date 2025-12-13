package ireader.domain.plugins

/**
 * Type aliases to re-export plugin-api types for backward compatibility.
 * The domain module uses these types internally while the plugin-api module
 * provides the public API for plugin developers.
 */

// Re-export core plugin types
typealias Plugin = ireader.plugin.api.Plugin
typealias PluginManifest = ireader.plugin.api.PluginManifest
typealias PluginAuthor = ireader.plugin.api.PluginAuthor
typealias PluginType = ireader.plugin.api.PluginType
typealias PluginPermission = ireader.plugin.api.PluginPermission
typealias Platform = ireader.plugin.api.Platform
typealias PluginContext = ireader.plugin.api.PluginContext
typealias PluginPreferencesStore = ireader.plugin.api.PluginPreferencesStore

// Re-export monetization types
typealias PluginMonetization = ireader.plugin.api.PluginMonetization
typealias PremiumFeature = ireader.plugin.api.PremiumFeature

// Re-export theme plugin types
typealias ThemePlugin = ireader.plugin.api.ThemePlugin
typealias ThemeColorScheme = ireader.plugin.api.ThemeColorScheme
typealias ThemeExtraColors = ireader.plugin.api.ThemeExtraColors
typealias ThemeTypography = ireader.plugin.api.ThemeTypography
typealias ThemeBackgrounds = ireader.plugin.api.ThemeBackgrounds

// Re-export TTS plugin types
typealias TTSPlugin = ireader.plugin.api.TTSPlugin
typealias VoiceConfig = ireader.plugin.api.VoiceConfig
// Note: VoiceModel is NOT aliased here because domain has its own VoiceModel
// in ireader.domain.models.tts with more detailed fields for Piper TTS.
// Plugin developers should use ireader.plugin.api.VoiceModel directly.
typealias PluginVoiceModel = ireader.plugin.api.VoiceModel
typealias PluginVoiceGender = ireader.plugin.api.VoiceGender
typealias AudioFormat = ireader.plugin.api.AudioFormat
typealias AudioEncoding = ireader.plugin.api.AudioEncoding
typealias AudioStream = ireader.plugin.api.AudioStream

// Re-export translation plugin types
typealias TranslationPlugin = ireader.plugin.api.TranslationPlugin
typealias LanguagePair = ireader.plugin.api.LanguagePair

// Re-export feature plugin types
typealias FeaturePlugin = ireader.plugin.api.FeaturePlugin
typealias PluginMenuItem = ireader.plugin.api.PluginMenuItem
typealias PluginScreen = ireader.plugin.api.PluginScreen
typealias ReaderContext = ireader.plugin.api.ReaderContext
typealias PluginAction = ireader.plugin.api.PluginAction
