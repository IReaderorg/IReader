package ir.kazemcodes.infinity.core.utils


fun UiText?.asString(): String {
    return when(this) {
        is UiText.DynamicString -> this.value
        else -> {return UiText.unknownErrors()}
    }
}