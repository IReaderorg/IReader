package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailViewModel

@Composable
fun BookDetailToggleButtonsComposable(bookDetail : Book , navController : NavController , viewmodel : BookDetailViewModel) {

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier.fillMaxWidth()
    ) {
        var isInitialized by remember { mutableStateOf(bookDetail.initialized) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                isInitialized = !isInitialized
                if (!isInitialized) {
                    viewmodel.insertBookDetail(bookDetail.toBookEntity())
                } else {
                    viewmodel.deleteBook(bookDetail.toBookEntity())
                }
            }) {
            if (!isInitialized) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Library Icon",
                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(50.dp)
                )
                Text(
                    text = "Add to Library",
                    color = MaterialTheme.colors.onBackground
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Added to Library Icon",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .size(50.dp)
                )
                Text(
                    text = "Already in Your Library",
                    color = MaterialTheme.colors.onBackground
                )
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { }) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "WebView",
                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        navController.navigate(Routes.WebViewScreen.plus("?url=${bookDetail.link}"))
                    }
            )
            Text(text = "WebView", color = MaterialTheme.colors.onBackground)
        }
    }
}