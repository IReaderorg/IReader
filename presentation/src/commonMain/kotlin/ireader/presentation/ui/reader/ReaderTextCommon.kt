package ireader.presentation.ui.reader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.prefs.mapTextAlign
import ireader.i18n.resources.Res
import ireader.i18n.resources.bounce
import ireader.i18n.resources.bounceoffset
import ireader.i18n.resources.chapter_complete
import ireader.i18n.resources.continue_reading
import ireader.i18n.resources.float
import ireader.i18n.resources.glow
import ireader.i18n.resources.image
import ireader.i18n.resources.loading_1
import ireader.i18n.resources.next_chapter
import ireader.i18n.resources.pulse
import ireader.i18n.resources.release_for_previous
import ireader.i18n.resources.the_end
import ireader.i18n.resources.view_comments
import ireader.i18n.resources.void
import ireader.i18n.resources.youve_completed_this_story
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.core.toComposeTextAlign
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.components.SelectableTranslatableText
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

@androidx.compose.runtime.Stable
internal data class TextStyleParams(
    val fontSize: Int,
    val lineHeight: Int,
    val letterSpacing: Int,
    val fontWeight: Int,
    val paragraphIndent: Int,
    val textAlignment: PreferenceValues.PreferenceTextAlignment,
    val textColor: Color,
    val fontFamily: androidx.compose.ui.text.font.FontFamily?
) {
    val textStyle: androidx.compose.ui.text.TextStyle by lazy {
        androidx.compose.ui.text.TextStyle(
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = letterSpacing.sp,
            fontWeight = FontWeight(fontWeight),
            fontFamily = fontFamily,
            color = textColor,
            textAlign = mapTextAlign(textAlignment).toComposeTextAlign()
        )
    }
}

@Composable
internal fun MainText(
    modifier: Modifier,
    index: Int,
    page: Page,
    vm: ReaderScreenViewModel
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val context = LocalPlatformContext.current

    androidx.compose.runtime.key(page) {
        when (page) {
            is Text -> {
                val textStyleParams = remember(
                    vm.fontSize.lazyValue,
                    vm.lineHeight.lazyValue,
                    vm.betweenLetterSpaces.lazyValue,
                    vm.textWeight.lazyValue,
                    vm.paragraphsIndent.lazyValue,
                    vm.textAlignment.value,
                    vm.textColor.value,
                    vm.font?.value,
                    vm.fontVersion
                ) {
                    TextStyleParams(
                        fontSize = vm.fontSize.lazyValue,
                        lineHeight = vm.lineHeight.lazyValue,
                        letterSpacing = vm.betweenLetterSpaces.lazyValue,
                        fontWeight = vm.textWeight.lazyValue,
                        paragraphIndent = vm.paragraphsIndent.lazyValue,
                        textAlignment = vm.textAlignment.value,
                        textColor = vm.textColor.value.toComposeColor(),
                        fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily()
                    )
                }
                StyleTextOptimized(modifier, vm, index, page, vm.bionicReadingMode.value, textStyleParams)
            }
            is ImageUrl -> {
                val isLoading = remember { mutableStateOf(false) }
                Box(contentAlignment = Alignment.Center) {
                    IImageLoader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(500.dp),
                        model = ImageRequest.Builder(context=context).data(page.url.toUri()).diskCachePolicy(CachePolicy.DISABLED).build(),
                        contentDescription = localizeHelper.localize(Res.string.image),
                        contentScale = ContentScale.FillWidth,
                        onLoading = { isLoading.value = true },
                        onError = { isLoading.value = false },
                        onSuccess = { isLoading.value = false },
                    )
                    if (isLoading.value) {
                        CircularProgressIndicator()
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
internal fun StyleTextOptimized(
    modifier: Modifier,
    vm: ReaderScreenViewModel,
    index: Int,
    page: Text,
    enableBioReading: Boolean,
    styleParams: TextStyleParams
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success
    val currentContent = successState?.currentContent ?: emptyList()
    val isLastIndex = index == currentContent.lastIndex

    val originalText = remember(
        page.text,
        index,
        isLastIndex,
        vm.topContentPadding.lazyValue,
        vm.distanceBetweenParagraphs.lazyValue,
        vm.bottomContentPadding.lazyValue,
        vm.paragraphsIndent.lazyValue
    ) {
        setText(
            text = page.text,
            index = index,
            isLast = isLastIndex,
            topContentPadding = vm.topContentPadding.lazyValue,
            contentPadding = vm.distanceBetweenParagraphs.lazyValue,
            bottomContentPadding = vm.bottomContentPadding.lazyValue,
            paragraphIndent = vm.paragraphsIndent.lazyValue
        )
    }

    val bilingualModeEnabled = vm.bilingualModeEnabled.value
    val translatedText = vm.getTranslationForParagraph(index)

    if (bilingualModeEnabled && translatedText != null) {
        val bilingualMode = if (vm.bilingualModeLayout.value == 0) {
            ireader.presentation.ui.reader.components.BilingualMode.SIDE_BY_SIDE
        } else {
            ireader.presentation.ui.reader.components.BilingualMode.PARAGRAPH_BY_PARAGRAPH
        }

        ireader.presentation.ui.reader.components.BilingualText(
            originalText = originalText,
            translatedText = translatedText,
            mode = bilingualMode,
            modifier = modifier
                .fillMaxWidth(),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            originalColor = styleParams.textColor,
            translatedColor = styleParams.textColor.copy(alpha = 0.9f),
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight)
        )
    } else if (enableBioReading) {
        val bionicText = remember(originalText, styleParams.fontWeight) {
            buildAnnotatedString {
                originalText.split(" ").forEach { s ->
                    s.forEachIndexed { charIndex, c ->
                        if (charIndex <= (s.length / 2)) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(c)
                            }
                        } else {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                                append(c)
                            }
                        }
                    }
                    append(" ")
                }
            }
        }

        Text(
            text = bionicText,
            modifier = modifier
                .fillMaxWidth(),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            color = styleParams.textColor,
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight),
        )
    } else {
        SelectableTranslatableText(
            text = originalText,
            modifier = modifier
                .fillMaxWidth(),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            color = styleParams.textColor,
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight),
            selectable = vm.selectableMode.value,
            paragraphTranslationEnabled = vm.paragraphTranslationEnabled.value,
            onTranslateRequest = { selectedText ->
                vm.showParagraphTranslation(selectedText)
            }
        )
    }
}

