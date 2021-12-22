package ir.kazemcodes.infinity.presentation.components

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TitleText(text: String  , color: Colors? =null , style: FontStyle? = null , fontWeight: FontWeight? =null) {
    Text(
        text = text,
        color = MaterialTheme.colors.onBackground ,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold
    )
}