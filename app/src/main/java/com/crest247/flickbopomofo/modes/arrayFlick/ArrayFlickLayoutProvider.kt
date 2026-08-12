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
        fun radicalKey(
            centerText: String,
            displayTexts: List<String>,
            lookupTexts: List<String>
        ): FlickKeyData<*> {
            return FlickKeyData(
                KeyContent.Text(centerText),
                List(displayTexts.size) { KeyContent.Text(displayTexts[it]) },
                List(displayTexts.size) {
                    it to ArrayFlickAction.InputRadical(
                        displayTexts[it], lookupTexts[it],
                        it, centerText
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
                radicalKey("1", listOf("1-", "1^", "1v"), listOf("a", "q", "z")),
                radicalKey("2", listOf("2-", "2^", "2v"), listOf("s", "w", "x")),
                radicalKey("3", listOf("3-", "3^", "3v"), listOf("d", "e", "c")),
                SystemKeyFactory.create(FuncType.BACKSPACE, 1.0f, ArrayAction.Backspace),
            ),
            listOf(
                SystemKeyFactory.create(FuncType.DELETE, 1.0f),
                radicalKey("4", listOf("4-", "4^", "4v"), listOf("f", "r", "v")),
                radicalKey("5", listOf("5-", "5^", "5v"), listOf("g", "t", "b")),
                radicalKey("6", listOf("6-", "6^", "6v"), listOf("h", "y", "n")),
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, ArrayAction.Space),
            ),
            listOf(
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, ArrayAction.Space),
                radicalKey("7", listOf("7-", "7^", "7v"), listOf("j", "u", "m")),
                radicalKey("8", listOf("8-", "8^", "8v"), listOf("k", "i", ",")),
                radicalKey("9", listOf("9-", "9^", "9v"), listOf("l", "o", ".")),
                SystemKeyFactory.create(FuncType.SPACE, 1.0f, ArrayAction.Space),
            ),
            listOf(
                SystemKeyFactory.create(FuncType.LANGUAGE, 1.0f),
                modifierKey,
                radicalKey("0", listOf("0-", "0^", "0v"), listOf(";", "p", "/")),
                dpadKey,
                SystemKeyFactory.createEnterKey(editorInfo, 1.0f, ArrayFlickAction.Enter)
            ),
        )
    }
}