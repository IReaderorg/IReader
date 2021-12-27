package ir.kazemcodes.infinity.setting_feature.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.kazemcodes.infinity.presentation.book_detail.Constants

@Composable
fun SettingScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Setting",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
            )
        }) {

        }
    }

}