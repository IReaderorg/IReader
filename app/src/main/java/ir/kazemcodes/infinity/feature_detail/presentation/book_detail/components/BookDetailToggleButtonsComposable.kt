//package ir.kazemcodes.infinity.feature_detail.presentation.book_detail.components
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.Icon
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Language
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
//import ir.kazemcodes.infinity.core.domain.models.Book
//import ir.kazemcodes.infinity.core.domain.models.Chapter
//import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.BookDetailViewModel
//import ir.kazemcodes.infinity.feature_activity.presentation.WebViewKey
//
//@Composable
//fun BookDetailToggleButtonsComposable(
//    book: Book,
//    chapters: List<Chapter>,
//    viewModel: BookDetailViewModel,
//) {
//    val backStack = LocalBackstack.current
//
//    Row(
//        horizontalArrangement = Arrangement.SpaceAround,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        var inLibrary by remember { mutableStateOf(book.inLibrary) }
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.clickable {
//                if (!inLibrary) {
//                    viewModel.insertBookDetailToLocal()
//                    val chapterEntities =
//                        chapters.map { it.copy(bookName = book.bookName).toChapterEntity() }
//                    viewModel.insertChaptersToLocal()
//                    inLibrary = true
//                } else {
//                    viewModel.deleteLocalBook(book.bookName)
//                    viewModel.insertBookDetailToLocal()
//                    inLibrary = false
//                }
//            }) {
//            if (!inLibrary) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Add to Library Icon",
//                    tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
//                    modifier = Modifier
//                        .size(50.dp)
//                )
//                Text(
//                    text = "Add to Library",
//                    color = MaterialTheme.colors.onBackground
//                )
//            } else {
//                Icon(
//                    imageVector = Icons.Default.Check,
//                    contentDescription = "Added to Library Icon",
//                    tint = MaterialTheme.colors.primary,
//                    modifier = Modifier
//                        .size(50.dp)
//                )
//                Text(
//                    text = "Already in Your Library",
//                    color = MaterialTheme.colors.onBackground
//                )
//            }
//
//        }
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally) {
//            Icon(
//                imageVector = Icons.Default.Language,
//                contentDescription = "WebView",
//                tint = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
//                modifier = Modifier
//                    .size(50.dp)
//                    .clickable {
//                        backStack.goTo(WebViewKey(book.link))
//                    }
//            )
//            Text(text = "WebView", color = MaterialTheme.colors.onBackground)
//        }
//    }
//}