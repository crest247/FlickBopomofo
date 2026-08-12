package com.crest247.flickbopomofo.modes.english

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.engine.executeDefault
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.StandardKeyboard
import com.crest247.flickbopomofo.core.ui.preview.KeyboardPreviewWrapper
import com.crest247.flickbopomofo.core.ui.preview.ThemePreviews

@Composable
fun EnglishKeyLayout(processor: EnglishProcessor) {
    val dimens = LocalKeyboardDimens.current
    val state = LocalKeyboardState.current
    val systemProcessor = state.systemProcessor
    val editorInfo = state.currentEditorInfo
    val shiftState = processor.shiftState
    val variant = processor.variant

    val keyRows = remember(shiftState, variant, editorInfo) {
        EnglishLayoutProvider.createKeys(shiftState, variant, editorInfo)
    }
    StandardKeyboard(
        keyRows = keyRows,
        rowHeight = dimens.englishKeyHeight
    ) { keyEvent ->
        processor.executeDefault(keyEvent, systemProcessor)
    }
}

@ThemePreviews
@Composable
fun PreviewKeyLayout() {
    KeyboardPreviewWrapper {
        EnglishKeyLayout(processor = EnglishProcessor())
    }
}