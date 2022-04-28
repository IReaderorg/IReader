package org.ireader.tts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import org.ireader.core_ui.ui_components.reusable_composable.CaptionTextComposable
import org.ireader.reader.viewmodel.ReaderScreenViewModel


@Composable
fun VoiceChip(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .width(100.dp),
            text = "Voices",
            textAlign = TextAlign.Start,
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )

        LazyRow(modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
            items(count = viewModel.voices.size) { index ->
                viewModel.voices.filter { !it.isNetworkConnectionRequired }.let { voices ->
                    Spacer(modifier = modifier.width(10.dp))
                    Box(modifier = modifier
                        .height(20.dp)
                        .clip(RectangleShape)
                        .background(MaterialTheme.colors.background)
                        .border(2.dp,
                            if (voices[index].locale.displayName == viewModel.currentVoice) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(
                                .4f),
                            CircleShape)
                        .clickable {
                            viewModel.currentVoice = voices[index].locale.displayName
                            viewModel.speechPrefUseCases.saveVoice(voices[index].locale.displayName)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        CaptionTextComposable(text = voices[index].locale.displayName,
                            maxLine = 1,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                    }
                }
            }

        }

    }


}

@Composable
fun LanguageChip(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {
    val context = LocalContext.current
    Row(modifier = Modifier
        .height(50.dp)
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier
                .width(100.dp),
            text = "Languages",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )

        LazyRow {
            items(count = viewModel.languages.size) { index ->
                viewModel.languages.sortedBy { it.displayName }.let { language ->
                    Spacer(modifier = modifier.width(10.dp))
                    Box(modifier = modifier
                        .height(50.dp)
                        .clip(RectangleShape)
                        .background(MaterialTheme.colors.background)
                        .border(2.dp,
                            if (language[index].displayName == viewModel.currentLanguage) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(
                                .4f),
                            CircleShape)
                        .clickable {
                            viewModel.currentLanguage = language[index].displayName
                            viewModel.speechPrefUseCases.saveLanguage(language[index].displayName)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        CaptionTextComposable(text = language[index].displayName,
                            maxLine = 1,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                    }
                }
            }

        }

    }


}
