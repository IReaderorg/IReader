package ireader.presentation.ui.home.tts

import android.speech.tts.Voice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.component.reusable_composable.CaptionTextComposable
import ireader.presentation.ui.core.ui.string
import ireader.domain.services.tts_service.TTSState
import ireader.domain.services.tts_service.isSame
import ireader.presentation.R
import java.util.Locale

@Composable
fun VoiceChip(
    modifier: Modifier = Modifier,
    viewModel: TTSState,
    onVoice: (Voice) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .width(100.dp),
            text = string(id = R.string.voices),
            textAlign = TextAlign.Start,
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )

        LazyRow(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            items(count = viewModel.voices.size) { index ->
                viewModel.voices.filter { !it.isNetworkConnectionRequired }.let { voices ->
                    Spacer(modifier = modifier.width(10.dp))
                    Box(
                        modifier = modifier
                            .height(20.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                2.dp,
                                if (voices[index].isSame(viewModel.currentVoice)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                    .4f
                                ),
                                CircleShape
                            )
                            .clickable {
                                onVoice(voices[index])
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        /**
                         * I used the display name because the google voices name is not really good for the UI
                         * need to think of something else for it later
                         */
                        CaptionTextComposable(
                            text = voices[index].locale.displayName,
                            maxLine = 1,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageChip(
    modifier: Modifier = Modifier,
    viewModel: TTSState,
    onLanguage: (Locale) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .width(100.dp),
            text = string(id = R.string.languages),
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )

        LazyRow {
            items(count = viewModel.languages.size) { index ->
                viewModel.languages.sortedBy { it.displayName }.let { language ->
                    Spacer(modifier = modifier.width(10.dp))
                    Box(
                        modifier = modifier
                            .height(50.dp)
                            .clip(RectangleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                2.dp,
                                if (language[index].displayName == viewModel.currentLanguage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                    .4f
                                ),
                                CircleShape
                            )
                            .clickable {
                                onLanguage(language[index])
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CaptionTextComposable(
                            text = language[index].displayName,
                            maxLine = 1,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
