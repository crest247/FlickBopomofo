package com.crest247.flickbopomofo.core.engine

import android.view.KeyEvent
import com.crest247.flickbopomofo.core.state.KeyboardState
import com.crest247.flickbopomofo.modes.bopomofo.BopomofoModule
import com.crest247.flickbopomofo.modes.english.EnglishModule

class HardwareKeyRouter(
    private val keyboardState: KeyboardState,
) {
    val systemProcessor = keyboardState.systemProcessor
    private val consumedKeyCodes = mutableSetOf<Int>()

    fun dispatchKeyDown(event: KeyEvent): Boolean {
        when (keyboardState.currentModule) {
            EnglishModule, BopomofoModule -> {}
            else -> keyboardState.switchModule(EnglishModule)
        }
        val keyCode = event.keyCode
        keyboardState.isPhysicalKeyboardActive =
            keyCode != KeyEvent.KEYCODE_BACK
                    && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                    && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                    && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
        val handled = keyboardState.currentModule.processor.onHardwareKeyDown(event) ||
                systemProcessor.onHardwareKeyDown(event)
        if (handled)
            consumedKeyCodes.add(keyCode)
        return handled
    }

    fun dispatchKeyUp(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val wasConsumedOnDown = consumedKeyCodes.remove(keyCode)
        val handledByProcessor = keyboardState.currentModule.processor.onHardwareKeyUp(event) ||
                systemProcessor.onHardwareKeyUp(event)
        return wasConsumedOnDown || handledByProcessor
    }
}