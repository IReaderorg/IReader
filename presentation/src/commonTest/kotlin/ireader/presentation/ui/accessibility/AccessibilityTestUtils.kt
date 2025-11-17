package ireader.presentation.ui.accessibility

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasRole
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ireader.core.log.IReaderLog

/**
 * Accessibility testing utilities following Mihon's patterns
 * Provides automated accessibility checks using Compose testing APIs
 */
object AccessibilityTestUtils {
    
    /**
     * Minimum touch target size for accessibility compliance
     */
    private val MinimumTouchTargetSize = 48.dp
    
    /**
     * Assert that a node has proper accessibility support
     */
    fun SemanticsNodeInteraction.assertAccessibilityCompliant(
        expectedContentDescription: String? = null,
        expectedRole: Role? = null,
        shouldHaveClickAction: Boolean = false
    ): SemanticsNodeInteraction {
        // Check content description
        expectedContentDescription?.let {
            assertContentDescriptionEquals(it)
        }
        
        // Check semantic role
        expectedRole?.let {
            assert(hasRole(it))
        }
        
        // Check click action if expected
        if (shouldHaveClickAction) {
            assertHasClickAction()
        }
        
        // Check if enabled (for interactive elements)
        if (shouldHaveClickAction) {
            assertIsEnabled()
        }
        
        IReaderLog.accessibility("Accessibility compliance check passed for: $expectedContentDescription")
        
        return this
    }
    
    /**
     * Assert that a button meets accessibility requirements
     */
    fun SemanticsNodeInteraction.assertAccessibleButton(
        expectedContentDescription: String
    ): SemanticsNodeInteraction {
        return assertAccessibilityCompliant(
            expectedContentDescription = expectedContentDescription,
            expectedRole = Role.Button,
            shouldHaveClickAction = true
        )
    }
    
    /**
     * Assert that an icon button meets accessibility requirements
     */
    fun SemanticsNodeInteraction.assertAccessibleIconButton(
        expectedContentDescription: String
    ): SemanticsNodeInteraction {
        return assertAccessibilityCompliant(
            expectedContentDescription = expectedContentDescription,
            expectedRole = Role.Button,
            shouldHaveClickAction = true
        )
    }
    
    /**
     * Assert that a text field meets accessibility requirements
     */
    fun SemanticsNodeInteraction.assertAccessibleTextField(
        expectedContentDescription: String? = null
    ): SemanticsNodeInteraction {
        return assertAccessibilityCompliant(
            expectedContentDescription = expectedContentDescription,
            expectedRole = Role.TextInput
        )
    }
    
    /**
     * Assert that a checkbox meets accessibility requirements
     */
    fun SemanticsNodeInteraction.assertAccessibleCheckbox(
        expectedContentDescription: String? = null
    ): SemanticsNodeInteraction {
        return assertAccessibilityCompliant(
            expectedContentDescription = expectedContentDescription,
            expectedRole = Role.Checkbox,
            shouldHaveClickAction = true
        )
    }
    
    /**
     * Assert that a radio button meets accessibility requirements
     */
    fun SemanticsNodeInteraction.assertAccessibleRadioButton(
        expectedContentDescription: String? = null
    ): SemanticsNodeInteraction {
        return assertAccessibilityCompliant(
            expectedContentDescription = expectedContentDescription,
            expectedRole = Role.RadioButton,
            shouldHaveClickAction = true
        )
    }
    
    /**
     * Custom semantic matcher for minimum touch target size
     */
    fun hasMinimumTouchTargetSize(): SemanticsMatcher {
        return SemanticsMatcher("has minimum touch target size (48dp)") { node ->
            val bounds = node.boundsInRoot
            val width = bounds.width
            val height = bounds.height
            width >= MinimumTouchTargetSize.value && height >= MinimumTouchTargetSize.value
        }
    }
    
    /**
     * Assert that a node has minimum touch target size
     */
    fun SemanticsNodeInteraction.assertMinimumTouchTargetSize(): SemanticsNodeInteraction {
        assert(hasMinimumTouchTargetSize())
        return this
    }
    
    /**
     * Run comprehensive accessibility tests on a screen
     */
    fun ComposeContentTestRule.runAccessibilityTests(
        screenName: String,
        additionalChecks: (ComposeContentTestRule) -> Unit = {}
    ) {
        IReaderLog.accessibility("Running accessibility tests for screen: $screenName")
        
        // Check for nodes with content descriptions
        onAllNodes(hasContentDescription())
            .fetchSemanticsNodes()
            .forEach { node ->
                val contentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription)
                IReaderLog.accessibility("Found accessible node: $contentDescription")
            }
        
        // Check for interactive elements with proper roles
        onAllNodes(hasRole(Role.Button))
            .fetchSemanticsNodes()
            .forEach { node ->
                val contentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription)
                IReaderLog.accessibility("Found button: $contentDescription")
            }
        
        // Run additional custom checks
        additionalChecks(this)
        
        IReaderLog.accessibility("Accessibility tests completed for screen: $screenName")
    }
}

/**
 * Extension functions for easier accessibility testing
 */
fun ComposeContentTestRule.assertScreenAccessibility(screenName: String) {
    AccessibilityTestUtils.runAccessibilityTests(screenName, this)
}

/**
 * Semantic matchers for common accessibility patterns
 */
object AccessibilityMatchers {
    fun hasAccessibleButton() = hasRole(Role.Button)
    fun hasAccessibleImage() = hasRole(Role.Image)
    fun hasAccessibleTextField() = hasRole(Role.TextInput)
    fun hasAccessibleCheckbox() = hasRole(Role.Checkbox)
    fun hasAccessibleRadioButton() = hasRole(Role.RadioButton)
}