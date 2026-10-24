package com.crest247.flickbopomofo.modes.bopomofo

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

object BopomofoLayoutProvider {
    fun createKeys(
        editorInfo: EditorInfo?,
        metaCode: Int
    ): List<List<KeyData>> {
        fun bopomofoKey(radicals: List<String?>): FlickKeyData<*> {
            return FlickKeyData(
                KeyContent.Multi(radicals.map { it?.let { KeyContent.Text(it) } }),
                radicals.map { it?.let { KeyContent.Text(it) } },
                List(radicals.size) {
                    it to BopomofoAction.InputRadical(radicals[it], it)
                }.toMap()
            )
        }

        fun flickKey(centerText: String, popupTexts: List<String>): FlickKeyData<*> {
            return FlickKeyData(
                KeyContent.Text(centerText),
                List(popupTexts.size + 1) { index ->
                    if (index == 0) KeyContent.Text("")
                    else KeyContent.Text(popupTexts[index - 1])
                },
                List(popupTexts.size + 1) { index ->
                    if (index == 0) 0 to BopomofoAction.InputSymbolClick(centerText)
                    else index to BopomofoAction.InputSymbolUp(popupTexts[index - 1])
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
            directionActions = (0..2).associateWith { BopomofoAction.PhysicalModifier(it) },
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
                directionActions = (0..5).associateWith { BopomofoAction.DirectionalPad(it) },
                weight = 1.0f,
                backgroundType = KeyBackgroundType.FUNCTIONAL
            )

        fun leftSpaceKey(): FlickKeyData<*> {
            val popupTexts = listOf("「", "」", "）", "（", "』", "『")
            return FlickKeyData(
                KeyContent.Text(" "),
                List(popupTexts.size + 1) { index ->
                    if (index == 0) KeyContent.Text("")
                    else KeyContent.Text(popupTexts[index - 1])
                },
                List(popupTexts.size + 1) { index ->
                    if (index == 0) 0 to BopomofoAction.Space
                    else index to BopomofoAction.InputSymbolUp(popupTexts[index - 1])
                }.toMap()
            )
        }

        return listOf(
            listOf(
                SystemKeyFactory.create(FuncType.TAB, 1.0f),
                bopomofoKey(listOf("ㄅ", "ㄇ", "ㄈ", null, "ㄆ")),
                bopomofoKey(listOf("ㄉ", "ㄋ", "ㄌ", null, "ㄊ")),
                bopomofoKey(listOf("ㄍ", "ㄏ", null, null, "ㄎ")),
                SystemKeyFactory.create(FuncType.BACKSPACE, 1.0f, BopomofoAction.Backspace),
            ),
            listOf(
                modifierKey,
                bopomofoKey(listOf("ㄐ", "ㄒ", null, null, "ㄑ")),
                bopomofoKey(listOf("ㄓ", "ㄕ", "ㄖ", null, "ㄔ")),
                bopomofoKey(listOf("ㄗ", "ㄙ", null, null, "ㄘ")),
                dpadKey
            ),
            listOf(
                leftSpaceKey(),
                bopomofoKey(listOf("ㄚ", "ㄜ", "ㄝ", null, "ㄛ")),
                bopomofoKey(listOf("ㄞ", "ㄠ", "ㄡ", null, "ㄟ")),
                bopomofoKey(listOf("ㄢ", "ㄤ", "ㄥ", "ㄦ", "ㄣ")),
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, BopomofoAction.Space),
            ),
            listOf(
                SystemKeyFactory.create(FuncType.LANGUAGE, 1.0f),
                flickKey(
                    "，",
                    listOf("、", "。", "：", "？", "！", "；")
                ),
                bopomofoKey(listOf("ㄧ", "ㄩ", null, null, "ㄨ")),
                bopomofoKey(listOf("ˉ", "ˊ", "ˇ", "ˋ", "˙")),
                SystemKeyFactory.createEnterKey(editorInfo, 1.0f, BopomofoAction.Enter)
            ),
        )
    }
}