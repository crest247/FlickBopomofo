package com.crest247.flickbopomofo.modes.english

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crest247.flickbopomofo.core.engine.InputProcessor
import com.crest247.flickbopomofo.core.models.KeyboardAction

class EnglishProcessor : InputProcessor {
    private var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null; private set
    var shiftState: ShiftState by mutableStateOf(ShiftState.LOWERCASE); private set
    var variant: EnglishVariant by mutableStateOf(EnglishVariant.Default)
    private var isShiftPressed = false
    private var hasTypedDuringHold = false

    override fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.inputConnection = inputConnection
        this.editorInfo = editorInfo

        val inputClass = editorInfo.inputType and InputType.TYPE_MASK_CLASS
        val inputVariation = editorInfo.inputType and InputType.TYPE_MASK_VARIATION

        variant = if (inputClass == InputType.TYPE_CLASS_TEXT) {
            when (inputVariation) {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> EnglishVariant.Email
                InputType.TYPE_TEXT_VARIATION_URI -> EnglishVariant.Url
                else -> EnglishVariant.Default
            }
        } else
            EnglishVariant.Default
    }

    override fun resetStates() {
        shiftState = ShiftState.LOWERCASE
        isShiftPressed = false
        hasTypedDuringHold = false
    }

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is EnglishAction) return action

        when (action) {
            is EnglishAction.InputChar -> {
                if (isShiftPressed)
                    hasTypedDuringHold = true

                inputConnection?.commitText(action.char, 1)

                if (!isShiftPressed && shiftState == ShiftState.UPPERCASE)
                    shiftState = ShiftState.LOWERCASE
            }

            is EnglishAction.ToggleShiftDown -> {
                isShiftPressed = true
                hasTypedDuringHold = false
                shiftState = when (shiftState) {
                    ShiftState.LOWERCASE -> ShiftState.UPPERCASE
                    ShiftState.UPPERCASE -> ShiftState.CAPS_LOCK
                    ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
                }
            }

            is EnglishAction.ToggleShiftUp -> {
                isShiftPressed = false
                if (hasTypedDuringHold && shiftState == ShiftState.UPPERCASE)
                    shiftState = ShiftState.LOWERCASE
            }
        }
        return null
    }
}