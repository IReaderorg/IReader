package ireader.presentation.core.theme

import kotlin.test.Test
import kotlin.test.assertTrue

class SystemTimeFormatTest {

    @Test
    fun formatsTimestampUsingSystemTimeFormat() {
        val formatted = formatSystemTime(1_700_000_000_000L, null)

        assertTrue(formatted.isNotBlank(), "System time formatting must not be blank")
        assertTrue(formatted.contains(":"), "System time format must include hours and minutes: $formatted")
    }
}
