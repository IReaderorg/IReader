package ir.kazemcodes.infinity.setting_feature.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(16.dp)) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Setting", style = MaterialTheme.typography.h6)
        }
    }

}