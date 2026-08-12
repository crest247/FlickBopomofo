package com.crest247.flickbopomofo.modes.arrayFlick

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.models.KeyboardModule
import com.crest247.flickbopomofo.modes.shared.array.ArraySpellingLayout
import com.crest247.flickbopomofo.modes.shared.array.ArraySuggestionLayout

object ArrayFlickModule : KeyboardModule {
    override val processor: ArrayFlickProcessor by lazy { ArrayFlickProcessor() }

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
            ArrayFlickKeyLayout(processor = processor)
    }
}