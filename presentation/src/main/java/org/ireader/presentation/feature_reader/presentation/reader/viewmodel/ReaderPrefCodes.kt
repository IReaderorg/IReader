package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.ireader.core.utils.findComponentActivity
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.fonts
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import javax.inject.Inject

interface ReaderPrefFunctions {
    fun ReaderScreenViewModel.readPreferences()
    fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean? = null)
    fun ReaderScreenViewModel.saveBrightness(brightness: Float, context: Context)
    fun ReaderScreenViewModel.saveFont(index: Int)
    fun ReaderScreenViewModel.saveFontSize(event: FontSizeEvent)
    fun ReaderScreenViewModel.saveParagraphIndent(isIncreased: Boolean)
    fun ReaderScreenViewModel.saveScrollIndicatorWidth(increase: Boolean)
    fun ReaderScreenViewModel.saveScrollIndicatorPadding(increase: Boolean)
    fun ReaderScreenViewModel.saveFontHeight(isIncreased: Boolean)
    fun ReaderScreenViewModel.saveOrientation(context: Context)
    fun ReaderScreenViewModel.readImmersiveMode(context: Context)
    fun ReaderScreenViewModel.toggleImmersiveMode(context: Context)
    fun ReaderScreenViewModel.toggleSelectableMode()
    fun ReaderScreenViewModel.toggleAutoScrollMode()
    fun ReaderScreenViewModel.toggleScrollMode()
    fun ReaderScreenViewModel.saveParagraphDistance(isIncreased: Boolean)
    fun ReaderScreenViewModel.readScrollIndicatorPadding(): Int
    fun ReaderScreenViewModel.readScrollIndicatorWidth(): Int
    fun ReaderScreenViewModel.readBrightness(context: Context)
    fun ReaderScreenViewModel.readBackgroundColor(): Color
    fun ReaderScreenViewModel.readTextColor(): Color
    fun ReaderScreenViewModel.readOrientation(context: Context)
    fun ReaderScreenViewModel.changeBackgroundColor(colorIndex: Int)
    fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color)
    fun ReaderScreenViewModel.setReaderTextColor(color: Color)
    fun ReaderScreenViewModel.setAutoScrollIntervalReader(increase: Boolean)
    fun ReaderScreenViewModel.setAutoScrollOffsetReader(increase: Boolean)
    fun ReaderScreenViewModel.toggleAutoBrightness()
    fun ReaderScreenViewModel.toggleRemoteLoaded(loaded: Boolean)
    fun ReaderScreenViewModel.showSystemBars(context: Context)
    fun ReaderScreenViewModel.hideSystemBars(context: Context)
}

class ReaderPrefFunctionsImpl @Inject constructor() : ReaderPrefFunctions {
    override fun ReaderScreenViewModel.readPreferences() {
        font = readerUseCases.selectedFontStateUseCase.readFont()
       // readerUseCases.selectedFontStateUseCase.saveFont(0)


        this.fontSize = readerUseCases.fontSizeStateUseCase.read()

        readBackgroundColor()
        readTextColor()

        lineHeight = readerUseCases.fontHeightUseCase.read()
        distanceBetweenParagraphs = readerUseCases.paragraphDistanceUseCase.read()
        paragraphsIndent = readerUseCases.paragraphIndentUseCase.read()
        verticalScrolling = readerUseCases.scrollModeUseCase.read()
        scrollIndicatorPadding = readerUseCases.scrollIndicatorUseCase.readPadding()
        scrollIndicatorWith = readerUseCases.scrollIndicatorUseCase.readWidth()
        autoScrollInterval = readerUseCases.autoScrollMode.readInterval()
        autoScrollOffset = readerUseCases.autoScrollMode.readOffset()
        autoBrightnessMode = readerUseCases.brightnessStateUseCase.readAutoBrightness()
        speechSpeed = speechPrefUseCases.readRate()
        pitch = speechPrefUseCases.readPitch()
        currentVoice = speechPrefUseCases.readVoice()
        currentLanguage = speechPrefUseCases.readLanguage()
        selectableMode = readerUseCases.selectedFontStateUseCase.readSelectableText()
        autoNextChapter = speechPrefUseCases.readAutoNext()
    }

    override fun ReaderScreenViewModel.toggleReaderMode(enable: Boolean?) {
        isReaderModeEnable = enable ?: !state.isReaderModeEnable
        isMainBottomModeEnable = true
        isSettingModeEnable = false
    }

