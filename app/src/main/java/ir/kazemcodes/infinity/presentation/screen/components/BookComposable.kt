package ir.kazemcodes.infinity.presentation.screen.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage
import ir.kazemcodes.infinity.data.remote.source.model.Book


@Composable
fun LinearBookItem(title : String , img_thumbnail : String) {
    // TODO need to change this part

    Box(modifier = Modifier.padding(vertical = 8.dp , horizontal = 4.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) ,
            verticalAlignment = Alignment.CenterVertically) {

            GlideImage(
                img_thumbnail,
                contentDescription = " $title Book Cover",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .border(
                        shape = CircleShape,
                        width = 1.dp,
                        color = Color.Transparent,
                    )
                    .size(size = 50.dp)
            )
//            Image(
//                painter = painterResource(R.drawable.book_cover),
//                contentDescription = " $title Book Cover",
//                contentScale = ContentScale.Fit,
//                modifier = Modifier
//                    .border(
//                        shape = CircleShape,
//                        width = 1.dp,
//                        color = Color.Transparent,
//                    )
//                    .size(size = 50.dp)
//            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(text = title,  style = MaterialTheme.typography.body2 , color = MaterialTheme.colors.onBackground)
        }
    }

}


@Preview(showBackground = true)
@Composable
fun BookComposablePreview() {
    LinearViewList(books = Test.booksTest )
}

@Composable
fun LinearViewList(books: List<Book>) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(count = books.size) { index ->
            LinearBookItem(title = books[index].title , img_thumbnail = books[index].thumbnailUrl ?: "")
        }

    }


}

object Test {
    val booksTest = listOf(
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        },
        Book.create().apply {
            title = "Shock! Spell is in English"
            thumbnailUrl = "https://wuxiaworld.site/novel/one-man-army/"
        }
    )
}