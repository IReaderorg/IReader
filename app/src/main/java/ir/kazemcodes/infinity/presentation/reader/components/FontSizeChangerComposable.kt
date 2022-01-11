package ir.kazemcodes.infinity.presentation.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kazemcodes.infinity.presentation.reader.FontSizeEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderEvent
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton

@Composable
fun FontSizeChangerComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel
) {
    Column(verticalArrangement = Arrangement.Center) {
        Text(
            text = "Font Size",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colors.onBackground
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopAppBarActionButton(modifier = modifier.size(40.dp), imageVector = Icons.Default.TextFormat, title = "Decrease font size", onClick = {  viewModel.onEvent(
                ReaderEvent.ChangeFontSize(FontSizeEvent.Decrease)) })
            TopAppBarActionButton(modifier = modifier.size(60.dp),imageVector = Icons.Default.TextFormat, title = "Increase font size", onClick = { viewModel.onEvent(ReaderEvent.ChangeFontSize(FontSizeEvent.Increase)) })
        }
    }

}
@Composable
fun FontHeightChangerComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {
    Column(verticalArrangement = Arrangement.Center) {
        Text(
            text = "Line Height",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colors.onBackground
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopAppBarActionButton(modifier = modifier.size(20.dp), imageVector = Icons.Default.CloseFullscreen, title = "Decrease font height", onClick = { viewModel.saveFontHeight(false) })
            TopAppBarActionButton(modifier = modifier.size(20.dp),imageVector = Icons.Default.OpenInFull, title = "Increase font height", onClick = { viewModel.saveFontHeight(true) })
        }
    }

}