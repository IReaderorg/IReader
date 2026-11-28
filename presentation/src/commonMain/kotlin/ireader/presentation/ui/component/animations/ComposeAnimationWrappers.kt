@file:Suppress("unused")

package ireader.presentation.ui.component.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat as composeAnimateFloat
import androidx.compose.animation.core.animateDpAsState as composeAnimateDpAsState
import androidx.compose.animation.core.animateFloatAsState as composeAnimateFloatAsState
import androidx.compose.animation.core.animateIntAsState as composeAnimateIntAsState
import androidx.compose.animation.core.animateIntOffsetAsState as composeAnimateIntOffsetAsState
import androidx.compose.animation.core.animateIntSizeAsState as composeAnimateIntSizeAsState
import androidx.compose.animation.core.animateOffsetAsState as composeAnimateOffsetAsState
import androidx.compose.animation.core.animateRectAsState as composeAnimateRectAsState
import androidx.compose.animation.core.animateSizeAsState as composeAnimateSizeAsState
import androidx.compose.animation.core.infiniteRepeatable as composeInfiniteRepeatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring as composeSpring
import androidx.compose.animation.core.tween as composeTween
import androidx.compose.animation.expandHorizontally as composeExpandHorizontally
import androidx.compose.animation.expandIn as composeExpandIn
import androidx.compose.animation.expandVertically as composeExpandVertically
import androidx.compose.animation.fadeIn as composeFadeIn
import androidx.compose.animation.fadeOut as composeFadeOut
import androidx.compose.animation.scaleIn as composeScaleIn
import androidx.compose.animation.scaleOut as composeScaleOut
import androidx.compose.animation.shrinkHorizontally as composeShrinkHorizontally
import androidx.compose.animation.shrinkOut as composeShrinkOut
import androidx.compose.animation.shrinkVertically as composeShrinkVertically
import androidx.compose.animation.slideIn as composeSlideIn
import androidx.compose.animation.slideInHorizontally as composeSlideInHorizontally
import androidx.compose.animation.slideInVertically as composeSlideInVertically
import androidx.compose.animation.slideOut as composeSlideOut
import androidx.compose.animation.slideOutHorizontally as composeSlideOutHorizontally
import androidx.compose.animation.slideOutVertically as composeSlideOutVertically
import androidx.compose.animation.AnimatedContent as composeAnimatedContent
import androidx.compose.animation.AnimatedVisibility as composeAnimatedVisibility
import androidx.compose.animation.Crossfade as composeCrossfade
import androidx.compose.animation.animateColorAsState as composeAnimateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ireader.presentation.ui.component.AnimationPolicy

/*
 * Drop-in replacements for Compose animation entry points.
 *
 * Import these instead of the androidx.compose animation functions. When max
 * performance mode is on, specs become `snap()` and enter/exit transitions
 * become `None`, so screens pop into place instead of sliding or fading.
 */

public fun <T> tween(
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing,
): FiniteAnimationSpec<T> = if (AnimationPolicy.animationsEnabled) {
    composeTween(durationMillis = durationMillis, delayMillis = delayMillis, easing = easing)
} else {
    snap()
}

public fun <T> spring(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMedium,
    visibilityThreshold: T? = null,
): FiniteAnimationSpec<T> = if (AnimationPolicy.animationsEnabled) {
    composeSpring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
        visibilityThreshold = visibilityThreshold,
    )
} else {
    snap()
}

public fun <T> infiniteRepeatable(
    animation: FiniteAnimationSpec<T>,
    repeatMode: RepeatMode = RepeatMode.Restart,
    initialStartOffset: StartOffset = StartOffset(0),
): InfiniteRepeatableSpec<T> = if (AnimationPolicy.animationsEnabled) {
    composeInfiniteRepeatable(
        animation = animation as DurationBasedAnimationSpec<T>,
        repeatMode = repeatMode,
        initialStartOffset = initialStartOffset,
    )
} else {
    // InfiniteRepeatableSpec rejects a zero-duration inner spec. The animateFloat
    // wrapper bypasses the animation entirely in this mode; 1ms is only a safe
    // fallback for any other infinite consumer.
    InfiniteRepeatableSpec(
        animation = composeTween(durationMillis = 1),
        repeatMode = repeatMode,
        initialStartOffset = initialStartOffset,
    )
}

public fun fadeIn(
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    initialAlpha: Float = 0f,
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeFadeIn(animationSpec = animationSpec, initialAlpha = initialAlpha)
} else {
    EnterTransition.None
}

