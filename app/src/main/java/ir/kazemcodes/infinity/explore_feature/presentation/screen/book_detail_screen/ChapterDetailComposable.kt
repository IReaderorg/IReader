package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ChapterDetailComposable(modifier : Modifier = Modifier, name : String ,chapterNumber : String ,  dateUploaded : String ) {

    Box(modifier = modifier
        .fillMaxWidth()
        .height(40.dp).padding(8.dp)) {
        Row(modifier = modifier.fillMaxSize() , horizontalArrangement = Arrangement.SpaceBetween) {

            Text(text = if (name.contains(chapterNumber)) name else chapterNumber , style = MaterialTheme.typography.body2)
            Text(text = dateUploaded, style = MaterialTheme.typography.body2)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterDetailPrev() {
    ChapterDetailComposable(name = "1-CHAPTER One - Cat And Dog" , chapterNumber = "1-" , dateUploaded = "3 min ago")
}