package ireader.presentation.ui.component.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.ui.IReaderLoadingScreen
import kotlin.random.Random

@Composable
fun LoadingScreen() {
    IReaderLoadingScreen()
}


@Composable
fun LoadingScreen(
        isLoading: Boolean = true,
        modifier: Modifier = Modifier.fillMaxSize(),
        /*@FloatRange(from = 0.0, to = 1.0)*/
        progress: Float = 0.0F,
        errorMessage: String? = null,
        retryMessage: String = localize(Res.string.retry),
        retry: (() -> Unit)? = null
) {
    Crossfade(isLoading, modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (it) {
                if (progress != 0.0F && !progress.isNaN()) {
                    val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )
                    CircularProgressIndicator(animatedProgress, Modifier.align(Alignment.Center))
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                ErrorScreen(errorMessage, modifier, retryMessage, retry)
            }
        }
    }
}

object ProgressIndicatorDefaults {
    /**
     * Default stroke width for [CircularProgressIndicator], and default height for
     * [LinearProgressIndicator].
     *
     * This can be customized with the `strokeWidth` parameter on [CircularProgressIndicator],
     * and by passing a layout modifier setting the height for [LinearProgressIndicator].
     */
    val StrokeWidth = 4.dp

    /**
     * The default opacity applied to the indicator color to create the background color in a
     * [LinearProgressIndicator].
     */
    const val IndicatorBackgroundOpacity = 0.24f

    /**
     * The default [AnimationSpec] that should be used when animating between progress in a
     * determinate progress indicator.
     */
    val ProgressAnimationSpec = SpringSpec(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow,
            // The default threshold is 0.01, or 1% of the overall progress range, which is quite
            // large and noticeable.
            visibilityThreshold = 1 / 1000f
    )
}
@Composable
fun ErrorScreen(
        errorMessage: String? = null,
        modifier: Modifier = Modifier,
        retryMessage: String = localize(Res.string.retry),
        retry: (() -> Unit)? = null
) {
    Box(modifier then Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            val errorFace = remember { getRandomErrorFace() }
            Text(
                    text = errorFace,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
            )
            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.onBackground)
            }
            if (retry != null) {
                TextButton(retry) {
                    Text(retryMessage)
                }
            }
        }
    }
}

private val ERROR_FACES = arrayOf(
        "(･o･;)",
        "Σ(ಠ_ಠ)",
        "ಥ_ಥ",
        "(˘･_･˘)",
        "(；￣Д￣)",
        "(･Д･。"
)

fun getRandomErrorFace(): String {
    return ERROR_FACES[Random.nextInt(ERROR_FACES.size)]
}
