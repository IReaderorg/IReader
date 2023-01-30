package ireader.domain.models

import android.content.Context
import ireader.domain.models.entities.Category
import ireader.core.prefs.Preference
import ireader.domain.R

// sealed class DisplayMode(val title: Int, val layout: LayoutType, val layoutIndex: Int) {
//    object CompactModel : DisplayMode(R.string.compact_layout, layout = LayoutType.CompactGrid, 0)
//    object GridLayout : DisplayMode(R.string.grid_layout, layout = LayoutType.GridLayout, 1)
//    object ListLayout : DisplayMode(R.string.list_layout, layout = LayoutType.ListLayout, 2)
// }
//
// val layouts = listOf<DisplayMode>(
//    DisplayMode.CompactModel,
//    DisplayMode.GridLayout,
//    DisplayMode.ListLayout,
// )
interface Flag {
    val flag: Long
}

interface Mask<T : Flag> {

    val mask: Long

    val values: Array<T>

    fun getFlag(flags: Long): T? {
        return values.firstOrNull { flags and mask == it.flag }
    }

    fun setFlag(flags: Long, value: Flag): Long {
        return flags and mask.inv() or (value.flag and mask)
    }
}

enum class DisplayMode(override val flag: Long) : Flag {
    ComfortableGrid(0b0001L),
    List(0b0010L),
    CompactGrid(0b0011L),
    OnlyCover(0b00110L);

    companion object : Mask<DisplayMode> {
        override val mask = 0b0111L
        override val values = values()

        val Category.displayMode: DisplayMode
            get() {
                return getFlag(flags) ?: CompactGrid
            }

        fun Category.set(value: DisplayMode): Category {
            return copy(flags = setFlag(flags, value))
        }

        fun Preference<Long>.set(value: DisplayMode): Long {
            val flags = setFlag(get(), value)
            set(flags)
            return flags
        }
    }
}

fun DisplayMode.getLayoutName(context: Context): String {
    return when (this) {
        DisplayMode.CompactGrid -> {
            context.getString(R.string.compact_layout)
        }
        DisplayMode.ComfortableGrid -> {
            context.getString(R.string.comfortable_layout)
        }
        DisplayMode.List -> {
            context.getString(R.string.list_layout)
        }
        DisplayMode.OnlyCover -> {
            context.getString(R.string.cover_only_layout)
        }
    }
}
