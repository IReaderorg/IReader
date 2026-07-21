package ireader.domain.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class StringFormatUtilsTest {

    @Test
    fun formatDecimalShouldFormatWithTwoDecimals() {
        assertEquals("3.14", 3.14159.formatDecimal(2))
        assertEquals("3.15", 3.145.formatDecimal(2))
    }

    @Test
    fun formatDecimalShouldFormatWithZeroDecimals() {
        assertEquals("3", 3.14.formatDecimal(0))
        assertEquals("4", 3.5.formatDecimal(0))
    }

    @Test
    fun formatDecimalShouldPadWithZeros() {
        assertEquals("3.10", 3.1.formatDecimal(2))
        assertEquals("3.00", 3.0.formatDecimal(2))
    }

    @Test
    fun formatDecimalShouldHandleNegativeDecimals() {
        assertEquals("3.14159", 3.14159.formatDecimal(-1))
    }

    @Test
    fun formatRatingWithReviewsShouldFormatCorrectly() {
        assertEquals("4.5 (100 reviews)", formatRatingWithReviews(4.5f, 100))
        assertEquals("0.0 (0 reviews)", formatRatingWithReviews(0f, 0))
    }

    @Test
    fun formatRatingShortShouldFormatCorrectly() {
        assertEquals("4.5 (100)", formatRatingShort(4.5f, 100))
        assertEquals("0.0 (0)", formatRatingShort(0f, 0))
    }

    @Test
    fun formatCurrencyShouldFormatCorrectly() {
        assertEquals("$ 3.14", formatCurrency("$", 3.14))
        assertEquals("EUR 100.00", formatCurrency("EUR", 100.0))
    }

    @Test
    fun formatPriceShouldFormatCorrectly() {
        assertEquals("$3.14", formatPrice(3.14))
        assertEquals("€10.00", formatPrice(10.0, "€"))
    }

    @Test
    fun formatCompactNumberShouldFormatThousands() {
        assertEquals("1.0K", formatCompactNumber(1000))
        assertEquals("1.5K", formatCompactNumber(1500))
        assertEquals("999", formatCompactNumber(999))
    }

    @Test
    fun formatCompactNumberShouldFormatMillions() {
        assertEquals("1.0M", formatCompactNumber(1000000))
        assertEquals("2.5M", formatCompactNumber(2500000))
    }

    @Test
    fun formatDonationAmountShouldFormatCorrectly() {
        assertEquals("$1.5K", formatDonationAmount(1500.0))
        assertEquals("$100", formatDonationAmount(100.0))
        assertEquals("$3.14", formatDonationAmount(3.14))
    }

    @Test
    fun formatBytesKmpShouldFormatBytes() {
        assertEquals("500 B", formatBytesKmp(500L))
    }

    @Test
    fun formatBytesKmpShouldFormatKB() {
        assertEquals("1.0 KB", formatBytesKmp(1024L))
        assertEquals("1.5 KB", formatBytesKmp(1536L))
    }

    @Test
    fun formatBytesKmpShouldFormatMB() {
        assertEquals("1.00 MB", formatBytesKmp(1024L * 1024L))
        assertEquals("5.50 MB", formatBytesKmp(1024L * 1024L * 5 + 1024L * 512))
    }

    @Test
    fun formatBytesKmpShouldFormatGB() {
        assertEquals("1.00 GB", formatBytesKmp(1024L * 1024L * 1024L))
    }

    @Test
    fun formatSpeedKmpShouldFormatBytesPerSecond() {
        assertEquals("500 B/s", formatSpeedKmp(500f))
    }

    @Test
    fun formatSpeedKmpShouldFormatKBPerSecond() {
        assertEquals("1.0 KB/s", formatSpeedKmp(1024f))
    }

    @Test
    fun formatSpeedKmpShouldFormatMBPerSecond() {
        assertEquals("1.00 MB/s", formatSpeedKmp(1024f * 1024f))
    }

    @Test
    fun formatPercentageShouldFormatCorrectly() {
        assertEquals("50.0%", formatPercentage(50.0))
        assertEquals("33.33%", formatPercentage(33.33, 2))
    }

    @Test
    fun formatMultiplierShouldFormatCorrectly() {
        assertEquals("2.0x", formatMultiplier(2f))
        assertEquals("1.5x", formatMultiplier(1.5f))
    }

    @Test
    fun formatSecondsShouldFormatCorrectly() {
        assertEquals("5s", formatSeconds(5.0))
        assertEquals("5.5s", formatSeconds(5.5))
    }

    @Test
    fun padZeroShouldPadCorrectly() {
        assertEquals("05", 5.padZero(2))
        assertEquals("005", 5.padZero(3))
        assertEquals("123", 123.padZero(2)) // Already longer than pad length
    }

    @Test
    fun longPadZeroShouldPadCorrectly() {
        assertEquals("05", 5L.padZero(2))
        assertEquals("005", 5L.padZero(3))
    }
}
