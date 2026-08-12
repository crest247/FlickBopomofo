package com.crest247.flickbopomofo.modes.emoji

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.crest247.flickbopomofo.core.engine.InputProcessor
import com.crest247.flickbopomofo.core.models.KeyboardAction
import kotlin.math.ceil

class EmojiProcessor : InputProcessor {
    private var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null; private set
    var currentCategoryIndex by mutableIntStateOf(0); private set
    var currentPageIndex by mutableIntStateOf(0); private set

    override fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.inputConnection = inputConnection
        this.editorInfo = editorInfo
    }

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is EmojiAction) return action

        when (action) {
            is EmojiAction.SwitchCategory -> {
                currentCategoryIndex = action.index
                currentPageIndex = 0
            }

            is EmojiAction.PrevPage -> {
                if (currentPageIndex > 0)
                    currentPageIndex--
            }

            is EmojiAction.NextPage -> {
                val totalEmojis =
                    EmojiData.categories.getOrNull(currentCategoryIndex)?.emojis?.size ?: 0
                val maxPages = ceil(totalEmojis.toDouble() / 40.0).toInt()
                if (currentPageIndex < maxPages - 1)
                    currentPageIndex++
            }

            is EmojiAction.InputEmoji -> {
                inputConnection?.commitText(action.emoji, 1)
            }
        }
        return null
    }
}