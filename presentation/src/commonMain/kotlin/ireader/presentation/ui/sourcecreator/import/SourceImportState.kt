package ireader.presentation.ui.sourcecreator.import

import androidx.compose.runtime.Stable
import ireader.domain.usersource.model.UserSource

/**
 * State for the source import screen.
 */
@Stable
data class SourceImportState(
    val jsonInput: String = "",
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null,
    val importedSources: List<UserSource> = emptyList(),
    val selectedSources: Set<Long> = emptySet()
)
