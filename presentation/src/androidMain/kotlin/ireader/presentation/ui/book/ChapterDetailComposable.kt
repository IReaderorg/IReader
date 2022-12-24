package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

@Preview(showBackground = true)
@Composable
fun ChapterDetailPrev() {
    ChapterDetailComposable(
        name = "1-CHAPTER One - Cat And Dog",
        chapterNumber = "1-",
        dateUploaded = "3 min ago"
    )
}
