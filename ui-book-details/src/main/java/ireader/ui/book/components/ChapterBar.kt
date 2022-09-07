package ireader.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.ui.book.viewmodel.BookDetailViewModel
import ireader.common.models.entities.Chapter
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.book.R

@Composable
fun ChapterBar(
    vm: BookDetailViewModel,
    chapters:List<Chapter>,
    onMap: () -> Unit,
    onSortClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier,
            text = "${chapters.size.toString()} Chapters",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            AppIconButton(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
                onClick = {
                    vm.searchMode = !vm.searchMode
                },
            )
            AppIconButton(
                imageVector = Icons.Filled.Place,
                contentDescription = stringResource(R.string.find_current_chapter),
                onClick = onMap
            )
            AppIconButton(
                imageVector = Icons.Default.Sort,
                onClick = onSortClick
            )
        }

    }
}