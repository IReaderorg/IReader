package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme

@Composable
fun ReadingScreen(
    url: String,
    name: String,
    chapterNumber: String,
    viewModel: ReadingScreenViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    Box(
        Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = state.readingContent)
        if (state.error.isNotBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
        }
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ReadingDark() {
    InfinityTheme {
        ReadingScreen("", "", "")
    }
}

@Preview(showBackground = true)
@Composable
fun ReadingLight() {
    InfinityTheme {

        ReadingScreen("", "", "")
    }
}

