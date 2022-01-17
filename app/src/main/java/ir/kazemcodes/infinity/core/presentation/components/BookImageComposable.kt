package ir.kazemcodes.infinity.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackextensions.servicesktx.lookup
import okhttp3.OkHttpClient

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    val backstack = LocalBackstack.current
    val okHttpClient = remember {
        backstack.lookup<OkHttpClient>()
    }
    val painter = rememberImagePainter(data = image) {
        crossfade(durationMillis = 1000)
    }
    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = "image",
        alignment = alignment,
        contentScale = contentScale,
    )

//    CoilImage(
//        image,
//        contentDescription = "Book Cover",
//        modifier = modifier,
//        contentScale = contentScale,
//        loading = {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator(modifier = Modifier.size(25.dp))
//            }
//        },
//        failure = {
//            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
//                Column(
//                    modifier = modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Image,
//
//                        contentDescription = "image Icon",
//                        Modifier.size(40.dp),
//                        tint = MaterialTheme.colors.onBackground
//                    )
//                    TopAppBarTitle(title = "No Image Available")
//                }
//            }
//        }, success = { loaded ->
//            Box(modifier.fillMaxSize()) {
//                Image(
//                    modifier = modifier.fillMaxSize(),
//                    painter = rememberDrawablePainter(drawable = loaded.drawable),
//                    contentDescription = "book cover",
//                    contentScale = contentScale,
//                    alignment = Alignment.TopCenter,
//                )
//            }
//        },
//        imageLoader = {
//            ImageLoader.Builder(context)
//                .okHttpClient(okHttpClient)
//                .crossfade(true)
//                .build()
//        }
//
//    )
}