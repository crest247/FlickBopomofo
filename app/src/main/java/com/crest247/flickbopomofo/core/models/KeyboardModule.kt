package com.crest247.flickbopomofo.core.models

import androidx.compose.runtime.Composable
import com.crest247.flickbopomofo.core.engine.InputProcessor

interface KeyboardModule {
    val processor: InputProcessor

    @Composable
    fun SpellingAreaLayout()
    @Composable
    fun SuggestionAreaLayout()
    @Composable
    fun KeyAreaLayout()
}