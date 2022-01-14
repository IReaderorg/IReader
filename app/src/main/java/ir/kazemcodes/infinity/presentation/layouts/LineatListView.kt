package ir.kazemcodes.infinity.presentation.layouts

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.presentation.components.BookImageComposable


@Composable
fun LinearBookItem(
    modifier: Modifier = Modifier,
    title: String,
    img_thumbnail: Any,

    ) {

    Box(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)) {
            BookImageComposable(image = img_thumbnail, modifier = modifier
                .height(40.dp)
                .width(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(.2.dp, MaterialTheme.colors.onBackground.copy(alpha = .1f)))
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
fun LinearListDisplay(
    books: List<Book>,
    onClick: (index: Int) -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        items(count = books.size) { index ->
            LinearBookItem(
                title = books[index].bookName,
                img_thumbnail = books[index].coverLink ?: "",
                modifier = Modifier.clickable {
                    onClick(index)
                }
            )
        }

    }
}

