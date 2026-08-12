package com.crest247.flickbopomofo.modes.array30

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.engine.executeDefault
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.StandardKeyboard
import com.crest247.flickbopomofo.core.ui.preview.KeyboardPreviewWrapper
import com.crest247.flickbopomofo.core.ui.preview.ThemePreviews
import com.crest247.flickbopomofo.modes.shared.array.ArrayProcessor

@Composable
fun Array30KeyLayout(processor: ArrayProcessor) {
    val dimens = LocalKeyboardDimens.current
    val state = LocalKeyboardState.current
    val systemProcessor = state.systemProcessor
    val editorInfo = state.currentEditorInfo

    val keyRows = remember(editorInfo) {
        Array30LayoutProvider.createKeys(editorInfo)
    }
    StandardKeyboard(
        keyRows = keyRows,
        rowHeight = dimens.array30KeyHeight
    ) { keyEvent ->
        processor.executeDefault(keyEvent, systemProcessor)
    }
}

@ThemePreviews
@Composable
fun PreviewKeyLayout() {
    KeyboardPreviewWrapper {
        Array30KeyLayout(processor = ArrayProcessor())
    }
}