public fun fadeOut(
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    targetAlpha: Float = 0f,
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeFadeOut(animationSpec = animationSpec, targetAlpha = targetAlpha)
} else {
    ExitTransition.None
}

public fun slideIn(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    initialOffset: (fullSize: IntSize) -> IntOffset,
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideIn(animationSpec = animationSpec, initialOffset = initialOffset)
} else {
    EnterTransition.None
}

public fun slideOut(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    targetOffset: (fullSize: IntSize) -> IntOffset,
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideOut(animationSpec = animationSpec, targetOffset = targetOffset)
} else {
    ExitTransition.None
}

public fun slideInHorizontally(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    initialOffsetX: (fullWidth: Int) -> Int = { -it / 2 },
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideInHorizontally(animationSpec = animationSpec, initialOffsetX = initialOffsetX)
} else {
    EnterTransition.None
}

public fun slideInVertically(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    initialOffsetY: (fullHeight: Int) -> Int = { -it / 2 },
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideInVertically(animationSpec = animationSpec, initialOffsetY = initialOffsetY)
} else {
    EnterTransition.None
}

public fun slideOutHorizontally(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    targetOffsetX: (fullWidth: Int) -> Int = { -it / 2 },
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideOutHorizontally(animationSpec = animationSpec, targetOffsetX = targetOffsetX)
} else {
    ExitTransition.None
}

public fun slideOutVertically(
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    ),
    targetOffsetY: (fullHeight: Int) -> Int = { -it / 2 },
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeSlideOutVertically(animationSpec = animationSpec, targetOffsetY = targetOffsetY)
} else {
    ExitTransition.None
}

public fun scaleIn(
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    initialScale: Float = 0f,
    transformOrigin: androidx.compose.ui.graphics.TransformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center,
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeScaleIn(
        animationSpec = animationSpec,
        initialScale = initialScale,
        transformOrigin = transformOrigin,
    )
} else {
    EnterTransition.None
}

public fun scaleOut(
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    targetScale: Float = 0f,
    transformOrigin: androidx.compose.ui.graphics.TransformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center,
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeScaleOut(
        animationSpec = animationSpec,
        targetScale = targetScale,
        transformOrigin = transformOrigin,
    )
} else {
    ExitTransition.None
}

public fun expandIn(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    expandFrom: Alignment = Alignment.BottomEnd,
    clip: Boolean = true,
    initialSize: (fullSize: IntSize) -> IntSize = { IntSize(0, 0) },
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeExpandIn(
        animationSpec = animationSpec,
        expandFrom = expandFrom,
        clip = clip,
        initialSize = initialSize,
    )
} else {
    EnterTransition.None
}

public fun shrinkOut(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    shrinkTowards: Alignment = Alignment.BottomEnd,
    clip: Boolean = true,
    targetSize: (fullSize: IntSize) -> IntSize = { IntSize(0, 0) },
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeShrinkOut(
        animationSpec = animationSpec,
        shrinkTowards = shrinkTowards,
        clip = clip,
        targetSize = targetSize,
    )
} else {
    ExitTransition.None
}

public fun expandHorizontally(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    expandFrom: Alignment.Horizontal = Alignment.End,
    clip: Boolean = true,
    initialWidth: (fullWidth: Int) -> Int = { 0 },
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeExpandHorizontally(
        animationSpec = animationSpec,
        expandFrom = expandFrom,
        clip = clip,
        initialWidth = initialWidth,
    )
} else {
    EnterTransition.None
}

public fun expandVertically(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    expandFrom: Alignment.Vertical = Alignment.Bottom,
    clip: Boolean = true,
    initialHeight: (fullHeight: Int) -> Int = { 0 },
): EnterTransition = if (AnimationPolicy.animationsEnabled) {
    composeExpandVertically(
        animationSpec = animationSpec,
        expandFrom = expandFrom,
        clip = clip,
        initialHeight = initialHeight,
    )
} else {
    EnterTransition.None
}

public fun shrinkHorizontally(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    shrinkTowards: Alignment.Horizontal = Alignment.End,
    clip: Boolean = true,
    targetWidth: (fullWidth: Int) -> Int = { 0 },
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeShrinkHorizontally(
        animationSpec = animationSpec,
        shrinkTowards = shrinkTowards,
        clip = clip,
        targetWidth = targetWidth,
    )
} else {
    ExitTransition.None
}

