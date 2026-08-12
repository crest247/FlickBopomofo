package com.crest247.flickbopomofo.modes.symbol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.engine.executeDefault
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.StandardKeyboard

@Composable
fun SymbolKeyLayout(processor: SymbolProcessor) {
    val dimens = LocalKeyboardDimens.current
    val state = LocalKeyboardState.current
    val systemProcessor = state.systemProcessor
    val editorInfo = state.currentEditorInfo

    val keyRows = remember(editorInfo, processor.currentCategoryIndex) {
        SymbolLayoutProvider.createKeys(editorInfo, processor.currentCategoryIndex)
    }

    StandardKeyboard(
        keyRows = keyRows,
        rowHeight = dimens.symbolKeyHeight
    ) { keyEvent ->
        processor.executeDefault(keyEvent, systemProcessor)
    }
}