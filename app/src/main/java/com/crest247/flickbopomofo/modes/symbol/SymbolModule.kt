package com.crest247.flickbopomofo.modes.symbol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.models.KeyboardModule
import com.crest247.flickbopomofo.core.ui.components.KeyContent
import com.crest247.flickbopomofo.core.ui.components.StandardCategoryTabs

object SymbolModule : KeyboardModule {
    override val processor: SymbolProcessor by lazy { SymbolProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
    }

    @Composable
    override fun SuggestionAreaLayout() {
        val tabs = remember {
            SymbolLayoutProvider.categories.map { KeyContent.Text(it) }
        }

        StandardCategoryTabs(
            tabs = tabs,
            selectedIndex = processor.currentCategoryIndex,
            onTabSelected = { index ->
                processor.onAction(SymbolAction.SwitchCategory(index))
            }
        )
    }

    @Composable
    override fun KeyAreaLayout() {
        SymbolKeyLayout(processor = processor)
    }
}