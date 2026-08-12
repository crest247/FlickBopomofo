package com.crest247.flickbopomofo.modes.number

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.crest247.flickbopomofo.core.engine.InputProcessor
import com.crest247.flickbopomofo.core.models.KeyboardAction

class NumberProcessor : InputProcessor {
    private var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null; private set

    override fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.inputConnection = inputConnection
        this.editorInfo = editorInfo
    }

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is NumberAction) return action

        when (action) {
            is NumberAction.InputChar ->
                inputConnection?.commitText(action.char, 1)
            is NumberAction.InputSymbolUp ->
                inputConnection?.commitText(action.symbol, 1)
            is NumberAction.InputSymbolClick ->
                inputConnection?.commitText(action.symbol, 1)
        }
        return null
    }
}