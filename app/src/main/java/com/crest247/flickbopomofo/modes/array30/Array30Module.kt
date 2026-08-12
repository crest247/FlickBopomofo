package com.crest247.flickbopomofo.modes.array30

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.models.KeyboardModule
import com.crest247.flickbopomofo.modes.shared.array.ArrayProcessor
import com.crest247.flickbopomofo.modes.shared.array.ArraySpellingLayout
import com.crest247.flickbopomofo.modes.shared.array.ArraySuggestionLayout

object Array30Module : KeyboardModule {
    override val processor: ArrayProcessor by lazy { ArrayProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
        ArraySpellingLayout(processor = processor)
    }

    @Composable
    override fun SuggestionAreaLayout() {
        ArraySuggestionLayout(processor = processor)
    }

    @Composable
    override fun KeyAreaLayout() {
        val state = LocalKeyboardState.current

        if (!state.isPhysicalKeyboardActive)
            Array30KeyLayout(processor = processor)
    }
}