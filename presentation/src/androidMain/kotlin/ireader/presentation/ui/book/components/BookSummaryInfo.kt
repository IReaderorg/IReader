package ireader.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book

@Composable
internal fun BookSummaryInfo(
    modifier: Modifier = Modifier,
    onSummaryExpand: () -> Unit,
    book: Book,
    isSummaryExpanded: Boolean
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()

    ) {
        BookSummary(
            onClickToggle = { onSummaryExpand() },
            description = book.description,
            genres = book.genres,
            expandedSummary = isSummaryExpanded,
        )
    }
}
