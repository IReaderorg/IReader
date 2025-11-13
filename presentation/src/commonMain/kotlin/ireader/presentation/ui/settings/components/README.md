# Unified Settings Component Library

This library provides reusable, consistent Material3 components for building settings screens in IReader.

## Overview

The unified settings component library ensures:
- **Consistent Material3 styling** across all settings screens
- **Standardized spacing, typography, and colors**
- **Accessibility compliance** with proper semantics and content descriptions
- **Reusable patterns** for common settings UI elements
- **Easy maintenance** with centralized component definitions

## Components

### SettingsComponents.kt

#### SettingsSectionHeader
Section header for grouping related settings with an optional icon.

```kotlin
SettingsSectionHeader(
    title = "Account",
    icon = Icons.Filled.AccountCircle
)
```

**Parameters:**
- `title: String` - The section title text
- `icon: ImageVector?` - Optional icon to display before the title
- `modifier: Modifier` - Optional modifier for customization

**Design:**
- Primary color for icon and text
- Bold typography
- Horizontal divider line
- 16dp horizontal padding, 16dp vertical padding

---

#### SettingsItem
Standard settings item with title, description, icon, and navigation indicator.

```kotlin
SettingsItem(
    title = "Profile & Sync",
    description = "Manage your account and sync reading progress",
    icon = Icons.Outlined.AccountCircle,
    onClick = { /* navigate to profile */ }
)
```

**Parameters:**
- `title: String` - The main title text
- `description: String?` - Optional descriptive text below the title
- `icon: ImageVector?` - Optional leading icon
- `onClick: () -> Unit` - Click handler for the item
- `modifier: Modifier` - Optional modifier for customization
- `enabled: Boolean` - Whether the item is clickable (default: true)
- `showNavigationIcon: Boolean` - Whether to show the chevron icon (default: true)

**Design:**
- Surface with 1dp tonal elevation
- 12dp rounded corners
- 16dp horizontal padding, 12dp vertical padding
- Chevron right icon for navigation
- Disabled state with 38% opacity

---

#### SettingsSwitchItem
Settings item with a switch control for boolean preferences.

```kotlin
SettingsSwitchItem(
    title = "Show Empty Categories",
    description = "Display categories with no books",
    checked = showEmptyCategories,
    onCheckedChange = { showEmptyCategories = it }
)
```

**Parameters:**
- `title: String` - The main title text
- `description: String?` - Optional descriptive text below the title
- `icon: ImageVector?` - Optional leading icon
- `checked: Boolean` - Current switch state
- `onCheckedChange: (Boolean) -> Unit` - Callback when switch state changes
- `modifier: Modifier` - Optional modifier for customization
- `enabled: Boolean` - Whether the switch is interactive (default: true)

**Design:**
- Same layout as SettingsItem
- Material3 Switch with primary colors
- Clickable row toggles the switch
- Accessibility role: Switch

---

#### SettingsItemWithTrailing
Settings item with custom trailing content.

```kotlin
SettingsItemWithTrailing(
    title = "Theme",
    description = "Choose app theme",
    icon = Icons.Outlined.DarkMode,
    onClick = { /* show theme picker */ },
    trailingContent = {
        Text("Dark", style = MaterialTheme.typography.bodyMedium)
    }
)
```

**Parameters:**
- `title: String` - The main title text
- `description: String?` - Optional descriptive text below the title
- `icon: ImageVector?` - Optional leading icon
- `onClick: () -> Unit` - Click handler for the item
- `modifier: Modifier` - Optional modifier for customization
- `enabled: Boolean` - Whether the item is clickable (default: true)
- `trailingContent: @Composable () -> Unit` - Custom composable content to display at the end

**Use Cases:**
- Displaying current selection (e.g., "English", "Dark Mode")
- Custom badges or indicators
- Action buttons

---

#### SettingsHighlightCard
Highlighted settings card for important features or status.

```kotlin
SettingsHighlightCard(
    title = "IReader",
    description = "Your personal book reading companion",
    icon = Icons.Filled.MenuBook,
    onClick = { /* show app info */ },
    containerColor = MaterialTheme.colorScheme.surfaceVariant
)
```

**Parameters:**
- `title: String` - The main title text
- `description: String?` - Optional descriptive text below the title
- `icon: ImageVector?` - Optional leading icon (displayed in a circular container)
- `onClick: () -> Unit` - Click handler for the card
- `modifier: Modifier` - Optional modifier for customization
- `containerColor: Color` - Background color of the card

**Design:**
- 16dp rounded corners
- Icon in circular primary-colored container
- Larger padding (16dp all sides)
- Semi-transparent background

---

#### SettingsDivider
Settings divider for visual separation between groups.

```kotlin
SettingsDivider()
```

**Design:**
- 1dp thickness
- Outline variant color with 50% opacity
- 8dp vertical padding

---

#### SettingsSpacer
Settings spacer for vertical spacing between sections.

```kotlin
SettingsSpacer(height = 24.dp)
```

**Parameters:**
- `height: Dp` - The height of the spacer (default: 16.dp)
- `modifier: Modifier` - Optional modifier for customization

