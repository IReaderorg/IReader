package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

@Composable
expect fun OnShowRestore(show:Boolean, onFileSelected: suspend (Uri) -> Unit)

@Composable
expect fun OnShowBackup(show:Boolean, onFileSelected: suspend (Uri) -> Unit)