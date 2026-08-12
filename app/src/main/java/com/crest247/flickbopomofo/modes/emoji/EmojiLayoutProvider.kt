package com.crest247.flickbopomofo.modes.emoji

import android.view.inputmethod.EditorInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import com.crest247.flickbopomofo.core.models.FlickKeyData
import com.crest247.flickbopomofo.core.models.FuncType
import com.crest247.flickbopomofo.core.models.KeyBackgroundType
import com.crest247.flickbopomofo.core.models.KeyData
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.SystemKeyFactory
import com.crest247.flickbopomofo.core.models.TapKeyData
import com.crest247.flickbopomofo.core.ui.components.KeyContent
import kotlin.math.ceil

object EmojiLayoutProvider {

    fun createKeys(editorInfo: EditorInfo?, categoryIndex: Int, pageIndex: Int): List<List<KeyData>> {
        val currentCategory =
            EmojiData.categories.getOrElse(categoryIndex) { EmojiData.categories.first() }
        val allEmojis = currentCategory.emojis
        val totalEmojis = allEmojis.size
        val totalRows = ceil(totalEmojis.toDouble() / 40.0).toInt() * 4

        val baseSize = totalEmojis / totalRows
        val remainder = totalEmojis % totalRows

        val rowSizes = List(totalRows) { index ->
            if (index < remainder) baseSize + 1 else baseSize
        }

        val allEmojiRows = mutableListOf<List<String>>()
        var currentIdx = 0
        for (size in rowSizes) {
            val endIdx = minOf(currentIdx + size, totalEmojis)
            if (currentIdx < totalEmojis) {
                allEmojiRows.add(allEmojis.subList(currentIdx, endIdx))
            } else {
                allEmojiRows.add(emptyList())
            }
            currentIdx = endIdx
        }

        val startRowIdx = pageIndex * 4
        val endRowIdx = minOf(startRowIdx + 4, allEmojiRows.size)
        val pagedEmojiRows = allEmojiRows.subList(startRowIdx, endRowIdx)

        val emojiRowsData = pagedEmojiRows.map { row ->
            row.map { emoji ->
                if (SkinToneProcessor.hasSkinToneVariants(emoji)) {
                    val variants = SkinToneProcessor.generateVariants(emoji)
                    val popupContents =
                        listOf(KeyContent.Text(emoji)) + variants.map { KeyContent.Text(it) }

                    val directionActions = mutableMapOf<Int, KeyboardAction>()
                    directionActions[0] = EmojiAction.InputEmoji(emoji)
                    variants.forEachIndexed { index, variant ->
                        directionActions[index + 1] = EmojiAction.InputEmoji(variant)
                    }

                    FlickKeyData(
                        content = KeyContent.Text(emoji),
                        popupContents = popupContents,
                        directionActions = directionActions
                    )
                } else {
                    TapKeyData(
                        content = KeyContent.Text(emoji),
                        action = EmojiAction.InputEmoji(emoji)
                    )
                }
            }
        }

        val customBottomRow = listOf(
            SystemKeyFactory.create(FuncType.LANGUAGE, 1.5f),
            SystemKeyFactory.create(FuncType.DPAD, 1.5f),
            TapKeyData(
                content = KeyContent.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft),
                action = EmojiAction.PrevPage,
                weight = 2.0f,
                backgroundType = KeyBackgroundType.FUNCTIONAL
            ),
            TapKeyData(
                content = KeyContent.Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight),
                action = EmojiAction.NextPage,
                weight = 2.0f,
                backgroundType = KeyBackgroundType.FUNCTIONAL
            ),
            SystemKeyFactory.create(FuncType.BACKSPACE, 1.5f),
            SystemKeyFactory.createEnterKey(editorInfo, 1.5f)
        )

        return emojiRowsData + listOf(customBottomRow)
    }
}