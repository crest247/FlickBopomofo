package com.crest247.flickbopomofo.core.engine

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.crest247.flickbopomofo.core.state.KeyboardState
import com.crest247.flickbopomofo.core.extension.sendDownUpKeyEvents
import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.Repeatable
import com.crest247.flickbopomofo.core.models.UpTriggerable

sealed interface SystemAction : KeyboardAction {
    object Backspace : SystemAction, Clickable, Repeatable
    object Space : SystemAction, UpTriggerable
    object Enter : SystemAction, UpTriggerable
    object Tab : SystemAction, UpTriggerable
    object Delete : SystemAction, Clickable, Repeatable
    data class DirectionalPad(val direction: Int) : SystemAction, UpTriggerable
    data class SwitchModule(val moduleId: Int) : SystemAction, UpTriggerable
}

class SystemProcessor(
    private val state: KeyboardState
) : InputProcessor {

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is SystemAction) return action

        val inputConnection = state.currentInputConnection
        val editorInfo = state.currentEditorInfo

        when (action) {
            is SystemAction.Backspace ->
                inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)

            is SystemAction.Space ->
                inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_SPACE)

            is SystemAction.Tab ->
                inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB)

            is SystemAction.Delete ->
                inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_FORWARD_DEL)

            is SystemAction.Enter -> {
                val options = editorInfo?.imeOptions ?: 0
                val noEnterAction = (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
                val actionId =
                    if (noEnterAction) EditorInfo.IME_ACTION_NONE else options and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE)
                    inputConnection?.performEditorAction(actionId)
                else
                    inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            }

            is SystemAction.SwitchModule ->
                state.switchModule(state.availableModules[action.moduleId])

            is SystemAction.DirectionalPad ->
                when (action.direction) {
                    1 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP, 0)
                    2 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT, 0)
                    3 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN, 0)
                    4 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                }
        }
        return null
    }

    override fun onHardwareKeyDown(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_SPACE && event.isCtrlPressed) {
            state.switchToNextModule()
            return true
        }
        return false
    }
}