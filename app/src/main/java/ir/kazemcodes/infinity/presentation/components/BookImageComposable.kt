package ir.kazemcodes.infinity.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil.CoilImage
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle

@Composable
fun BookImageComposable(
    image: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
) {
    CoilImage(
        image,
        contentDescription = "Book Cover",
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = modifier.size(4.dp))
            }
        },
        failure = {
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,

                        contentDescription = "image Icon",
                        Modifier.size(40.dp),
                        tint = MaterialTheme.colors.onBackground
                    )
                    TopAppBarTitle(title = "No Image Available")
                }
            }
        }

    )
}