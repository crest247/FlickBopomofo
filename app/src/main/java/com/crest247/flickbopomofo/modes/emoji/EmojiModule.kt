package com.crest247.flickbopomofo.modes.emoji

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.crest247.flickbopomofo.core.models.KeyboardModule
import com.crest247.flickbopomofo.core.ui.components.KeyContent
import com.crest247.flickbopomofo.core.ui.components.StandardCategoryTabs

object EmojiModule : KeyboardModule {
    override val processor: EmojiProcessor by lazy { EmojiProcessor() }

    @Composable
    override fun SpellingAreaLayout() {
    }

    @Composable
    override fun SuggestionAreaLayout() {
        val tabs = remember {
            EmojiData.categories.map { KeyContent.Text(it.icon) }
        }

        StandardCategoryTabs(
            tabs = tabs,
            selectedIndex = processor.currentCategoryIndex,
            onTabSelected = { index ->
                processor.onAction(EmojiAction.SwitchCategory(index))
            }
        )
    }

    @Composable
    override fun KeyAreaLayout() {
        EmojiKeyLayout(processor = processor)
    }
}