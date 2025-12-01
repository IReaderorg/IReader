package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents a tab in the main navigation.
 * This replaces Voyager's Tab interface with a pure Compose Navigation approach.
 */
sealed class AppTab(
    val index: Int,
    val route: String
) {
    abstract val title: String
        @Composable get
    
    abstract val icon: Painter
        @Composable get
    
    @Composable
    abstract fun Content()
    
    data object Library : AppTab(0, "tab_library") {
        override val title: String
            @Composable get() = LibraryScreenSpec.getTitle()
        
        override val icon: Painter
            @Composable get() = LibraryScreenSpec.getIcon()
        
        @Composable
        override fun Content() = LibraryScreenSpec.TabContent()
    }
    
    data object Updates : AppTab(1, "tab_updates") {
        override val title: String
            @Composable get() = UpdateScreenSpec.getTitle()
        
        override val icon: Painter
            @Composable get() = UpdateScreenSpec.getIcon()
        
        @Composable
        override fun Content() = UpdateScreenSpec.TabContent()
    }
    
    data object History : AppTab(2, "tab_history") {
        override val title: String
            @Composable get() = HistoryScreenSpec.getTitle()
        
        override val icon: Painter
            @Composable get() = HistoryScreenSpec.getIcon()
        
        @Composable
        override fun Content() = HistoryScreenSpec.TabContent()
    }
    
    data object Extensions : AppTab(3, "tab_extensions") {
        override val title: String
            @Composable get() = ExtensionScreenSpec.getTitle()
        
        override val icon: Painter
            @Composable get() = ExtensionScreenSpec.getIcon()
        
        @Composable
        override fun Content() = ExtensionScreenSpec.TabContent()
    }
    
    data object More : AppTab(4, "tab_more") {
        override val title: String
            @Composable get() = MoreScreenSpec.getTitle()
        
        override val icon: Painter
            @Composable get() = MoreScreenSpec.getIcon()
        
        @Composable
        override fun Content() = MoreScreenSpec.TabContent()
    }
    
    companion object {
        val entries = listOf(Library, Updates, History, Extensions, More)
        
        fun fromRoute(route: String?): AppTab = when (route) {
            Library.route -> Library
            Updates.route -> Updates
            History.route -> History
            Extensions.route -> Extensions
            More.route -> More
            else -> Library
        }
        
        fun fromIndex(index: Int): AppTab = when (index) {
            0 -> Library
            1 -> Updates
            2 -> History
            3 -> Extensions
            4 -> More
            else -> Library
        }
    }
}
