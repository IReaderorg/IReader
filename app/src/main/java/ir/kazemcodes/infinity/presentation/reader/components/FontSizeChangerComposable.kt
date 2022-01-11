package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.presentation.reader.FontSizeEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton

@Composable
fun FontSizeChangerComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel
) {
        Row( verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = modifier.width(5.dp))
            Text(
                text = "Font Size",
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.W400),
                color = MaterialTheme.colors.onBackground
            )
            IconButton(onClick = { viewModel.onEvent(
                ReaderEvent.ChangeFontSize(FontSizeEvent.Decrease))}) {
                Icon(painter = painterResource(id = R.drawable.ic_decrease_text_size), contentDescription = "Decrease font size")
            }
            IconButton(onClick = { viewModel.onEvent(ReaderEvent.ChangeFontSize(FontSizeEvent.Increase)) }) {
                Icon(painter = painterResource(id = R.drawable.ic_increase_text_size), contentDescription = "Increase font size")
            }
            //TopAppBarActionButton(modifier = modifier.size(40.dp), imageVector = Icons.Default.TextFormat, title = "Decrease font size", onClick = {   })
            //TopAppBarActionButton(modifier = modifier.size(60.dp),imageVector = Icons.Default.TextFormat, title = "Increase font size", onClick = {  })
        }


}
@Composable
fun FontHeightChangerComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {
        Row(verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = modifier.width(5.dp))
            Text(
                text = "Line Height",
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.W400),
                color = MaterialTheme.colors.onBackground
            )
            TopAppBarActionButton( imageVector = Icons.Default.CloseFullscreen, title = "Decrease font height", onClick = { viewModel.saveFontHeight(false) })
            TopAppBarActionButton(imageVector = Icons.Default.OpenInFull, title = "Increase font height", onClick = { viewModel.saveFontHeight(true) })
        }


}

@Composable
fun ParagraphDistanceComposable(modifier: Modifier=Modifier,viewModel: ReaderScreenViewModel,) {
    Row(verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = modifier.width(5.dp))
        Text(
            text = "Paragraph Distance",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colors.onBackground
        )
        TopAppBarActionButton( imageVector = Icons.Default.VerticalAlignTop, tint = MaterialTheme.colors.onBackground, title = "Decrease font height", onClick = { viewModel.saveParagraphDistance(false) })
        TopAppBarActionButton(imageVector = Icons.Default.VerticalAlignBottom, tint = MaterialTheme.colors.onBackground,title = "Increase font height", onClick = { viewModel.saveParagraphDistance(true) })
    }
}