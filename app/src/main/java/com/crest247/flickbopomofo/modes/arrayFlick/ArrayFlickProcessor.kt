package com.crest247.flickbopomofo.modes.arrayFlick

import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.crest247.flickbopomofo.core.engine.SystemAction
import com.crest247.flickbopomofo.core.extension.sendDownUpKeyEvents
import com.crest247.flickbopomofo.core.extension.sendKeyDownEvent
import com.crest247.flickbopomofo.core.extension.sendKeyUpEvent
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.modes.shared.array.ArrayAction
import com.crest247.flickbopomofo.modes.shared.array.ArrayProcessor

class ArrayFlickProcessor : ArrayProcessor() {
    var metaCode by mutableIntStateOf(0)

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        when (action) {
            is ArrayFlickAction.InputRadicalUp ->
                super.onAction(
                    ArrayAction.InputRadical(
                        displayStr = action.displayStr,
                        lookupStr = action.lookupStr
                    )
                )

            is ArrayFlickAction.InputRadicalLongPress ->
                if (lookupTokens.size == 1 && lookupTokens[0] == "w" && action.direction == 0)
                    super.onAction(
                        ArrayAction.InputRadical(
                            displayStr = action.longPressStr,
                            lookupStr = action.longPressStr
                        )
                    )

            is ArrayFlickAction.DirectionalPad -> {
                if (metaCode == KeyEvent.KEYCODE_CTRL_LEFT)
                    when (action.direction) {
                        1 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON
                        )

                        2 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON
                        )

                        3 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_Y, KeyEvent.META_CTRL_ON
                        )

                        4 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON
                        )

                        5 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON
                        )
                    }
                else when (action.direction) {
                    1 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP, 0)
                    2 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT, 0)
                    3 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN, 0)
                    4 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                }
            }

            is ArrayFlickAction.PhysicalModifierFlick -> {
                val newMetaCode = when (action.direction) {
                    1 -> KeyEvent.KEYCODE_SHIFT_LEFT
                    2 -> KeyEvent.KEYCODE_CTRL_LEFT
                    else -> 0
                }
                if (metaCode != newMetaCode) {
                    inputConnection?.sendKeyUpEvent(metaCode, 0)
                    inputConnection?.sendKeyDownEvent(newMetaCode, 0)
                    metaCode = newMetaCode
                }
            }

            is ArrayFlickAction.PhysicalModifierUp -> {
                inputConnection?.sendKeyUpEvent(metaCode, 0)
                metaCode = 0
            }

            is ArrayFlickAction.Enter -> {
                if (candidates.isNotEmpty()) commitCandidate(candidates.first())
                else if (displayTokens.isEmpty())
                    when (metaCode) {
                        KeyEvent.KEYCODE_CTRL_LEFT -> {
                            inputConnection?.sendDownUpKeyEvents(
                                KeyEvent.KEYCODE_ENTER, KeyEvent.META_CTRL_ON
                            )
                        }

                        KeyEvent.KEYCODE_SHIFT_LEFT ->
                            inputConnection?.sendDownUpKeyEvents(
                                KeyEvent.KEYCODE_ENTER, KeyEvent.META_SHIFT_ON
                            )

                        else -> return SystemAction.Enter
                    }
            }

            else -> return super.onAction(action)
        }
        return null
    }
}