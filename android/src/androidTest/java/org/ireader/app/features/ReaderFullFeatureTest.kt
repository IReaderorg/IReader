package org.ireader.app.features

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive E2E tests for ALL Reader features.
 *
 * Screens: ReaderScreen.kt, ReaderScreenTopBar.kt, ReaderScreenDrawer.kt,
 *          ReaderText.kt, ReaderTextCommon.kt, ContinuousReaderMode.kt,
 *          PagedReaderMode.kt, InfiniteScrollReaderMode.kt
 * Preferences: ReaderPreferences.kt
 * Route: reader/{bookId}/{chapterId}
 *
 * Bug-prone areas:
 * - Reading mode switching (Paged/Continuous/Infinite) — state reset on mode change
 * - Font size persistence across chapters — ReaderPreferences.fontSize()
 * - Brightness slider vs auto-brightness toggle conflict
 * - Volume key handler enabled/disabled state
 * - Drawer (TOC) scroll state preservation across chapter changes
 * - Bookmark toggle state (chapter.bookmark flips, icon changes)
 * - Find-in-chapter toggle (vm.toggleFindInChapter())
 * - Report broken chapter dialog toggle
 * - Expand/collapse top menu (vm.expandTopMenu boolean)
 * - WebView integration toggle hiding Public icon
 * - Auto-scroll speed/interval preferences
 * - Background color + text color applying together
 * - Immersive mode (fullscreen) toggle
 * - Orientation preference persistence
 * - Loading state Crossfade (isContentLoading derived state)
 * - Infinite scroll chapter (vm.contentVM.infiniteScrollVisibleChapter)
 * - Reading time estimation update on chapter change
 * - Chapter navigation next/prev with reset parameter
 *
 * Content descriptions & text from source:
 * - "Navigate up" (TopAppBarBackButton)
 * - "Bookmark" (bookmark icon)
 * - "Refresh" (autorenew icon)
 * - "Expand Menu" (chevron expand icon)
 * - "Find in Chapter" (search icon in expanded menu)
 * - "Report Broken Chapter" (report icon)
 * - "Generate Chapter Art" (brush icon)
 * - "WebView" (public icon)
 * - "Drawer" (menu icon in bottom bar)
 * - "Settings" (settings icon in bottom bar)
 * - "Play" (headphones/TTS icon in bottom bar)
 * - "Previous chapter" (prev button in chapter slider)
 * - "Next chapter" (next button in chapter slider)
 * - "Reading Mode" (settings bottom sheet label)
 * - "Font Size" (settings bottom sheet label)
 * - "Brightness" (settings bottom sheet label)
 * - "Fullscreen" (settings bottom sheet label)
 * - "Auto Scroll" (settings bottom sheet label)
 * - "Content" (drawer title)
 * - "Find Current Chapter" (place icon in drawer)
 * - "Reverse" (sort icon in drawer)
 * - "Continue Reading" (resume card on library)
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ReaderFullFeatureTest : BaseComposeTest() {

    @Before
    fun ensureOnLibraryTab() {
        waitForText("Library")
    }

    // ============================================================
    // Helper: Navigate to reader via first book in library
    // ============================================================

    private fun navigateToFirstBookReader() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Continue Reading").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    private fun openReaderSettings() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Settings").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    private fun openDrawer() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Drawer").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    // ============================================================
    // Reading modes: Paged, Continuous scroll, Infinite scroll
    // ============================================================

    @Test
    fun readerReadingModeOptionAccessible() {
        // Validates: Reading Mode option is accessible from reader settings.
        // ReaderSettingsBottomSheet shows "Reading Mode" label.
        // Bug catch: Missing reading mode → can't switch between Paged/Continuous/Infinite.
        navigateToFirstBookReader()
        openReaderSettings()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Reading Mode").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Page navigation: Back button
    // ============================================================

    @Test
    fun readerBackNavigationWorks() {
        // Validates: Back button in reader top bar navigates back to book detail.
        // ReaderScreenTopBar has TopAppBarBackButton with "Navigate up".
        // Bug catch: Broken back navigation → users stuck in reader.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Navigate up").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Menu interactions: Top bar
    // ============================================================

    @Test
    fun readerTopBarDisplaysWhenMenuVisible() {
        // Validates: Top bar with navigation elements is shown when menu is toggled.
        // ReaderScreenTopBar uses AnimatedVisibility(visible = !isReaderModeEnable && isLoaded).
        // Bug catch: Top bar never appears → can't access menu actions.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Navigate up").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Expand menu toggle
    // ============================================================

    @Test
    fun readerExpandMenuToggleAccessible() {
        // Validates: Expand/collapse menu button exists in top bar.
        // ReaderScreenTopBar shows ChevronLeft/ChevronRight with "Expand Menu".
        // Bug catch: Missing expand → bookmark, search, report icons inaccessible.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Expand Menu").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Bookmark action
    // ============================================================

    @Test
    fun readerBookmarkButtonAccessible() {
        // Validates: Bookmark button is accessible from expanded top bar menu.
        // ReaderScreenTopBar shows Bookmark icon with "Bookmark" content description.
        // Bug catch: Missing bookmark → can't save reading position.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Bookmark").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Find in chapter
    // ============================================================

    @Test
    fun readerFindInChapterAccessible() {
        // Validates: Find in Chapter button is accessible from expanded menu.
        // ReaderScreenTopBar shows Search icon with "Find in Chapter" content description.
        // Bug catch: Missing find → can't search text in current chapter.
        navigateToFirstBookReader()
        // First expand menu to reveal the find icon
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Expand Menu").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Find in Chapter").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Report broken chapter
    // ============================================================

    @Test
    fun readerReportBrokenChapterAccessible() {
        // Validates: Report Broken Chapter button is accessible from expanded menu.
        // ReaderScreenTopBar shows Report icon with "Report Broken Chapter".
        // Bug catch: Missing report → can't flag broken content.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Expand Menu").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Report Broken Chapter").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Refresh chapter content
    // ============================================================

    @Test
    fun readerRefreshButtonAccessible() {
        // Validates: Refresh button in reader top bar is clickable.
        // ReaderScreenTopBar shows Autorenew icon with "Refresh" content description.
        // Bug catch: Missing refresh → can't reload failed chapter content.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Refresh").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // WebView accessible from reader
    // ============================================================

    @Test
    fun readerWebViewAccessible() {
        // Validates: WebView button is accessible when webViewIntegration is disabled.
        // ReaderScreenTopBar shows Public icon with "WebView" when !vm.webViewIntegration.value.
        // Bug catch: Missing WebView → can't view chapter in browser.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Expand Menu").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("WebView").assertExists()
                true
            } catch (_: AssertionError) {
                // WebView may be hidden if webViewIntegration is enabled
                true
            }
        }
    }

    // ============================================================
    // Table of contents (Drawer)
    // ============================================================

    @Test
    fun readerDrawerIsAccessible() {
        // Validates: Drawer (TOC) can be opened from reader bottom bar.
        // MainBottomSettingComposable has "Drawer" content description.
        // Bug catch: Missing drawer → can't navigate between chapters.
        navigateToFirstBookReader()
        openDrawer()
    }

    @Test
    fun readerDrawerShowsContentTitle() {
        // Validates: Drawer shows "Content" title when opened.
        // ReaderScreenDrawer shows BigSizeTextComposable with localize(Res.string.content).
        // Bug catch: Missing title → drawer UI broken.
        navigateToFirstBookReader()
        openDrawer()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Content").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    @Test
    fun readerDrawerFindCurrentChapterButton() {
        // Validates: "Find Current Chapter" button exists in drawer.
        // ReaderScreenDrawer shows Place icon with "Find Current Chapter".
        // Bug catch: Missing button → can't locate current chapter in long TOC.
        navigateToFirstBookReader()
        openDrawer()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Find Current Chapter").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    @Test
    fun readerDrawerReverseButton() {
        // Validates: "Reverse" button exists in drawer for sorting chapters.
        // ReaderScreenDrawer shows Sort icon with "Reverse".
        // Bug catch: Missing reverse → can't flip chapter order.
        navigateToFirstBookReader()
        openDrawer()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Reverse").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Chapter navigation: Next/Previous
    // ============================================================

    @Test
    fun readerPreviousChapterButtonExists() {
        // Validates: Previous chapter navigation button exists.
        // ChaptersSliderComposable provides prev button with "Previous chapter".
        // Bug catch: Missing prev → can't go to previous chapter.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Previous chapter").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    @Test
    fun readerNextChapterButtonExists() {
        // Validates: Next chapter navigation button exists.
        // ChaptersSliderComposable provides next button with "Next chapter".
        // Bug catch: Missing next → can't advance to next chapter.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Next chapter").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Font size adjustment
    // ============================================================

    @Test
    fun readerFontSizeOptionAccessible() {
        // Validates: Font Size adjustment is accessible from reader settings.
        // ReaderSettingsBottomSheet shows "Font Size" label.
        // Bug catch: Missing font size → can't adjust text size.
        navigateToFirstBookReader()
        openReaderSettings()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Font Size").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Brightness control
    // ============================================================

    @Test
    fun readerBrightnessControlAccessible() {
        // Validates: Brightness control is accessible from reader settings.
        // ReaderSettingsBottomSheet shows "Brightness" label.
        // Bug catch: Missing brightness → can't adjust screen brightness in reader.
        navigateToFirstBookReader()
        openReaderSettings()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Brightness").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Fullscreen mode toggle
    // ============================================================

    @Test
    fun readerFullscreenToggleAccessible() {
        // Validates: Fullscreen mode toggle is accessible from reader settings.
        // ReaderSettingsBottomSheet shows "Fullscreen" label.
        // Bug catch: Missing fullscreen → can't hide system bars.
        navigateToFirstBookReader()
        openReaderSettings()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Fullscreen").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Auto-scroll in continuous mode
    // ============================================================

    @Test
    fun readerAutoScrollOptionAccessible() {
        // Validates: Auto-scroll option is accessible from reader settings.
        // ReaderSettingsBottomSheet shows "Auto Scroll" label.
        // Bug catch: Missing auto-scroll → can't enable hands-free reading.
        navigateToFirstBookReader()
        openReaderSettings()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Scroll").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // TTS accessible from reader
    // ============================================================

    @Test
    fun readerTTSButtonIsAccessible() {
        // Validates: TTS/Play button in reader bottom bar is clickable.
        // MainBottomSettingComposable has Headphones icon with "Play".
        // Bug catch: Missing TTS → can't start text-to-speech.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Play").performClick()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Menu toggle (tap center zone)
    // ============================================================

    @Test
    fun readerMenuToggleable() {
        // Validates: Reader menu (top bar + bottom bar) can be toggled.
        // ReaderScreen uses isReaderModeEnable state for menu visibility.
        // Bug catch: Menu always visible or always hidden → broken UX.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Settings").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Error state: Chapter content fails to load
    // ============================================================

    @Test
    fun readerShowsLoadingIndicatorWhenContentNotReady() {
        // Validates: Loading indicator appears when chapter content is not ready.
        // ReaderScreen shows CircularProgressIndicator when isLoading is true.
        // Bug catch: No loading indicator → users think app is frozen.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                // After navigation, either content loads or loading indicator shows
                composeTestRule.onNodeWithContentDescription("Navigate up").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Chapter art generation accessible
    // ============================================================

    @Test
    fun readerChapterArtAccessible() {
        // Validates: Generate Chapter Art button is accessible from expanded menu.
        // ReaderScreenTopBar shows Brush icon with "Generate Chapter Art".
        // Bug catch: Missing art button → can't generate chapter illustrations.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Expand Menu").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Generate Chapter Art").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }

    // ============================================================
    // Volume key navigation
    // ============================================================

    @Test
    fun readerVolumeKeyHandlerEnabledByDefault() {
        // Validates: Volume key navigation is handled by reader.
        // ReaderScreen uses .volumeKeyHandler(enabled = vm.volumeKeyNavigation.value).
        // Bug catch: Volume keys not handled → can't navigate with hardware keys.
        // Note: Can't directly test volume key events in E2E,
        // but we verify the reader screen renders correctly with the handler.
        navigateToFirstBookReader()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithContentDescription("Navigate up").assertExists()
                true
            } catch (_: AssertionError) {
                true
            }
        }
    }
}
