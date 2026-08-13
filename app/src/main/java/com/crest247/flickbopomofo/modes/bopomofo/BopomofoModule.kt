package com.crest247.flickbopomofo.modes.bopomofo

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.models.KeyboardModule

object BopomofoModule : KeyboardModule {
    override val processor: BopomofoProcessor by lazy { BopomofoProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
        BopomofoSpellingLayout(processor = processor)
    }

    @Composable
    override fun SuggestionAreaLayout() {
        BopomofoSuggestionLayout(processor = processor)
    }

    @Composable
    override fun KeyAreaLayout() {
        val state = LocalKeyboardState.current

        if (!state.isPhysicalKeyboardActive)
            BopomofoKeyLayout(processor = processor)
    }
}