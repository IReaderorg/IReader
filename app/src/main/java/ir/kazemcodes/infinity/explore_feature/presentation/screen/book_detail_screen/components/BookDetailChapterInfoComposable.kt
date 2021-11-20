package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.domain.util.encodeUrl
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.ChapterState
import java.lang.reflect.Type

@Composable
fun BookDetailChapterInfoComposable(modifier : Modifier = Modifier, chapterState : ChapterState ,navController : NavController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(40.dp)
    ) {

        if (chapterState.chapters.isNotEmpty()) {
            Text(
                text = "${chapterState.chapters.size} Chapters",
                color = MaterialTheme.colors.onBackground
            )
        } else {
            Text(text = "0 Chapters", color = MaterialTheme.colors.onBackground)
        }
        Text(text = "Details" , color = MaterialTheme.colors.primary , modifier = modifier.clickable {
            val listType: Type =
                object : TypeToken<List<Chapter>>() {}.type
            val json = Gson().toJson(chapterState.chapters , listType)
            navController.navigate(Routes.ChapterDetailScreen.plus("/${encodeUrl(json)}"))
        })

    }
}