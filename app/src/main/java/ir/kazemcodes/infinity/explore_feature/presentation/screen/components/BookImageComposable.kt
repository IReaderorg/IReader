package ir.kazemcodes.infinity.presentation.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun BookImageComposable(image : Any, modifier: Modifier = Modifier) {
        CoilImage(
            image,
            contentDescription = "Book Cover",
            modifier = modifier
                .fillMaxSize(),
            contentScale = ContentScale.Fit,
            loading = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = modifier.size(4.dp))
                }
            },
            failure = {
                Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Occurred",
                        Modifier.size(16.dp)
                    )
                }
            },

            )

}