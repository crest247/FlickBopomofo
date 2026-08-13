package com.crest247.flickbopomofo.service

import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.Region
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.crest247.flickbopomofo.core.engine.HardwareKeyRouter
import com.crest247.flickbopomofo.core.state.KeyboardState
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.theme.KeyboardTheme
import com.crest247.flickbopomofo.core.ui.areas.MainKeyboardContainer
import com.crest247.flickbopomofo.core.ui.components.LocalPreviewHandler
import com.crest247.flickbopomofo.core.ui.components.PreviewHandler
import com.crest247.flickbopomofo.modes.bopomofo.BopomofoDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainInputMethodService : ComposedInputMethodService() {
    private lateinit var keyboardState: KeyboardState
    private lateinit var hardwareKeyRouter: HardwareKeyRouter
    private lateinit var composeView: ComposeView
    private var keyboardBounds: Rect? = null
    private var radicalBounds: Rect? = null

    init {
        CoroutineScope(Dispatchers.Main).launch {
            BopomofoDecoder.loadData(this@MainInputMethodService)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val config = resources.configuration
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isTablet = config.smallestScreenWidthDp >= 600
        keyboardState = KeyboardState().apply {
            updateScreenConfig(isLandscape, isTablet, isInitial = true)
        }
        hardwareKeyRouter = HardwareKeyRouter(keyboardState)
    }

    override fun onCreateInputComposeView(): AbstractComposeView {
        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        composeView.setContent {
            val previewHandler = remember { PreviewHandler() }
            CompositionLocalProvider(
                LocalPreviewHandler provides previewHandler,
                LocalKeyboardState provides keyboardState
            ) {
                KeyboardTheme {
                    MainKeyboardContainer(
                        state = keyboardState,
                        onKeyboardLayoutChanged = { updateKeyboardLayout(it) },
                        onRadicalLayoutChanged = { updateRadicalLayout(it) }
                    )
                }
            }
        }

        return composeView
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        val isHardwareHidden =
            resources.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES
        if (isHardwareHidden)
            keyboardState.isPhysicalKeyboardActive = false

        currentInputConnection?.let { inputConnection ->
            keyboardState.updateConnection(inputConnection, info)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
            keyboardState.isPhysicalKeyboardActive = false

        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isTablet = newConfig.smallestScreenWidthDp >= 600
        keyboardState.updateScreenConfig(isLandscape, isTablet, isInitial = false)
    }

    private fun updateKeyboardLayout(coordinates: LayoutCoordinates) {
        val position = coordinates.positionInWindow()
        val size = coordinates.size
        val newBounds = Rect(
            position.x.toInt(),
            position.y.toInt(),
            (position.x + size.width).toInt(),
            (position.y + size.height).toInt()
        )
        if (keyboardBounds != newBounds) {
            keyboardBounds = newBounds
            window?.window?.decorView?.requestLayout()
        }
    }

    private fun updateRadicalLayout(coordinates: LayoutCoordinates?) {
        val newBounds = coordinates?.let {
            val position = it.positionInWindow()
            val size = it.size
            Rect(
                position.x.toInt(),
                position.y.toInt(),
                (position.x + size.width).toInt(),
                (position.y + size.height).toInt()
            )
        }
        if (radicalBounds != newBounds) {
            radicalBounds = newBounds
            window?.window?.decorView?.requestLayout()
        }
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val bounds = keyboardBounds ?: return

        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
        outInsets.touchableRegion.set(bounds)

        radicalBounds?.let {
            outInsets.touchableRegion.op(it, Region.Op.UNION)
        }

        outInsets.contentTopInsets = bounds.top
        outInsets.visibleTopInsets = bounds.top
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (hardwareKeyRouter.dispatchKeyDown(event)) true
        else super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (hardwareKeyRouter.dispatchKeyUp(event)) true
        else super.onKeyUp(keyCode, event)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::composeView.isInitialized) {
            composeView.disposeComposition()
        }
    }
}
