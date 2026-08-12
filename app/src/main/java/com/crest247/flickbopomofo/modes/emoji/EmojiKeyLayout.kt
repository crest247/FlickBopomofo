package com.crest247.flickbopomofo.modes.emoji

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.engine.executeDefault
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.StandardKeyboard

@Composable
fun EmojiKeyLayout(processor: EmojiProcessor) {
    val dimens = LocalKeyboardDimens.current
    val state = LocalKeyboardState.current
    val systemProcessor = state.systemProcessor
    val editorInfo = state.currentEditorInfo

    val keyRows = remember(editorInfo, processor.currentCategoryIndex, processor.currentPageIndex) {
        EmojiLayoutProvider.createKeys(
            editorInfo, processor.currentCategoryIndex, processor.currentPageIndex
        )
    }

    StandardKeyboard(
        keyRows = keyRows,
        rowHeight = dimens.emojiKeyHeight
    ) { keyEvent ->
        processor.executeDefault(keyEvent, systemProcessor)
    }
}