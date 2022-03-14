package org.ireader.presentation.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import org.ireader.domain.feature_services.io.BookCover
import org.ireader.presentation.R

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookImageComposable(
    image: BookCover,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.FillHeight,
    iconBadge: (@Composable () -> Unit)? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_no_image_placeholder,
) {

    val painter = rememberImagePainter(data = image) {
        crossfade(durationMillis = 700)
        placeholder(placeholder)
        error(placeholder)
    }

    Image(
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
        painter = painter,
        contentDescription = "an image",
        alignment = alignment,
    )
}

//@Composable
//fun BookItem(
//    title: String,
//    cover: BookCover,
//    onClick: () -> Unit = {},
//) {
//    val fontStyle = LocalTextStyle.current.merge(
//        TextStyle(letterSpacing = 0.sp, fontFamily = FontFamily.Default, fontSize = 14.sp)
//    )
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(3f / 4f)
//            .padding(4.dp)
//            .clickable(onClick = onClick),
//        elevation = 4.dp,
//        shape = RoundedCornerShape(4.dp)
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            Image(
//                painter = rememberImagePainter(cover),
//                contentDescription = null,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop
//            )
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .then(shadowGradient)
//            )
//            Text(
//                text = title,
//                color = Color.White,
//                style = fontStyle,
//                modifier = Modifier
//                    .wrapContentHeight(Alignment.CenterVertically)
//                    .align(Alignment.BottomStart)
//                    .padding(8.dp)
//            )
//        }
//    }
//}

//private val shadowGradient = Modifier.drawWithCache {
//    val gradient = Brush.linearGradient(
//        0.75f to Color.Transparent,
//        1.0f to Color(0xAA000000),
//        start = Offset(0f, 0f),
//        end = Offset(0f, size.height)
//    )
//    onDrawBehind {
//        drawRect(gradient)
//    }
//}
