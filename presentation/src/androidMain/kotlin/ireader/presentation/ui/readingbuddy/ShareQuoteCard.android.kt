package ireader.presentation.ui.readingbuddy

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

actual class QuoteCardSharer(
    private val context: Context
) {
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val imageFile = createQuoteImage(quote, style)
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, formatQuoteText(quote))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooserIntent = Intent.createChooser(shareIntent, "Share Quote")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to share quote")
        }
    }
    
    private fun createQuoteImage(quote: Quote, style: QuoteCardStyle): File {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val colors = getStyleColors(style)
        
        drawBackground(canvas, width, height, colors, style)
        drawDecorativeElements(canvas, width, height, colors, style)
        drawContentCard(canvas, width, height, quote, colors, style)
        drawBranding(canvas, width, height, colors)
        
        val cacheDir = File(context.cacheDir, "shared_quotes")
        cacheDir.mkdirs()
        val file = File(cacheDir, "quote_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        
        return file
    }
    
    private fun drawBackground(canvas: Canvas, width: Int, height: Int, colors: StyleColors, style: QuoteCardStyle) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val gradient = when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFFFF6B6B.toInt(), 0xFFFF8E53.toInt(), 0xFFFFE66D.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            QuoteCardStyle.GRADIENT_OCEAN -> LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF667EEA.toInt(), 0xFF5B9BD5.toInt(), 0xFF64B5F6.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            QuoteCardStyle.GRADIENT_FOREST -> LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF134E5E.toInt(), 0xFF11998E.toInt(), 0xFF38EF7D.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFFE8D5E8.toInt(), 0xFFD4B8D4.toInt(), 0xFFF3E5F5.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF0F0C29.toInt(), 0xFF302B63.toInt(), 0xFF24243E.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            else -> LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                colors.startColor, colors.endColor,
                Shader.TileMode.CLAMP
            )
        }
        
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        if (style == QuoteCardStyle.PAPER_TEXTURE || style == QuoteCardStyle.BOOK_COVER) {
            drawPaperTexture(canvas, width, height)
        }
    }
    
    private fun drawPaperTexture(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0x08000000
        val random = Random(42)
        repeat(3000) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawCircle(x, y, 1f, paint)
        }
    }
    
    private fun drawDecorativeElements(canvas: Canvas, width: Int, height: Int, colors: StyleColors, style: QuoteCardStyle) {
        when (style) {
            QuoteCardStyle.GRADIENT_SUNSET, QuoteCardStyle.GRADIENT_OCEAN,
            QuoteCardStyle.GRADIENT_FOREST, QuoteCardStyle.GRADIENT_MIDNIGHT -> {
                drawBokehEffect(canvas, width, height, colors)
            }
            QuoteCardStyle.GRADIENT_LAVENDER -> {
                drawSoftShapes(canvas, width, height, colors)
            }
            QuoteCardStyle.MINIMAL_LIGHT, QuoteCardStyle.MINIMAL_DARK -> {
                drawGeometricLines(canvas, width, height, colors)
            }
            QuoteCardStyle.PAPER_TEXTURE -> {
                drawVintageCorners(canvas, width, height, colors)
            }
            QuoteCardStyle.BOOK_COVER -> {
                drawBookElements(canvas, width, height, colors)
            }
        }
    }
    
    private fun drawBokehEffect(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val random = Random(123)
        
        repeat(8) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = 60f + random.nextFloat() * 120f
            
            val gradient = RadialGradient(
                x, y, radius,
                intArrayOf(0x25FFFFFF, 0x00FFFFFF),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawCircle(x, y, radius, paint)
        }
        
        paint.shader = null
        paint.color = 0x40FFFFFF
        repeat(20) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawCircle(x, y, 2f + random.nextFloat() * 4f, paint)
        }
    }
    
    private fun drawSoftShapes(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0x20000000 or (colors.accentColor and 0x00FFFFFF)
        
        val path = Path()
        path.moveTo(0f, 0f)
        path.quadTo(150f, 80f, 100f, 200f)
        path.quadTo(50f, 100f, 0f, 0f)
        canvas.drawPath(path, paint)
        
        path.reset()
        path.moveTo(width.toFloat(), height.toFloat())
        path.quadTo(width - 150f, height - 80f, width - 100f, height - 200f)
        path.quadTo(width - 50f, height - 100f, width.toFloat(), height.toFloat())
        canvas.drawPath(path, paint)
    }
    
    private fun drawGeometricLines(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0x15000000 or (colors.textColor and 0x00FFFFFF)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        
        for (i in 0 until width step 80) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), paint)
        }
        for (i in 0 until height step 80) {
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), paint)
        }
        
        paint.strokeWidth = 3f
        paint.color = 0x50000000 or (colors.accentColor and 0x00FFFFFF)
        canvas.drawLine(40f, 40f, 140f, 40f, paint)
        canvas.drawLine(40f, 40f, 40f, 140f, paint)
        canvas.drawLine(width - 40f, height - 40f, width - 140f, height - 40f, paint)
        canvas.drawLine(width - 40f, height - 40f, width - 40f, height - 140f, paint)
    }
    
    private fun drawVintageCorners(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = 0x35000000 or (colors.textColor and 0x00FFFFFF)
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        
        val cornerSize = 80f
        val margin = 50f
        
        val path = Path()
        path.moveTo(margin, margin + cornerSize)
        path.lineTo(margin, margin)
        path.lineTo(margin + cornerSize, margin)
        canvas.drawPath(path, paint)
        
        path.reset()
        path.moveTo(width - margin - cornerSize, margin)
        path.lineTo(width - margin, margin)
        path.lineTo(width - margin, margin + cornerSize)
        canvas.drawPath(path, paint)
        
        path.reset()
        path.moveTo(margin, height - margin - cornerSize)
        path.lineTo(margin, height - margin)
        path.lineTo(margin + cornerSize, height - margin)
        canvas.drawPath(path, paint)
        
        path.reset()
        path.moveTo(width - margin - cornerSize, height - margin)
        path.lineTo(width - margin, height - margin)
        path.lineTo(width - margin, height - margin - cornerSize)
        canvas.drawPath(path, paint)
    }
    
    private fun drawBookElements(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val spineGradient = LinearGradient(
            0f, 0f, 60f, 0f,
            intArrayOf(0x50000000, 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = spineGradient
        canvas.drawRect(0f, 0f, 60f, height.toFloat(), paint)
        
        paint.shader = null
        paint.color = 0x30000000 or (colors.textColor and 0x00FFFFFF)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        
        val rect = RectF(30f, 30f, width - 30f, height - 30f)
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        
        paint.strokeWidth = 1f
        val innerRect = RectF(45f, 45f, width - 45f, height - 45f)
        canvas.drawRoundRect(innerRect, 4f, 4f, paint)
    }
    
    private fun drawContentCard(canvas: Canvas, width: Int, height: Int, quote: Quote, colors: StyleColors, style: QuoteCardStyle) {
        val cardMargin = 60f
        val cardTop = 180f
        val cardBottom = height - 220f
        val cardRect = RectF(cardMargin, cardTop, width - cardMargin, cardBottom)
        
        if (style in listOf(QuoteCardStyle.MINIMAL_LIGHT, QuoteCardStyle.MINIMAL_DARK,
                QuoteCardStyle.PAPER_TEXTURE, QuoteCardStyle.BOOK_COVER)) {
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            shadowPaint.color = 0x40000000
            shadowPaint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawRoundRect(RectF(cardMargin + 8f, cardTop + 8f, width - cardMargin + 8f, cardBottom + 8f), 24f, 24f, shadowPaint)
            
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            cardPaint.color = if (style == QuoteCardStyle.MINIMAL_DARK) 0xFF2D2D2D.toInt() else 0xFFFFFFF8.toInt()
            canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)
        }
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textAlign = Paint.Align.CENTER
        
        val centerX = width / 2f
        
        // Calculate quote text first to know its height
        textPaint.textSize = 44f
        textPaint.letterSpacing = 0.01f
        val quoteLines = wrapText(quote.text, textPaint, (width - cardMargin * 2 - 80).toInt())
        val lineHeight = 64f
        val quoteTextHeight = quoteLines.size * lineHeight
        
        // Position opening quote mark at top left of text area
        var currentY = cardTop + 60f
        textPaint.textSize = 80f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = colors.quoteMarkColor
        canvas.drawText("❝", cardMargin + 30f, currentY, textPaint)
        
        // Start quote text below the opening quote mark
        currentY += 50f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = colors.textColor
        textPaint.textSize = 44f
        textPaint.letterSpacing = 0.01f
        
        for (line in quoteLines) {
            canvas.drawText(line, centerX, currentY, textPaint)
            currentY += lineHeight
        }
        
        // Closing quote mark at bottom right of text area
        currentY += 10f
        textPaint.textSize = 80f
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = colors.quoteMarkColor
        canvas.drawText("❞", width - cardMargin - 30f, currentY, textPaint)
        currentY += 40f
        
        // Elegant divider
        drawElegantDivider(canvas, centerX, currentY, colors)
        currentY += 45f
        
        // Book title - full opacity
        textPaint.color = colors.textColor
        textPaint.textSize = 34f
        textPaint.isFakeBoldText = true
        textPaint.letterSpacing = 0.08f
        canvas.drawText(quote.bookTitle.uppercase(), centerX, currentY, textPaint)
        currentY += 38f
        
        // Author - slightly dimmed
        if (quote.author.isNotBlank()) {
            textPaint.textSize = 26f
            textPaint.isFakeBoldText = false
            textPaint.letterSpacing = 0.02f
            textPaint.color = colors.secondaryTextColor
            canvas.drawText("by ${quote.author}", centerX, currentY, textPaint)
            currentY += 32f
        }
        
        // Submitter credit
        if (quote.submitterUsername.isNotBlank()) {
            textPaint.textSize = 20f
            textPaint.color = colors.tertiaryTextColor
            canvas.drawText("Shared by ${quote.submitterUsername}", centerX, currentY + 15f, textPaint)
        }
    }
    
    private fun drawElegantDivider(canvas: Canvas, centerX: Float, y: Float, colors: StyleColors) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = colors.dividerColor
        paint.strokeWidth = 2f
        paint.strokeCap = Paint.Cap.ROUND
        
        val path = Path()
        path.moveTo(centerX, y - 8f)
        path.lineTo(centerX + 8f, y)
        path.lineTo(centerX, y + 8f)
        path.lineTo(centerX - 8f, y)
        path.close()
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        
        paint.style = Paint.Style.STROKE
        canvas.drawLine(centerX - 100f, y, centerX - 20f, y, paint)
        canvas.drawLine(centerX + 20f, y, centerX + 100f, y, paint)
        
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX - 105f, y, 4f, paint)
        canvas.drawCircle(centerX + 105f, y, 4f, paint)
    }
    
    private fun drawBranding(canvas: Canvas, width: Int, height: Int, colors: StyleColors) {
        val centerX = width / 2f
        val brandingY = height - 70f
        
        // Load and draw app icon
        try {
            val iconDrawable = context.packageManager.getApplicationIcon(context.packageName)
            val iconSize = 64
            val iconBitmap = iconDrawable.toBitmap(iconSize, iconSize)
            
            val iconX = centerX - 90f
            val iconY = brandingY - 45f
            
            // Draw circular background for icon
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            bgPaint.color = 0x20000000 or (colors.textColor and 0x00FFFFFF)
            canvas.drawCircle(iconX + iconSize / 2f, iconY + iconSize / 2f, (iconSize / 2f) + 4f, bgPaint)
            
            // Draw icon
            canvas.drawBitmap(iconBitmap, iconX, iconY, null)
            iconBitmap.recycle()
            
            // App name next to icon
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textSize = 32f
            textPaint.isFakeBoldText = true
            textPaint.letterSpacing = 0.05f
            textPaint.color = colors.brandingTextColor
            canvas.drawText("IReader", centerX + 10f, brandingY - 12f, textPaint)
            
            // Tagline
            textPaint.textSize = 18f
            textPaint.isFakeBoldText = false
            textPaint.letterSpacing = 0.02f
            textPaint.color = colors.brandingSubtextColor
            canvas.drawText("Your Reading Companion", centerX + 10f, brandingY + 12f, textPaint)
            
        } catch (e: Exception) {
            // Fallback if icon loading fails
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 28f
            textPaint.isFakeBoldText = true
            textPaint.color = colors.brandingTextColor
            canvas.drawText("📚 IReader", centerX, brandingY, textPaint)
        }
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
    
    private fun getStyleColors(style: QuoteCardStyle): StyleColors {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> StyleColors(
                startColor = 0xFFFF6B6B.toInt(),
                endColor = 0xFFFFE66D.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
                secondaryTextColor = 0xE6FFFFFF.toInt(),
                tertiaryTextColor = 0xB3FFFFFF.toInt(),
                accentColor = 0xFFFFFFFF.toInt(),
                quoteMarkColor = 0x80FFFFFF.toInt(),
                dividerColor = 0x99FFFFFF.toInt(),
                brandingTextColor = 0xFFFFFFFF.toInt(),
                brandingSubtextColor = 0xB3FFFFFF.toInt()
            )
            QuoteCardStyle.GRADIENT_OCEAN -> StyleColors(
                startColor = 0xFF667EEA.toInt(),
                endColor = 0xFF64B5F6.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
                secondaryTextColor = 0xE6FFFFFF.toInt(),
                tertiaryTextColor = 0xB3FFFFFF.toInt(),
                accentColor = 0xFFE3F2FD.toInt(),
                quoteMarkColor = 0x80FFFFFF.toInt(),
                dividerColor = 0x99FFFFFF.toInt(),
                brandingTextColor = 0xFFFFFFFF.toInt(),
                brandingSubtextColor = 0xB3FFFFFF.toInt()
            )
            QuoteCardStyle.GRADIENT_FOREST -> StyleColors(
                startColor = 0xFF11998E.toInt(),
                endColor = 0xFF38EF7D.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
                secondaryTextColor = 0xE6FFFFFF.toInt(),
                tertiaryTextColor = 0xB3FFFFFF.toInt(),
                accentColor = 0xFFE8F5E9.toInt(),
                quoteMarkColor = 0x80FFFFFF.toInt(),
                dividerColor = 0x99FFFFFF.toInt(),
                brandingTextColor = 0xFFFFFFFF.toInt(),
                brandingSubtextColor = 0xB3FFFFFF.toInt()
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> StyleColors(
                startColor = 0xFFE8D5E8.toInt(),
                endColor = 0xFFF3E5F5.toInt(),
                textColor = 0xFF4A148C.toInt(),
                secondaryTextColor = 0xE64A148C.toInt(),
                tertiaryTextColor = 0x994A148C.toInt(),
                accentColor = 0xFF7B1FA2.toInt(),
                quoteMarkColor = 0x607B1FA2.toInt(),
                dividerColor = 0x807B1FA2.toInt(),
                brandingTextColor = 0xFF4A148C.toInt(),
                brandingSubtextColor = 0x994A148C.toInt()
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> StyleColors(
                startColor = 0xFF232526.toInt(),
                endColor = 0xFF414345.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
                secondaryTextColor = 0xE6FFFFFF.toInt(),
                tertiaryTextColor = 0xB3FFFFFF.toInt(),
                accentColor = 0xFF90CAF9.toInt(),
                quoteMarkColor = 0x6090CAF9.toInt(),
                dividerColor = 0x8090CAF9.toInt(),
                brandingTextColor = 0xFFFFFFFF.toInt(),
                brandingSubtextColor = 0xB3FFFFFF.toInt()
            )
            QuoteCardStyle.MINIMAL_LIGHT -> StyleColors(
                startColor = 0xFFF5F5F5.toInt(),
                endColor = 0xFFFFFFFF.toInt(),
                textColor = 0xFF212121.toInt(),
                secondaryTextColor = 0xE6212121.toInt(),
                tertiaryTextColor = 0x99212121.toInt(),
                accentColor = 0xFF757575.toInt(),
                quoteMarkColor = 0x40757575.toInt(),
                dividerColor = 0x60757575.toInt(),
                brandingTextColor = 0xFF424242.toInt(),
                brandingSubtextColor = 0x99424242.toInt()
            )
            QuoteCardStyle.MINIMAL_DARK -> StyleColors(
                startColor = 0xFF1E1E1E.toInt(),
                endColor = 0xFF2D2D2D.toInt(),
                textColor = 0xFFE0E0E0.toInt(),
                secondaryTextColor = 0xCCE0E0E0.toInt(),
                tertiaryTextColor = 0x99E0E0E0.toInt(),
                accentColor = 0xFF9E9E9E.toInt(),
                quoteMarkColor = 0x509E9E9E.toInt(),
                dividerColor = 0x709E9E9E.toInt(),
                brandingTextColor = 0xFFE0E0E0.toInt(),
                brandingSubtextColor = 0x99E0E0E0.toInt()
            )
            QuoteCardStyle.PAPER_TEXTURE -> StyleColors(
                startColor = 0xFFF5F5DC.toInt(),
                endColor = 0xFFFAF0E6.toInt(),
                textColor = 0xFF3E2723.toInt(),
                secondaryTextColor = 0xE63E2723.toInt(),
                tertiaryTextColor = 0x993E2723.toInt(),
                accentColor = 0xFF5D4037.toInt(),
                quoteMarkColor = 0x505D4037.toInt(),
                dividerColor = 0x705D4037.toInt(),
                brandingTextColor = 0xFF3E2723.toInt(),
                brandingSubtextColor = 0x993E2723.toInt()
            )
            QuoteCardStyle.BOOK_COVER -> StyleColors(
                startColor = 0xFF8D6E63.toInt(),
                endColor = 0xFFA1887F.toInt(),
                textColor = 0xFFFFF8E1.toInt(),
                secondaryTextColor = 0xE6FFF8E1.toInt(),
                tertiaryTextColor = 0xB3FFF8E1.toInt(),
                accentColor = 0xFFFFECB3.toInt(),
                quoteMarkColor = 0x60FFECB3.toInt(),
                dividerColor = 0x80FFECB3.toInt(),
                brandingTextColor = 0xFFFFF8E1.toInt(),
                brandingSubtextColor = 0xB3FFF8E1.toInt()
            )
        }
    }
    
    private fun formatQuoteText(quote: Quote): String {
        return buildString {
            append("\"${quote.text}\"\n\n")
            append("— ${quote.bookTitle}")
            if (quote.author.isNotBlank()) {
                append(" by ${quote.author}")
            }
            append("\n\n📚 Shared via IReader")
        }
    }
    
    data class StyleColors(
        val startColor: Int,
        val endColor: Int,
        val textColor: Int,
        val secondaryTextColor: Int,
        val tertiaryTextColor: Int,
        val accentColor: Int,
        val quoteMarkColor: Int,
        val dividerColor: Int,
        val brandingTextColor: Int,
        val brandingSubtextColor: Int
    )
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    val context = LocalContext.current
    return remember { QuoteCardSharer(context) }
}
