package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.Composable

@Composable
expect fun OnShowThemeExport(show: Boolean, themeJson: String, onFileSelected: suspend (Boolean) -> Unit)
