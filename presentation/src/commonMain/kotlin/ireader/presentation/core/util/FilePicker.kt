package ireader.presentation.core.util

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri


@Composable
expect fun FilePicker(
	show: Boolean,
	initialDirectory: String? = null,
	fileExtensions: List<String> = emptyList(),
	onFileSelected: (Uri?) -> Unit
)

@Composable
expect fun DirectoryPicker(
    show: Boolean,
    initialDirectory: String? = null,
    onFileSelected: (String?) -> Unit
)