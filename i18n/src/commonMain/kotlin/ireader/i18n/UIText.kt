package ireader.i18n

import ireader.i18n.resources.Res
import ireader.i18n.resources.error_unknown
import org.jetbrains.compose.resources.StringResource as ComposeStringResource

/**
 * Sealed class representing UI text that can come from different sources
 */
sealed class UiText {
    /**
     * Dynamic string that is already localized or doesn't need localization
     */
    data class DynamicString(val text: String) : UiText()
    
    /**
     * String resource identified by integer ID (legacy support)
     */
    data class StringResource(val resId: Int) : UiText()
    
    /**
     * String resource using Compose Multiplatform resources
     * @param res The string resource
     * @param args Optional format arguments for the string
     */
    data class MStringResource(val res: ComposeStringResource, val args: Array<out Any> = emptyArray()) : UiText() {
        // Override equals and hashCode to handle array properly
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as MStringResource
            if (res != other.res) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }
        
        override fun hashCode(): Int {
            var result = res.hashCode()
            result = 31 * result + args.contentHashCode()
            return result
        }
    }
    
    /**
     * Exception message as string
     */
    data class ExceptionString(val e: Throwable) : UiText()
}

fun UiText.asString(localizeHelper: LocalizeHelper): String {
    return when (this) {
        is UiText.DynamicString -> text
        is UiText.StringResource -> localizeHelper.localize(resId)
        is UiText.MStringResource -> {
            if (args.isEmpty()) {
                localizeHelper.localize(res)
            } else {
                localizeHelper.localize(res, *args)
            }
        }
        is UiText.ExceptionString -> {
            val eString = e.localizedMessage ?: localizeHelper.localize(Res.string.error_unknown)
            eString.substring(0, eString.length.coerceAtMost(500))
        }
    }
}

