# Accessibility Implementation Guide

This document outlines the accessibility improvements implemented across the IReader application UI components.

## Overview

All UI components have been enhanced to meet WCAG AA accessibility standards, ensuring the application is usable by people with disabilities, including those using screen readers, keyboard navigation, and other assistive technologies.

## Implemented Accessibility Features

### 1. Content Descriptions

All interactive elements now include proper content descriptions for screen readers:

#### PreferenceRow Components
- **PreferenceRow**: Includes title, subtitle, and role information
- **SwitchPreference**: Includes current state (Enabled/Disabled)
- **SliderPreference**: Includes current value and range information
- **ChipPreference**: Includes selected option information
- **ChoicePreference**: Includes current selection
- **ColorPreference**: Includes color value and long-press hint

#### Enhanced Components
- **RowPreference**: Full content description with title and subtitle
- **NavigationRowPreference**: Indicates navigation action
- **SectionHeader**: Properly labeled section headers
- **EnhancedCard**: Appropriate role and description

#### Other Components
- **AppIconButton**: Descriptive labels for all icon buttons
- **VisibilityIcon**: Clear show/hide descriptions
- **FilterChip**: Selection state information

### 2. Semantic Roles

All interactive elements have appropriate semantic roles:

- **Button**: Standard clickable elements
- **Switch**: Toggle controls
- **Slider**: Range input controls
- **RadioButton**: Single selection from multiple options
- **DropdownList**: Selection dialogs
- **Checkbox**: Multi-selection controls

### 3. Touch Targets

All interactive elements meet the minimum 48dp touch target size:

#### Updated Components
- **PreferenceRow**: Minimum 56dp height (72dp with subtitle)
- **SliderPreference**: Minimum 80dp height (96dp with subtitle)
- **AppIconButton**: 48dp × 48dp touch target
- **Dialog Radio Buttons**: Minimum 48dp height
- **FilterChips**: Adequate padding for 48dp minimum
- **IconButtons**: 48dp × 48dp minimum size

### 4. Color Contrast

All text and UI elements meet WCAG AA contrast requirements:

#### Contrast Ratios
- **Normal Text**: 4.5:1 minimum (implemented via ContentAlpha)
- **Large Text**: 3:1 minimum (implemented via ContentAlpha)
- **UI Components**: 3:1 minimum (implemented via Material Design 3 colors)

#### Alpha Values
- **High Emphasis**: 1.00 (100% opacity)
- **Medium Emphasis**: 0.74 (high contrast) / 0.60 (low contrast)
- **Disabled State**: 0.38 (exempt from WCAG requirements)
- **Decorative Elements**: 0.12 (non-text elements)

#### Accessibility Utilities
A new `AccessibilityUtils` class provides:
- Contrast ratio calculation
- WCAG AA compliance checking
- Accessible color generation
- Minimum alpha constants

## Usage Guidelines

### For Developers

#### Adding Content Descriptions
```kotlin
@Composable
fun MyComponent() {
    Box(
        modifier = Modifier.semantics {
            contentDescription = "Descriptive text for screen readers"
            role = Role.Button
        }
    )
}
```

#### Ensuring Touch Targets
```kotlin
@Composable
fun MyButton() {
    Button(
        modifier = Modifier
            .heightIn(min = 48.dp) // Minimum touch target
            .widthIn(min = 48.dp)
    ) {
        // Button content
    }
}
```

#### Checking Color Contrast
```kotlin
import ireader.presentation.ui.core.theme.AccessibilityUtils

// Check if colors meet WCAG AA
val meetsStandard = AccessibilityUtils.meetsWCAGAANormalText(
    foreground = textColor,
    background = backgroundColor
)

// Or use extension function
val isAccessible = textColor.hasAccessibleContrastWith(backgroundColor)
```

### For Designers

When creating new themes or color schemes:

1. **Test Contrast**: Use the `AccessibilityUtils` to verify all text meets WCAG AA standards
2. **Avoid Low Alpha**: Don't use alpha values below 0.60 for important text
3. **Use Material Colors**: Material Design 3 color scheme automatically provides accessible colors
4. **Test Both Themes**: Verify contrast in both light and dark modes

## Testing Accessibility

### Manual Testing
1. Enable TalkBack (Android) or VoiceOver (iOS)
2. Navigate through the app using only screen reader
3. Verify all elements are announced correctly
4. Check that all actions can be performed

### Automated Testing
1. Use Android Accessibility Scanner
2. Run contrast checking tools
3. Verify touch target sizes with layout inspector

### Checklist
- [ ] All interactive elements have content descriptions
- [ ] All interactive elements have appropriate semantic roles
- [ ] All touch targets are at least 48dp × 48dp
- [ ] All text meets WCAG AA contrast requirements (4.5:1 for normal, 3:1 for large)
- [ ] All UI components meet 3:1 contrast requirement
- [ ] App is fully navigable with screen reader
- [ ] App is fully navigable with keyboard (desktop)

## Component-Specific Notes

### PreferenceRow
- Automatically builds content descriptions from title and subtitle
- Merges descendant semantics for cleaner screen reader experience
- Minimum 56dp height ensures adequate touch target

### SliderPreference
- Includes current value in content description
- Slider thumb is large enough for easy manipulation
- Visual feedback during interaction

### SwitchPreference
- Announces current state (Enabled/Disabled)
- Toggle action is clear to screen reader users
- Visual state is also indicated by color

### ChoicePreference
- Dialog items have proper radio button semantics
- Selection state is announced
- Minimum 48dp height for each option

### ColorPreference
- Includes hex color value in description
- Long-press action is announced
- Color preview has adequate size

## Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material Design Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)

## Future Improvements

Potential enhancements for future releases:

1. **Haptic Feedback**: Add vibration feedback for important actions
2. **Focus Indicators**: Enhanced visual focus indicators for keyboard navigation
3. **Reduced Motion**: Respect system reduced motion preferences
4. **High Contrast Mode**: Additional high contrast theme option
5. **Font Scaling**: Better support for large font sizes
6. **Screen Reader Hints**: Add hints for complex interactions

## Maintenance

When adding new components:

1. Add appropriate content descriptions
2. Set semantic roles
3. Ensure minimum 48dp touch targets
4. Verify color contrast
5. Test with screen reader
6. Update this documentation

## Contact

For accessibility questions or issues, please refer to the project's issue tracker or contact the development team.
