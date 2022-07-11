package org.ireader.components.components.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.R
import kotlin.math.round

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    initialColor: Color = Color.Unspecified,
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    var showPresets by remember { mutableStateOf(true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        text = {
            if (showPresets) {
                ColorPresets(
                    initialColor = currentColor,
                    onColorChanged = { currentColor = it }
                )
            } else {
                ColorPalette(
                    initialColor = currentColor,
                    onColorChanged = { currentColor = it }
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                onSelected(currentColor)
            }) {
                androidx.compose.material3.Text(stringResource(R.string.select))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = {
                showPresets = !showPresets
            }) {
                val text =
                    if (showPresets) R.string.presents else R.string.custom
                androidx.compose.material3.Text(stringResource(text))
            }
        }
    )
}

@Composable
private fun ColorPresets(
    initialColor: Color,
    onColorChanged: (Color) -> Unit
) {
    val presets = remember {
        if (initialColor.isSpecified) {
            (listOf(initialColor) + presetColors).distinct()
        } else {
            presetColors
        }
    }

    var selectedColor by remember { mutableStateOf(initialColor.takeOrElse { presets.first() }) }
    var selectedShade by remember { mutableStateOf<Color?>(null) }

    val shades = remember(selectedColor) { getColorShades(selectedColor) }

    val borderColor = MaterialTheme.colors.onBackground.copy(alpha = 0.54f)

    Column {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(56.dp)
        ) {
            items(presets) { color ->
                ColorPresetItem(
                    color = color,
                    borderColor = borderColor,
                    isSelected = selectedShade == null && initialColor == color,
                    onClick = {
                        selectedShade = null
                        selectedColor = color
                        onColorChanged(color)
                    }
                )
            }
        }
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        LazyRow {
            items(shades) { color ->
                ColorPresetItem(
                    color = color,
                    borderColor = borderColor,
                    isSelected = selectedShade == color,
                    onClick = {
                        selectedShade = color
                        onColorChanged(color)
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorPresetItem(
    color: Color,
    borderColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .requiredSize(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                tint = if (color.luminance() > 0.5) Color.Black else Color.White,
                contentDescription = null,
                modifier = Modifier
                    .requiredWidth(32.dp)
                    .requiredHeight(32.dp)
            )
        }
    }
}

private fun getColorShades(color: Color): List<Color> {
    // Remove transparency from color
    val c = (0xFFFFFF and color.toArgb()).toLong()
    return listOf(
        shadeColor(c, 0.9), shadeColor(c, 0.7), shadeColor(c, 0.5),
        shadeColor(c, 0.333), shadeColor(c, 0.166), shadeColor(c, -0.125),
        shadeColor(c, -0.25), shadeColor(c, -0.375), shadeColor(c, -0.5),
        shadeColor(c, -0.675), shadeColor(c, -0.7), shadeColor(c, -0.775)
    )
}

private fun shadeColor(f: Long, percent: Double): Color {
    val t = if (percent < 0) 0.0 else 255.0
    val p = if (percent < 0) percent * -1 else percent
    val r = f shr 16
    val g = f shr 8 and 0x00FF
    val b = f and 0x0000FF

    val red = (round((t - r) * p) + r).toInt()
    val green = (round((t - g) * p) + g).toInt()
    val blue = (round((t - b) * p) + b).toInt()
    return Color(red = red, green = green, blue = blue, alpha = 255)
}

@Composable
fun ColorPalette(
    initialColor: Color = Color.White,
    onColorChanged: (Color) -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    var textFieldHex by remember { mutableStateOf(initialColor.toHexString()) }

    var hue by remember { mutableStateOf(initialColor.toHsv()[0]) }
    var hueCursor by remember { mutableStateOf(0f) }

    var matrixSize by remember { mutableStateOf(IntSize(0, 0)) }
    var matrixCursor by remember { mutableStateOf(Offset(0f, 0f)) }

    val hueColor = remember(hue) { hueToColor(hue) }

    val cursorColor = MaterialTheme.colors.onBackground
    val cursorStroke = Stroke(4f)
    val borderStroke = BorderStroke(Dp.Hairline, Color.LightGray)

    fun setSelectedColor(color: Color, invalidate: Boolean = false) {
        selectedColor = color
        textFieldHex = color.toHexString()
        if (invalidate) {
            val hsv = color.toHsv()
            hue = hsv[0]
            matrixCursor = satValToCoordinates(hsv[1], hsv[2], matrixSize)
            hueCursor = hueToCoordinate(hsv[0], matrixSize)
        }
        onColorChanged(color)
    }

    Column {
        Row(Modifier.height(IntrinsicSize.Max)) {
            Box(
                Modifier
                    .aspectRatio(1f)
                    .weight(1f)
                    .onSizeChanged {
                        matrixSize = it
                        val hsv = selectedColor.toHsv()
                        matrixCursor = satValToCoordinates(hsv[1], hsv[2], it)
                        hueCursor = hueToCoordinate(hue, it)
                    }
                    .background(hueColor)
                    .background(Brush.horizontalGradient(listOf(Color.White, Color.Transparent)))
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    .border(borderStroke)
                    .drawWithContent {
                        drawCircle(
                            Color.Black,
                            radius = 8f,
                            center = matrixCursor,
                            style = cursorStroke
                        )
                        drawCircle(
                            Color.LightGray,
                            radius = 12f,
                            center = matrixCursor,
                            style = cursorStroke
                        )
                    }
                    .pointerInput(Unit) {
                        detectMove { offset ->
                            val safeOffset = offset.copy(
                                x = offset.x.coerceIn(0f, matrixSize.width.toFloat()),
                                y = offset.y.coerceIn(0f, matrixSize.height.toFloat())
                            )
                            matrixCursor = safeOffset
                            val newColor = matrixCoordinatesToColor(hue, safeOffset, matrixSize)
                            setSelectedColor(newColor)
                        }
                    }
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .requiredWidth(48.dp)
                    .padding(start = 8.dp)
                    .drawWithCache {
                        var h = 360f
                        val colors = MutableList(size.height.toInt()) {
                            hueToColor(h).also {
                                h -= 360f / size.height
                            }
                        }
                        val cursorSize = Size(size.width, 10f)
                        val cursorTopLeft = Offset(0f, hueCursor - (cursorSize.height / 2))
                        onDrawBehind {
                            colors.forEachIndexed { i, color ->
                                val pos = i.toFloat()
                                drawLine(color, Offset(0f, pos), Offset(size.width, pos))
                            }
                            drawRect(
                                cursorColor,
                                topLeft = cursorTopLeft,
                                size = cursorSize,
                                style = cursorStroke
                            )
                        }
                    }
                    .border(borderStroke)
                    .pointerInput(Unit) {
                        detectMove { offset ->
                            val safeY = offset.y.coerceIn(0f, matrixSize.height.toFloat())
                            hueCursor = safeY
                            hue = hueCoordinatesToHue(safeY, matrixSize)
                            val newColor = matrixCoordinatesToColor(hue, matrixCursor, matrixSize)
                            setSelectedColor(newColor)
                        }
                    }
            )
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier
                    .size(72.dp, 48.dp)
                    .background(selectedColor)
                    .border(1.dp, MaterialTheme.colors.onBackground.copy(alpha = 0.54f))
            )
            Spacer(Modifier.requiredWidth(32.dp))
            OutlinedTextField(
                value = textFieldHex,
                onValueChange = {
                    val newColor = hexStringToColor(it)
                    if (newColor != null) {
                        setSelectedColor(newColor, invalidate = true)
                    } else {
                        textFieldHex = it
                    }
                }
            )
        }
    }
}

private suspend fun PointerInputScope.detectMove(onMove: (Offset) -> Unit) {
    forEachGesture {
        awaitPointerEventScope {
            var change = awaitFirstDown()
            while (change.pressed) {
                onMove(change.position)
                change = awaitPointerEvent().changes.first()
            }
        }
    }
}

// Coordinates <-> Color

private fun matrixCoordinatesToColor(hue: Float, position: Offset, size: IntSize): Color {
    val saturation = 1f / size.width * position.x
    val value = 1f - (1f / size.height * position.y)
    return hsvToColor(hue, saturation, value)
}

private fun hueCoordinatesToHue(y: Float, size: IntSize): Float {
    val hue = 360f - y * 360f / size.height
    return hsvToColor(hue, 1f, 1f).toHsv()[0]
}

private fun satValToCoordinates(saturation: Float, value: Float, size: IntSize): Offset {
    return Offset(saturation * size.width, ((1f - value) * size.height))
}

private fun hueToCoordinate(hue: Float, size: IntSize): Float {
    return size.height - (hue * size.height / 360f)
}

// Color space conversions

@OptIn(ExperimentalGraphicsApi::class)
private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    return Color.hsv(hue, saturation, value)
}

// Adapted from Skia's implementation
private fun Color.toHsv(): FloatArray {
    val min = minOf(red, green, blue)
    val value = maxOf(red, green, blue)
    val delta = value - min
    if (delta == 0f) { // we're a shade of gray
        return floatArrayOf(0f, 0f, value)
    }

    val sat = delta / value
    val hue = (
        when {
            red == value -> (green - blue) / delta
            green == value -> 2 + (blue - red) / delta
            else -> 4 + (red - green) / delta
        } * 60
        ).let { if (it < 0) it + 360 else it }
    return floatArrayOf(hue, sat, value)
}

private fun hueToColor(hue: Float): Color {
    return hsvToColor(hue, 1f, 1f)
}

private fun Color.toHexString(): String {
    return buildString {
        append("#")
        val color = (0xFFFFFF and toArgb()).toString(16).uppercase()
        repeat(6 - color.length) { append(0) } // Prepend 0s if needed
        append(color)
    }
}

private fun hexStringToColor(hex: String): Color? {
    return try {
        Color(0xFF000000 or hex.removePrefix("#").toLong(16))
    } catch (e: Exception) {
        null
    }
}

private val presetColors = listOf(
    Color(0xFFF44336), // RED 500
    Color(0xFFE91E63), // PINK 500
    Color(0xFFFF2C93), // LIGHT PINK 500
    Color(0xFF9C27B0), // PURPLE 500
    Color(0xFF673AB7), // DEEP PURPLE 500
    Color(0xFF3F51B5), // INDIGO 500
    Color(0xFF2196F3), // BLUE 500
    Color(0xFF03A9F4), // LIGHT BLUE 500
    Color(0xFF00BCD4), // CYAN 500
    Color(0xFF009688), // TEAL 500
    Color(0xFF4CAF50), // GREEN 500
    Color(0xFF8BC34A), // LIGHT GREEN 500
    Color(0xFFCDDC39), // LIME 500
    Color(0xFFFFEB3B), // YELLOW 500
    Color(0xFFFFC107), // AMBER 500
    Color(0xFFFF9800), // ORANGE 500
    Color(0xFF795548), // BROWN 500
    Color(0xFF607D8B), // BLUE GREY 500
    Color(0xFF9E9E9E), // GREY 500
)
