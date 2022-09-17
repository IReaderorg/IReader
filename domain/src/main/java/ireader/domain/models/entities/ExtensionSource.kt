package ireader.common.models.entities

import androidx.compose.runtime.Composable

data class ExtensionSource(
    val id: Long,
    val name: String,
    val key: String,
    val owner: String,
    val source: String,
    val lastUpdate:Long,
    val isEnable:Boolean,
) {

    fun visibleName(): String {
        return when {
            id < 0 -> "IReader"
            else -> name
        }
    }
}