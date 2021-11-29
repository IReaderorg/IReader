package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FontSizeChangerComposable(
    modifier: Modifier = Modifier,
    onFontDecease: () -> Unit,
    ontFontIncrease: () -> Unit,
    fontSize: Int
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(end = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Font Size",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            modifier =modifier.fillMaxWidth(.2f)
        )
        Row(
            modifier = modifier.fillMaxWidth(.8f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease Font Size",
                modifier = modifier
                    .clickable {
                        onFontDecease()
                    }
                    .size(30.dp),
                tint = MaterialTheme.colors.onBackground

            )
            Text(text = "$fontSize", fontSize = 20.sp)
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase Font Size",
                modifier = modifier
                    .clickable {
                        ontFontIncrease()
                    }
                    .size(30.dp), tint = MaterialTheme.colors.onBackground
            )
        }
    }
}