public fun shrinkVertically(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize.VisibilityThreshold,
    ),
    shrinkTowards: Alignment.Vertical = Alignment.Bottom,
    clip: Boolean = true,
    targetHeight: (fullHeight: Int) -> Int = { 0 },
): ExitTransition = if (AnimationPolicy.animationsEnabled) {
    composeShrinkVertically(
        animationSpec = animationSpec,
        shrinkTowards = shrinkTowards,
        clip = clip,
        targetHeight = targetHeight,
    )
} else {
    ExitTransition.None
}

@Composable
public fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = fadeOut() + shrinkOut(),
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    composeAnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (AnimationPolicy.animationsEnabled) enter else EnterTransition.None,
        exit = if (AnimationPolicy.animationsEnabled) exit else ExitTransition.None,
        label = label,
        content = content,
    )
}

@Composable
public fun AnimatedVisibility(
    visibleState: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = fadeOut() + shrinkOut(),
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    composeAnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = if (AnimationPolicy.animationsEnabled) enter else EnterTransition.None,
        exit = if (AnimationPolicy.animationsEnabled) exit else ExitTransition.None,
        label = label,
        content = content,
    )
}

@Composable
public fun <S> AnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
            .togetherWith(fadeOut(animationSpec = tween(90)))
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    // Read the policy during composition so toggling the preference recomposes this call site.
    // Reading it only inside transitionSpec would cache the disabled branch until an unrelated
    // recomposition happened.
    val animationsEnabled = AnimationPolicy.animationsEnabled
    composeAnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            if (animationsEnabled) {
                transitionSpec()
            } else {
                ContentTransform(
                    targetContentEnter = EnterTransition.None,
                    initialContentExit = ExitTransition.None,
                    sizeTransform = SizeTransform(clip = false),
                )
            }
        },
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
        content = content,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun <T> Crossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    label: String = "Crossfade",
    content: @Composable (targetState: T) -> Unit,
) {
    composeCrossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
        label = label,
        content = content,
    )
}

@Composable
public fun InfiniteTransition.animateFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: InfiniteRepeatableSpec<Float>,
    label: String = "FloatAnimation",
): State<Float> = if (AnimationPolicy.animationsEnabled) {
    this@animateFloat.composeAnimateFloat(initialValue, targetValue, animationSpec, label)
} else {
    remember(targetValue) { mutableStateOf(targetValue) }
}

@Composable
public fun animateFloatAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = spring(),
    visibilityThreshold: Float = 0.01f,
    label: String = "FloatAnimation",
    finishedListener: ((Float) -> Unit)? = null,
): State<Float> = composeAnimateFloatAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    visibilityThreshold = visibilityThreshold,
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateDpAsState(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = spring(),
    label: String = "DpAnimation",
    finishedListener: ((Dp) -> Unit)? = null,
): State<Dp> = composeAnimateDpAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateSizeAsState(
    targetValue: Size,
    animationSpec: AnimationSpec<Size> = spring(),
    label: String = "SizeAnimation",
    finishedListener: ((Size) -> Unit)? = null,
): State<Size> = composeAnimateSizeAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring(),
    label: String = "OffsetAnimation",
    finishedListener: ((Offset) -> Unit)? = null,
): State<Offset> = composeAnimateOffsetAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateRectAsState(
    targetValue: Rect,
    animationSpec: AnimationSpec<Rect> = spring(),
    label: String = "RectAnimation",
    finishedListener: ((Rect) -> Unit)? = null,
): State<Rect> = composeAnimateRectAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateIntAsState(
    targetValue: Int,
    animationSpec: AnimationSpec<Int> = spring(),
    label: String = "IntAnimation",
    finishedListener: ((Int) -> Unit)? = null,
): State<Int> = composeAnimateIntAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateIntOffsetAsState(
    targetValue: IntOffset,
    animationSpec: AnimationSpec<IntOffset> = spring(),
    label: String = "IntOffsetAnimation",
    finishedListener: ((IntOffset) -> Unit)? = null,
): State<IntOffset> = composeAnimateIntOffsetAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateIntSizeAsState(
    targetValue: IntSize,
    animationSpec: AnimationSpec<IntSize> = spring(),
    label: String = "IntSizeAnimation",
    finishedListener: ((IntSize) -> Unit)? = null,
): State<IntSize> = composeAnimateIntSizeAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)

@Composable
public fun animateColorAsState(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = spring(),
    label: String = "ColorAnimation",
    finishedListener: ((Color) -> Unit)? = null,
): State<Color> = composeAnimateColorAsState(
    targetValue = targetValue,
    animationSpec = if (AnimationPolicy.animationsEnabled) animationSpec else snap(),
    label = label,
    finishedListener = finishedListener,
)
