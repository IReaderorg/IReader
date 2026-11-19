package ireader.presentation.ui.reader.components

import ireader.domain.preferences.prefs.ReadingMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CurrentTimeToggleVisibilityTest {

    @Test
    fun visibleOnlyInPageModeWithImmersiveMode() {
        assertTrue(shouldShowCurrentTimeToggle(ReadingMode.Page, immersiveMode = true))

        assertFalse(shouldShowCurrentTimeToggle(ReadingMode.Page, immersiveMode = false))
        assertFalse(shouldShowCurrentTimeToggle(ReadingMode.Continues, immersiveMode = true))
        assertFalse(shouldShowCurrentTimeToggle(ReadingMode.InfiniteScroll, immersiveMode = true))
    }
}
