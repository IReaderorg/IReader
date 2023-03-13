package ireader.presentation.core

expect class PlatformHelper {
     fun copyToClipboard(label: String, content: String)
}