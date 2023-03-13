package ireader.presentation.ui.book

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChapterDetailComposable(
    modifier: Modifier = Modifier,
    name: String,
    chapterNumber: String,
    dateUploaded: String,
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(8.dp)
    ) {
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {

            Text(
                text = if (name.contains(chapterNumber)) name else chapterNumber,
                style = MaterialTheme.typography.labelMedium
            )
            Text(text = dateUploaded, style = MaterialTheme.typography.labelSmall)
        }
    }
}