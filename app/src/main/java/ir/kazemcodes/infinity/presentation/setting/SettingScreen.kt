package ir.kazemcodes.infinity.setting_feature.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.presentation.book_detail.Constants

@Composable
fun SettingScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()

                ,topBar = {
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
        } ) {
            Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center) {


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
    }

}