    override fun ReaderScreenViewModel.saveBrightness(brightness: Float, context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            this.brightness = brightness
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
            readerUseCases.brightnessStateUseCase.saveBrightness(brightness)
        }
    }

    override fun ReaderScreenViewModel.saveFont(index: Int) {
        this.font = fonts[index]
        readerUseCases.selectedFontStateUseCase.saveFont(index)

    }

    override fun ReaderScreenViewModel.saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            this.fontSize = this.fontSize + 1
            readerUseCases.fontSizeStateUseCase.save(prefState.fontSize)
        } else {
            if (prefState.fontSize > 0) {
                this.fontSize = this.fontSize - 1
                readerUseCases.fontSizeStateUseCase.save(prefState.fontSize)
            }
        }
    }

    override fun ReaderScreenViewModel.saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = prefState.paragraphsIndent
        if (isIncreased) {
            this.paragraphsIndent = paragraphsIndent + 1
            readerUseCases.paragraphIndentUseCase.save(this.paragraphsIndent)


        } else if (paragraphsIndent > 1 && !isIncreased) {
            this.paragraphsIndent = paragraphsIndent - 1
            readerUseCases.paragraphIndentUseCase.save( this.paragraphsIndent)
        }
    }

    override fun ReaderScreenViewModel.saveScrollIndicatorWidth(increase: Boolean) {
        if (increase) {
            scrollIndicatorWith += 1
            readerUseCases.scrollIndicatorUseCase.saveWidth(scrollIndicatorWith)
        } else if (scrollIndicatorWith > 0){
            scrollIndicatorWith -= 1
            readerUseCases.scrollIndicatorUseCase.saveWidth(scrollIndicatorWith)
        }
    }

    override fun ReaderScreenViewModel.saveScrollIndicatorPadding(increase: Boolean) {
        if (increase) {
            scrollIndicatorPadding += 1
            readerUseCases.scrollIndicatorUseCase.savePadding(scrollIndicatorPadding)
        } else if (scrollIndicatorPadding > 0) {
            scrollIndicatorPadding -= 1
            readerUseCases.scrollIndicatorUseCase.savePadding(scrollIndicatorPadding)
        }
    }

    override fun ReaderScreenViewModel.saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = prefState.lineHeight
        if (isIncreased) {
            lineHeight = currentFontHeight + 1
            readerUseCases.fontHeightUseCase.save(lineHeight)

        } else if (currentFontHeight > 20 && !isIncreased) {
            lineHeight = currentFontHeight - 1
            readerUseCases.fontHeightUseCase.save(lineHeight)

        }
    }

    override fun ReaderScreenViewModel.saveOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (prefState.orientation) {
                is Orientation.Landscape -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation = Orientation.Portrait
                    readerUseCases.orientationUseCase.save(OrientationMode.Portrait)

                }
                is Orientation.Portrait -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation = Orientation.Landscape
                    readerUseCases.orientationUseCase.save(OrientationMode.Landscape)
                }
            }
        }
    }

    override fun ReaderScreenViewModel.readImmersiveMode(context: Context) {
        immersiveMode = readerUseCases.immersiveModeUseCase.read()
        if (immersiveMode) {
            hideSystemBars(context = context)
        } else {
            showSystemBars(context)
        }
    }

    override fun ReaderScreenViewModel.toggleImmersiveMode(context: Context) {
        immersiveMode = !immersiveMode
        readerUseCases.immersiveModeUseCase.save(immersiveMode)
        if (immersiveMode) {
            hideSystemBars(context = context)
        } else {
            showSystemBars(context)
        }
    }

    override fun ReaderScreenViewModel.toggleSelectableMode() {
        selectableMode = !selectableMode
        readerUseCases.selectedFontStateUseCase.saveSelectableText(selectableMode)
    }

    override fun ReaderScreenViewModel.toggleAutoScrollMode() {
        autoScrollMode = !autoScrollMode
    }

    override fun ReaderScreenViewModel.toggleScrollMode() {
        verticalScrolling = !verticalScrolling
        readerUseCases.scrollModeUseCase.save(verticalScrolling)

    }

    override fun ReaderScreenViewModel.saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = prefState.distanceBetweenParagraphs
        if (isIncreased) {
            distanceBetweenParagraphs = currentDistance + 1
            readerUseCases.paragraphDistanceUseCase.save(distanceBetweenParagraphs)

        } else if (currentDistance > 0) {
            distanceBetweenParagraphs = currentDistance - 1
            readerUseCases.paragraphDistanceUseCase.save(distanceBetweenParagraphs)

        }
    }

    override fun ReaderScreenViewModel.readScrollIndicatorPadding(): Int {
        return readerUseCases.scrollIndicatorUseCase.readPadding()
    }

    override fun ReaderScreenViewModel.readScrollIndicatorWidth(): Int {
        return readerUseCases.scrollIndicatorUseCase.readWidth()
    }

    override fun ReaderScreenViewModel.readBrightness(context: Context) {
        val brightness = readerUseCases.brightnessStateUseCase.readBrightness()
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            if (!autoBrightnessMode) {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                layoutParams.screenBrightness = brightness
                window.attributes = layoutParams
                this.brightness = brightness
            } else {
                val layoutParams: WindowManager.LayoutParams = window.attributes
                showSystemBars(context = context)
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                window.attributes = layoutParams
            }
        }
    }

    override fun ReaderScreenViewModel.readBackgroundColor(): Color {
        val color = Color(readerUseCases.backgroundColorUseCase.read())
        this.backgroundColor = color
        return color
    }

    override fun ReaderScreenViewModel.readTextColor(): Color {
        val textColor = Color(readerUseCases.textColorUseCase.read())
        this.textColor = textColor
        return textColor
    }

    override fun ReaderScreenViewModel.readOrientation(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            when (readerUseCases.orientationUseCase.read()) {
                OrientationMode.Portrait -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    orientation = Orientation.Portrait

                }
                OrientationMode.Landscape -> {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation = Orientation.Landscape
                }
            }
        }
    }

    override fun ReaderScreenViewModel.changeBackgroundColor(colorIndex: Int) {
        val bgColor = readerScreenBackgroundColors[colorIndex].color
        val textColor = readerScreenBackgroundColors[colorIndex].onTextColor
        backgroundColor = bgColor
        this.textColor = textColor
        setReaderBackgroundColor(bgColor)
        setReaderTextColor(textColor)
    }

    override fun ReaderScreenViewModel.setReaderBackgroundColor(color: Color) {
        readerUseCases.backgroundColorUseCase.save(color.toArgb())
    }

    override fun ReaderScreenViewModel.setReaderTextColor(color: Color) {
        readerUseCases.textColorUseCase.save(color.toArgb())
    }

    override fun ReaderScreenViewModel.setAutoScrollIntervalReader(increase: Boolean) {
        if (increase) {
            autoScrollInterval += 500
            readerUseCases.autoScrollMode.saveInterval(autoScrollInterval + 500)
        } else if (autoScrollInterval > 1){
            autoScrollInterval -= 500
            readerUseCases.autoScrollMode.saveInterval(autoScrollInterval - 500)
        }
    }

    override fun ReaderScreenViewModel.setAutoScrollOffsetReader(increase: Boolean) {
        if (increase) {
            autoScrollOffset += 50
            readerUseCases.autoScrollMode.saveOffset(autoScrollOffset + 50)
        } else if (autoScrollOffset > 50) {
            autoScrollOffset -= 50
            readerUseCases.autoScrollMode.saveOffset(autoScrollOffset - 50)
        }
    }

    override fun ReaderScreenViewModel.toggleAutoBrightness() {
        if (autoBrightnessMode) {
            autoBrightnessMode = false
            readerUseCases.brightnessStateUseCase.saveAutoBrightness(false)
        } else {
            autoBrightnessMode = true
            readerUseCases.brightnessStateUseCase.saveAutoBrightness(true)
        }

    }


    override fun ReaderScreenViewModel.toggleRemoteLoaded(loaded: Boolean) {
        state.isRemoteLoaded = loaded
    }

    override fun ReaderScreenViewModel.showSystemBars(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null) {
            val window = activity.window
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE


            // Hide both the status bar and the navigation bar
            //windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun ReaderScreenViewModel.hideSystemBars(context: Context) {
        val activity = context.findComponentActivity()
        if (activity != null && immersiveMode) {
            val window = activity.window
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            // Configure the behavior of the hidden system bars
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            //  windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

}