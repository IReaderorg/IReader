package ir.kazemcodes.infinity.presentation.reusable_composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotImplementedText() {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {


        Text(
            text = "＼( °□° )／",
            style = MaterialTheme.typography.h3
            //fontSize = 200.dp
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = "Not Implemented yet.",
            style = MaterialTheme.typography.subtitle1
            //fontSize = 200.dp
        )


    }
}