package com.crest247.flickbopomofo.modes.english

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.state.LocalKeyboardState
import com.crest247.flickbopomofo.core.models.KeyboardModule

object EnglishModule : KeyboardModule {
    override val processor: EnglishProcessor by lazy { EnglishProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
    }

    @Composable
    override fun SuggestionAreaLayout() {
    }

    @Composable
    override fun KeyAreaLayout() {
        val state = LocalKeyboardState.current

        if (!state.isPhysicalKeyboardActive)
            EnglishKeyLayout(processor = processor)
    }
}