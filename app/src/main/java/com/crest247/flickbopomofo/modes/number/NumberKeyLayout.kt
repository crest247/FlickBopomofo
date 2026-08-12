package com.crest247.flickbopomofo.modes.number

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.engine.executeDefault
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.StandardKeyboard
import com.crest247.flickbopomofo.core.ui.preview.KeyboardPreviewWrapper
import com.crest247.flickbopomofo.core.ui.preview.ThemePreviews

@Composable
fun NumberKeyLayout(processor: NumberProcessor) {
    val dimens = LocalKeyboardDimens.current
    val state = LocalKeyboardState.current
    val systemProcessor = state.systemProcessor
    val editorInfo = state.currentEditorInfo

    val keyRows = remember(editorInfo) {
        NumberLayoutProvider.createKeys(editorInfo)
    }
    StandardKeyboard(
        keyRows = keyRows,
        rowHeight = dimens.numberKeyHeight
    ) { keyEvent ->
        processor.executeDefault(keyEvent, systemProcessor)
    }
}

@ThemePreviews
@Composable
fun PreviewKeyLayout() {
    KeyboardPreviewWrapper {
        NumberKeyLayout(processor = NumberProcessor())
    }
}