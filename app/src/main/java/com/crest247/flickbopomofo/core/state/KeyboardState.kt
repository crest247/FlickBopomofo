package com.crest247.flickbopomofo.core.state

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crest247.flickbopomofo.core.engine.SystemProcessor
import com.crest247.flickbopomofo.core.models.KeyboardModule
import com.crest247.flickbopomofo.modes.bopomofo.BopomofoModule
import com.crest247.flickbopomofo.modes.emoji.EmojiModule
import com.crest247.flickbopomofo.modes.english.EnglishModule
import com.crest247.flickbopomofo.modes.number.NumberModule
import com.crest247.flickbopomofo.modes.symbol.SymbolModule

class KeyboardState {
    val availableModules: List<KeyboardModule> = listOf(
        BopomofoModule,
        EnglishModule,
        SymbolModule,
        EmojiModule,
        NumberModule
    )
    var currentModule by mutableStateOf(BopomofoModule as KeyboardModule); private set
    var currentInputConnection by mutableStateOf<InputConnection?>(null); private set
    var currentEditorInfo by mutableStateOf<EditorInfo?>(null); private set
    val systemProcessor = SystemProcessor(this)
    var isPhysicalKeyboardActive by mutableStateOf(false)

    var isLandscape by mutableStateOf(false); private set
    var isTablet by mutableStateOf(false); private set

    fun updateScreenConfig(isLandscape: Boolean, isTablet: Boolean, isInitial: Boolean = false) {
        this.isLandscape = isLandscape
        this.isTablet = isTablet
        if (isInitial)
            currentModule = BopomofoModule
    }

    fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.currentInputConnection = inputConnection
        this.currentEditorInfo = editorInfo
        currentModule.processor.resetStates()
        currentModule.processor.updateConnection(inputConnection, editorInfo)
    }

    fun switchModule(module: KeyboardModule) {
        currentModule = module
        this.updateConnection(
            currentInputConnection ?: return,
            currentEditorInfo ?: return
        )
    }

    fun switchToNextModule() {
        when (currentModule) {
            BopomofoModule -> EnglishModule
            else -> BopomofoModule
        }.let { nextModule ->
            switchModule(nextModule)
        }
    }
}

val LocalKeyboardState = compositionLocalOf<KeyboardState> {
    error("No KeyboardState provided")
}