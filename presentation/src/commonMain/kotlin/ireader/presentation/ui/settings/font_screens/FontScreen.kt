package ireader.presentation.ui.settings.font_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.ui.component.reusable_composable.AppIcon
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
fun FontScreen(
    vm: FontScreenViewModel,
    onFont: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Info card - shown when preview is off
        if (!vm.previewMode.value) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.tap_the_eye_icon_to_preview_fonts),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Preview toggle and search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search field
            OutlinedTextField(
                value = vm.searchQuery,
                onValueChange = { vm.searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(localizeHelper.localize(Res.string.search_fonts)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = localizeHelper.localize(Res.string.search))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Preview toggle button
            IconButton(
                onClick = { vm.previewMode.value = !vm.previewMode.value }
            ) {
                Icon(
                    imageVector = if (vm.previewMode.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (vm.previewMode.value) "Hide preview" else "Show preview",
                    tint = if (vm.previewMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Preview text - Shows currently selected font
        if (vm.previewMode.value && vm.font?.value != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Text(
                        text = "Currently Selected: ${vm.font?.value?.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Sample paragraph
                    Text(
                        text = localizeHelper.localize(Res.string.the_quick_brown_fox_jumps),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Different sizes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.small),
                            fontSize = 12.sp,
                            fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.medium),
                            fontSize = 16.sp,
                            fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.large),
                            fontSize = 20.sp,
                            fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Numbers and special characters
                    Text(
                        text = "0123456789 !@#$%^&*()_+-=[]{}|;:',.<>?/",
                        fontSize = 14.sp,
                        fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Loading indicator
        if (vm.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Font list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(vm.uiFonts) { fontName ->
                FontItem(
                    fontName = fontName,
                    isSelected = fontName == vm.font?.value?.name,
                    showPreview = vm.previewMode.value,
                    onClick = {
                        onFont(fontName)
                    }
                )
            }
        }
    }
}

@Composable
private fun FontItem(
    fontName: String,
    isSelected: Boolean,
    showPreview: Boolean,
    onClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Create font family for this specific font with error handling
    val fontFamily = remember(fontName) {
        try {
            val customFont = ireader.domain.models.common.FontFamilyModel.Custom(fontName)
            customFont.toComposeFontFamily()
        } catch (e: Exception) {
            ireader.core.log.Log.error("Failed to load font in preview: $fontName", e)
            androidx.compose.ui.text.font.FontFamily.Default
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Font name in default font
                Text(
                    text = fontName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (showPreview) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Preview text in the actual font
                    Text(
                        text = localizeHelper.localize(Res.string.the_quick_brown_fox_jumps_over_the_lazy_dog),
                        fontFamily = fontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    // Numbers and special characters preview
                    Text(
                        text = "0123456789 !@#$%",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                }
            }
            
            if (isSelected) {
                AppIcon(
                    imageVector = Icons.Default.Check,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = localizeHelper.localize(Res.string.selected)
                )
            }
        }
    }
}