---

### SettingsDialogs.kt

#### SettingsConfirmationDialog
Standard confirmation dialog for destructive or important actions.

```kotlin
SettingsConfirmationDialog(
    title = "Delete Category?",
    message = "Are you sure you want to delete this category?",
    confirmText = "Delete",
    icon = Icons.Default.DeleteForever,
    isDestructive = true,
    onConfirm = { /* delete category */ },
    onDismiss = { /* close dialog */ }
)
```

**Parameters:**
- `title: String` - The dialog title
- `message: String` - The confirmation message
- `confirmText: String` - Text for the confirm button (default: "Confirm")
- `dismissText: String` - Text for the dismiss button (default: "Cancel")
- `icon: ImageVector?` - Optional icon to display in the title
- `onConfirm: () -> Unit` - Callback when user confirms
- `onDismiss: () -> Unit` - Callback when user dismisses
- `isDestructive: Boolean` - Whether this is a destructive action (uses error colors)

**Design:**
- Icon in title row
- Error colors for destructive actions
- Primary colors for non-destructive actions
- 8dp rounded button corners

---

#### SettingsTextInputDialog
Text input dialog for entering or editing text values.

```kotlin
SettingsTextInputDialog(
    title = "Rename Category",
    initialValue = "Fantasy",
    label = "Category Name",
    placeholder = "Enter new category name",
    confirmText = "Rename",
    icon = Icons.Default.Edit,
    validator = { text ->
        when {
            text.isEmpty() -> "Name cannot be empty"
            text.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    },
    onConfirm = { newName -> /* save new name */ },
    onDismiss = { /* close dialog */ }
)
```

**Parameters:**
- `title: String` - The dialog title
- `initialValue: String` - Initial text value (default: "")
- `label: String` - Label for the text field
- `placeholder: String` - Placeholder text (default: "")
- `confirmText: String` - Text for the confirm button (default: "Confirm")
- `dismissText: String` - Text for the dismiss button (default: "Cancel")
- `icon: ImageVector?` - Optional icon to display in the title
- `validator: ((String) -> String?)?` - Optional validation function that returns error message or null
- `onConfirm: (String) -> Unit` - Callback when user confirms with the entered text
- `onDismiss: () -> Unit` - Callback when user dismisses

**Features:**
- Real-time validation with error messages
- Clears error when user starts typing
- Keyboard action (Done) triggers confirmation
- Disabled confirm button when input is blank
- 12dp rounded text field corners

---

#### SettingsSingleChoiceDialog
Single choice dialog for selecting one option from a list.

```kotlin
SettingsSingleChoiceDialog(
    title = "Select Theme",
    options = listOf("Light", "Dark", "System"),
    selectedIndex = 1,
    optionLabel = { it },
    onOptionSelected = { index, option -> /* apply theme */ },
    onDismiss = { /* close dialog */ }
)
```

**Parameters:**
- `title: String` - The dialog title
- `options: List<T>` - List of options to choose from
- `selectedIndex: Int` - Currently selected option index
- `optionLabel: (T) -> String` - Function to get label for each option
- `onOptionSelected: (Int, T) -> Unit` - Callback when an option is selected
- `onDismiss: () -> Unit` - Callback when dialog is dismissed

**Design:**
- Radio buttons for single selection
- Automatically dismisses on selection
- Scrollable list for many options
- Accessibility role: RadioButton

---

#### SettingsMultiChoiceDialog
Multi-choice dialog for selecting multiple options from a list.

```kotlin
SettingsMultiChoiceDialog(
    title = "Select Languages",
    options = listOf("English", "Spanish", "French", "German"),
    selectedIndices = setOf(0, 2),
    optionLabel = { it },
    onConfirm = { selectedIndices -> /* save selection */ },
    onDismiss = { /* close dialog */ }
)
```

**Parameters:**
- `title: String` - The dialog title
- `options: List<T>` - List of options to choose from
- `selectedIndices: Set<Int>` - Currently selected option indices
- `optionLabel: (T) -> String` - Function to get label for each option
- `onConfirm: (Set<Int>) -> Unit` - Callback when user confirms with selected indices
- `onDismiss: () -> Unit` - Callback when dialog is dismissed

**Design:**
- Checkboxes for multiple selection
- Confirm/Cancel buttons
- Scrollable list for many options
- Accessibility role: Checkbox

---

## Design Principles

### Spacing
- **Section headers**: 16dp horizontal, 16dp vertical padding
- **Settings items**: 16dp horizontal, 4dp vertical padding (between items)
- **Item content**: 16dp horizontal, 12dp vertical padding
- **Icon spacing**: 16dp after icon
- **Description spacing**: 2dp between title and description

### Typography
- **Section headers**: `titleMedium`, Bold
- **Item titles**: `bodyLarge`, SemiBold
- **Item descriptions**: `bodySmall`
- **Dialog titles**: `headlineSmall`
- **Dialog content**: `bodyLarge`

### Colors
- **Section headers**: Primary color
- **Item titles**: OnSurface
- **Item descriptions**: OnSurfaceVariant
- **Icons**: OnSurfaceVariant (items), Primary (headers)
- **Disabled state**: 38% opacity
- **Destructive actions**: Error color scheme

