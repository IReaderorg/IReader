package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

@Composable
expect fun OnShowImportEpub(show:Boolean, onFileSelected: suspend (Uri) -> Unit)