@Composable
internal fun ChapterVoidSpace(
    chapter: Chapter,
    isLast: Boolean,
    textColor: Color,
    backgroundColor: Color = Color.Black,
    onShowComments: () -> Unit,
    onNextChapter: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.void))

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.float)
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.glow)
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.pulse)
    )

    val contentTextColor = textColor
    val contentTextColorMuted = textColor.copy(alpha = 0.6f)
    val contentTextColorSubtle = textColor.copy(alpha = 0.35f)
    val accentColor = textColor.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    translationY = floatOffset
                    alpha = glowAlpha * 0.3f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            textColor.copy(alpha = 0.15f),
                            textColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                contentTextColorSubtle,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { translationY = floatOffset }
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = localizeHelper.localize(Res.string.chapter_complete),
                    color = contentTextColorMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = chapter.name,
                    color = contentTextColor.copy(alpha = 0.9f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = onShowComments,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentTextColor
                ),
                border = BorderStroke(1.dp, contentTextColorSubtle),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 150.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentTextColorSubtle,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.view_comments),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentTextColorSubtle,
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.loading_1),
                    color = contentTextColorMuted,
                    fontSize = 12.sp
                )
            }

            if (!isLast && !isLoading) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .graphicsLayer { alpha = dotAlpha }
                                .background(contentTextColorMuted, RoundedCornerShape(2.5.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNextChapter() }
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.continue_reading),
                        color = contentTextColorMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = localizeHelper.localize(Res.string.next_chapter),
                        tint = accentColor,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { translationY = floatOffset * 0.3f }
                    )
                }
            } else if (isLast) {
                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { translationY = floatOffset }
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.the_end),
                        color = contentTextColorMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.youve_completed_this_story),
                        color = contentTextColorSubtle,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                contentTextColorSubtle,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

internal fun getHighlightColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red +
                    0.587 * backgroundColor.green +
                    0.114 * backgroundColor.blue)

    return if (luminance > 0.5) {
        Color(0xFFFFC107).copy(alpha = 0.45f)
    } else {
        Color(0xFFFFEB3B).copy(alpha = 0.35f)
    }
}

internal fun setText(
    text: String,
    index: Int,
    isLast: Boolean,
    topContentPadding: Int,
    bottomContentPadding: Int,
    contentPadding: Int,
    paragraphIndent: Int = 0,
): String {
    if (text.isEmpty()) {
        return ""
    }

    val stringBuilder = StringBuilder()

    if (index == 0 && topContentPadding > 0) {
        stringBuilder.append("\n".repeat(topContentPadding.coerceAtLeast(0)))
    }

    val cleanedText = if (index == 0) {
        text.trimStart()
    } else {
        text
    }

    val indentSpaces = if (paragraphIndent > 0) {
        " ".repeat((paragraphIndent / 2).coerceAtLeast(0))
    } else {
        ""
    }

    stringBuilder.append(indentSpaces)
    stringBuilder.append(cleanedText)

    if (isLast && bottomContentPadding > 0) {
        stringBuilder.append("\n".repeat(bottomContentPadding.coerceAtLeast(0)))
    }

    if (index > 0 && contentPadding > 0) {
        stringBuilder.append("\n".repeat(contentPadding.coerceAtLeast(0)))
    }

    return stringBuilder.toString()
}