### Shapes
- **Settings items**: 12dp rounded corners
- **Highlight cards**: 16dp rounded corners
- **Buttons**: 8dp rounded corners
- **Text fields**: 12dp rounded corners
- **Icon containers**: CircleShape

### Elevation
- **Settings items**: 1dp tonal elevation
- **Highlight cards**: 0dp (uses background color)

### Accessibility
- All components include proper semantic roles
- Content descriptions for screen readers
- Minimum touch target size (48dp)
- Keyboard navigation support
- Clear focus indicators

---

## Usage Examples

### Basic Settings Screen

```kotlin
@Composable
fun MySettingsScreen() {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            SettingsSectionHeader(
                title = "General",
                icon = Icons.Filled.Settings
            )
        }
        
        // Switch item
        item {
            SettingsSwitchItem(
                title = "Enable notifications",
                description = "Receive updates about new chapters",
                icon = Icons.Outlined.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }
        
        // Navigation item
        item {
            SettingsItem(
                title = "Language",
                description = "English",
                icon = Icons.Outlined.Language,
                onClick = { showLanguageDialog = true }
            )
        }
        
        // Divider
        item {
            SettingsDivider()
        }
        
        // Another section
        item {
            SettingsSectionHeader(
                title = "Advanced",
                icon = Icons.Filled.TuneIcon
            )
        }
        
        // More items...
    }
}
```

### Dialog Usage

```kotlin
@Composable
fun CategoryManagementScreen() {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    
    // Screen content...
    
    if (showDeleteDialog) {
        SettingsConfirmationDialog(
            title = "Delete Category?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            onConfirm = {
                deleteCategory()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    if (showRenameDialog) {
        SettingsTextInputDialog(
            title = "Rename Category",
            initialValue = currentCategoryName,
            label = "Category Name",
            validator = { name ->
                when {
                    name.isEmpty() -> "Name cannot be empty"
                    name.length < 2 -> "Name too short"
                    else -> null
                }
            },
            onConfirm = { newName ->
                renameCategory(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}
```

---

## Migration Guide

### From Old Components to New Components

#### PreferenceRow → SettingsItem
```kotlin
// Old
PreferenceRow(
    title = "Settings",
    subtitle = "Configure app",
    icon = Icons.Default.Settings,
    onClick = { }
)

// New
SettingsItem(
    title = "Settings",
    description = "Configure app",
    icon = Icons.Default.Settings,
    onClick = { }
)
```

#### SwitchPreference → SettingsSwitchItem
```kotlin
// Old
SwitchPreference(
    preference = showEmptyCategories,
    title = "Show Empty",
    subtitle = "Display empty categories"
)

// New
SettingsSwitchItem(
    title = "Show Empty",
    description = "Display empty categories",
    checked = showEmptyCategories.value,
    onCheckedChange = { showEmptyCategories.value = it }
)
```

#### Custom Dialogs → Unified Dialog Components
```kotlin
// Old - Custom implementation
IAlertDialog(
    title = { Text("Delete?") },
    text = { Text("Are you sure?") },
    confirmButton = { Button(...) },
    dismissButton = { TextButton(...) }
)

// New - Unified component
SettingsConfirmationDialog(
    title = "Delete?",
    message = "Are you sure?",
    isDestructive = true,
    onConfirm = { },
    onDismiss = { }
)
```

---

## Best Practices

1. **Use consistent spacing**: Always use the provided components to maintain consistent spacing across screens.

2. **Group related settings**: Use `SettingsSectionHeader` to group related settings together.

3. **Provide descriptions**: Always include descriptions for settings items to help users understand what each setting does.

4. **Use appropriate icons**: Choose icons that clearly represent the setting or action.

5. **Validate input**: Always provide validation for text input dialogs to prevent invalid data.

6. **Use destructive styling**: Set `isDestructive = true` for confirmation dialogs that perform irreversible actions.

7. **Accessibility**: The components handle accessibility automatically, but ensure your custom content also follows accessibility guidelines.

8. **Consistent terminology**: Use consistent button text across dialogs (e.g., always use "Delete" for delete actions, not "Remove" or "Erase").

---

## Testing

All components include:
- Proper semantic roles for accessibility testing
- Content descriptions for screen reader testing
- Keyboard navigation support
- Touch target size compliance (minimum 48dp)

Test your settings screens with:
- TalkBack/VoiceOver enabled
- Keyboard navigation
- Different text sizes
- Dark/Light themes
- Different screen sizes

---

## Future Enhancements

Potential additions to the library:
- `SettingsSliderItem` - For numeric value selection
- `SettingsColorPickerItem` - For color selection
- `SettingsDatePickerItem` - For date selection
- `SettingsDropdownItem` - For dropdown selection
- `SettingsExpandableItem` - For collapsible sections
- `SettingsInfoCard` - For displaying information without interaction

---

## Support

For questions or issues with the unified settings components, please:
1. Check this documentation first
2. Review existing settings screens for examples
3. Consult the Material3 design guidelines
4. Open an issue with the development team
