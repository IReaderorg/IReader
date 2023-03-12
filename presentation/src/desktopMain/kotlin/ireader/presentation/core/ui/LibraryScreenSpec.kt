package ireader.presentation.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.i18n.localize
import ireader.i18n.resources.MR

actual object LibraryScreenSpec : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = localize(MR.strings.library_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.Book)
            return remember {
                TabOptions(
                        index = 0u,
                        title = title,
                        icon = icon,
                )
            }

        }

    @Composable
    override fun Content() {

    }
}