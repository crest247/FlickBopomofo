package com.crest247.flickbopomofo.modes.number

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.models.KeyboardModule

object NumberModule : KeyboardModule {
    override val processor: NumberProcessor by lazy { NumberProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
    }

    @Composable
    override fun SuggestionAreaLayout() {
    }

    @Composable
    override fun KeyAreaLayout() {
        NumberKeyLayout(processor = processor)
    }
}