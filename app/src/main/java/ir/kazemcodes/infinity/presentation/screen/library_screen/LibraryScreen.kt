package ir.kazemcodes.infinity.presentation.screen.library_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LibraryScreen() {

    val coroutineScope = rememberCoroutineScope()


    Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Library")
 
        }
//        LaunchedEffect(true) {
//            coroutineScope.launch(Dispatchers.IO) {
//                val doc = Jsoup.connect("https://readwebnovels.net/").get()
//
//                val image = doc.select("div.item-summary > div.post-title.font-title > h3 > a").eachText()
//
//                    Log.d(TAG, "LibraryScreen: ${image}")
//
//
//
//                //Log.d(TAG, "LibraryScreen: $title")
//            }
//        }
    







}

