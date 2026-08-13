package com.crest247.flickbopomofo.modes.arrayFlick

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReadMore
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.KeyboardControlKey
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.SelectAll
import com.crest247.flickbopomofo.core.models.FlickKeyData
import com.crest247.flickbopomofo.core.models.FuncType
import com.crest247.flickbopomofo.core.models.KeyBackgroundType
import com.crest247.flickbopomofo.core.models.KeyData
import com.crest247.flickbopomofo.core.models.SystemKeyFactory
import com.crest247.flickbopomofo.core.ui.components.KeyContent
import com.crest247.flickbopomofo.core.ui.components.KeyContent.Icon
import com.crest247.flickbopomofo.modes.shared.array.ArrayAction

object ArrayFlickLayoutProvider {
    fun createKeys(
        editorInfo: EditorInfo?,
        metaCode: Int
    ): List<List<KeyData>> {
        fun bopomofoKey(
            displayTexts: List<String?>,
            lookupTexts: List<String?>
        ): FlickKeyData<*> {
            return FlickKeyData(
                KeyContent.Multi(displayTexts.map { it?.let { KeyContent.Text(it) } }),
                displayTexts.map { it?.let { KeyContent.Text(it) } },
                List(displayTexts.size) {
                    it to ArrayFlickAction.InputRadical(
                        displayTexts[it], lookupTexts[it], it
                    )
                }.toMap()
            )
        }

        val modifierKey = FlickKeyData(
            content = Icon(Icons.AutoMirrored.Outlined.ReadMore),
            popupContents = listOf(
                null,
                KeyContent.Text("⇧"),
                Icon(Icons.Outlined.KeyboardControlKey)
            ),
            directionActions = (0..2).associateWith { ArrayFlickAction.PhysicalModifier(it) },
            weight = 1.0f,
            backgroundType = KeyBackgroundType.FUNCTIONAL
        )

        val dpadKey =
            FlickKeyData(
                content = if (metaCode == KeyEvent.KEYCODE_CTRL_LEFT) Icon(Icons.Outlined.NoteAlt)
                else Icon(Icons.Outlined.ControlCamera),
                popupContents =
                    if (metaCode == KeyEvent.KEYCODE_CTRL_LEFT) listOf(
                        null,
                        Icon(Icons.Outlined.SelectAll),
                        Icon(Icons.Outlined.ContentPaste),
                        Icon(Icons.AutoMirrored.Outlined.Redo),
                        Icon(Icons.AutoMirrored.Outlined.Undo),
                        Icon(Icons.Outlined.ContentCopy)
                    )
                    else listOf(
                        null,
                        Icon(Icons.Default.ArrowUpward),
                        Icon(Icons.AutoMirrored.Filled.ArrowForward),
                        Icon(Icons.Default.ArrowDownward),
                        Icon(Icons.AutoMirrored.Filled.ArrowBack)
                    ),
                directionActions = (0..5).associateWith { ArrayFlickAction.DirectionalPad(it) },
                weight = 1.0f,
                backgroundType = KeyBackgroundType.FUNCTIONAL
            )


        return listOf(
            listOf(
                SystemKeyFactory.create(FuncType.TAB, 1.0f),
                bopomofoKey(listOf("ㄅ", "ㄇ", "ㄈ", null, "ㄆ"), listOf("1", "a", "z", null, "q")),
                bopomofoKey(listOf("ㄉ", "ㄋ", "ㄌ", null, "ㄊ"), listOf("2", "s", "x", null, "w")),
                bopomofoKey(listOf("ㄍ", null, "ㄏ", null, "ㄎ"), listOf("e", null, "c", null, "d")),
                SystemKeyFactory.create(FuncType.BACKSPACE, 1.0f, ArrayAction.Backspace),
            ),
            listOf(
                dpadKey,
                bopomofoKey(listOf("ㄐ", null, "ㄒ", null, "ㄑ"), listOf("r", null, "v", null, "f")),
                bopomofoKey(listOf("ㄓ", null, "ㄕ", "ㄖ", "ㄔ"), listOf("5", null, "g", "b", "t")),
                bopomofoKey(listOf("ㄗ", null, "ㄙ", null, "ㄘ"), listOf("y", null, "n", null, "h")),
                modifierKey
            ),
            listOf(
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, ArrayAction.Space),
                bopomofoKey(listOf("ㄚ", "ㄠ", "ㄢ", "ㄤ", "ㄞ"), listOf("8", "l", "0", ";", "9")),
                bopomofoKey(listOf("ㄛ", "ㄡ", null, null, null), listOf("i", ".", null, null, null)),
                bopomofoKey(listOf("ㄜ", null, "ㄣ", "ㄥ", null), listOf("k", null, "p", "/", null)),
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, ArrayAction.Space),
            ),
            listOf(
                SystemKeyFactory.create(FuncType.LANGUAGE, 1.0f),
                bopomofoKey(listOf("ㄝ", null, null, null, "ㄟ"), listOf(",", null, null, null, "o")),
                bopomofoKey(listOf("ㄩ", "ㄨ", null, null, "ㄧ"), listOf("m", "j", null, null, "u")),
                bopomofoKey(listOf("ㄦ", "ˊ", "ˇ", "ˋ", "˙"), listOf("-", "6", "3", "4", "7")),
                SystemKeyFactory.createEnterKey(editorInfo, 1.0f, ArrayFlickAction.Enter)
            ),
        )
    }
}