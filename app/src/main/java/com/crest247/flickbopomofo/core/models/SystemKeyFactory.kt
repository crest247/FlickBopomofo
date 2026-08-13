package com.crest247.flickbopomofo.core.models

import android.view.inputmethod.EditorInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.outlined.KeyboardReturn
import androidx.compose.material.icons.automirrored.outlined.KeyboardTab
import androidx.compose.material.icons.automirrored.outlined.NextPlan
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.EmojiSymbols
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined._123
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.crest247.flickbopomofo.core.engine.SystemAction
import com.crest247.flickbopomofo.core.ui.components.KeyContent
import com.crest247.flickbopomofo.core.ui.components.KeyContent.Icon

object SystemKeyFactory {
    private val defaultContents = mapOf(
        FuncType.BACKSPACE to KeyContent.Icon(Icons.AutoMirrored.Outlined.Backspace),
        FuncType.DELETE to KeyContent.Icon(Icons.AutoMirrored.Outlined.Backspace, true),
        FuncType.DPAD to KeyContent.Icon(Icons.Outlined.ControlCamera),
        FuncType.SPACE to KeyContent.Text(" "),
        FuncType.ENTER to KeyContent.Icon(Icons.AutoMirrored.Outlined.KeyboardReturn),
        FuncType.SHIFT to KeyContent.Text("⇧"),
        FuncType.LANGUAGE to KeyContent.Icon(Icons.Outlined.Language),
        FuncType.TAB to KeyContent.Icon(Icons.AutoMirrored.Outlined.KeyboardTab)
    )

    fun create(type: FuncType, weight: Float = 1.0f, action: Any? = null): VisibleKeyData<*> {
        val content = defaultContents[type] ?: throw (IllegalArgumentException("Unknown type"))

        return when (type) {
            FuncType.SPACE -> TapKeyData(
                content,
                action ?: SystemAction.Space,
                weight,
                KeyBackgroundType.NORMAL
            )

            FuncType.BACKSPACE -> TapKeyData(
                content,
                action ?: SystemAction.Backspace,
                weight,
                KeyBackgroundType.FUNCTIONAL
            )

            FuncType.ENTER -> TapKeyData(
                content,
                action ?: SystemAction.Enter,
                weight,
                KeyBackgroundType.FUNCTIONAL
            )

            FuncType.TAB -> TapKeyData(
                content,
                action ?: SystemAction.Tab,
                weight,
                KeyBackgroundType.FUNCTIONAL
            )

            FuncType.DELETE -> TapKeyData(
                content,
                action ?: SystemAction.Delete,
                weight,
                KeyBackgroundType.FUNCTIONAL
            )

            FuncType.LANGUAGE -> FlickKeyData(
                content,
                listOf(
                    KeyContent.Text(""),
                    KeyContent.Text("ㄅ"),
                    KeyContent.Icon(Icons.Outlined.Abc),
                    KeyContent.Icon(Icons.Outlined.EmojiSymbols),
                    KeyContent.Icon(Icons.Outlined.EmojiEmotions),
                    KeyContent.Icon(Icons.Outlined._123)
                ),
                mapOf(
                    0 to null,
                    1 to SystemAction.SwitchModule(0),
                    2 to SystemAction.SwitchModule(1),
                    3 to SystemAction.SwitchModule(2),
                    4 to SystemAction.SwitchModule(3),
                    5 to SystemAction.SwitchModule(4),
                ),
                weight,
                KeyBackgroundType.FUNCTIONAL
            )

            FuncType.DPAD -> FlickKeyData(
                content = content,
                popupContents =
                    listOf(
                        null,
                        Icon(Icons.Default.ArrowUpward),
                        Icon(Icons.AutoMirrored.Filled.ArrowForward),
                        Icon(Icons.Default.ArrowDownward),
                        Icon(Icons.AutoMirrored.Filled.ArrowBack)
                    ),
                directionActions = (0..5).associateWith { SystemAction.DirectionalPad(it) },
                weight,
                backgroundType = KeyBackgroundType.FUNCTIONAL
            )

            else -> TapKeyData(
                content,
                action,
                weight,
                KeyBackgroundType.FUNCTIONAL
            )
        }
    }

    fun createEnterKey(
        editorInfo: EditorInfo?,
        weight: Float = 1.5f,
        action: Any? = null
    ): TapKeyData<*> {
        val options = editorInfo?.imeOptions ?: 0
        val noEnterAction = (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        val finalAction =
            if (noEnterAction) EditorInfo.IME_ACTION_NONE else options and EditorInfo.IME_MASK_ACTION

        val content = when (finalAction) {
            EditorInfo.IME_ACTION_SEARCH -> KeyContent.Icon(Icons.Outlined.Search)
            EditorInfo.IME_ACTION_SEND -> KeyContent.Icon(Icons.AutoMirrored.Filled.Send)
            EditorInfo.IME_ACTION_DONE -> KeyContent.Icon(Icons.Outlined.Done)
            EditorInfo.IME_ACTION_GO -> KeyContent.Icon(Icons.Outlined.ArrowCircleRight)
            EditorInfo.IME_ACTION_NEXT -> KeyContent.Icon(Icons.AutoMirrored.Outlined.NextPlan)
            else -> KeyContent.Icon(Icons.AutoMirrored.Outlined.KeyboardReturn)
        }

        return TapKeyData(
            content = content,
            action = action ?: SystemAction.Enter,
            weight = weight,
            backgroundType = KeyBackgroundType.FUNCTIONAL
        )
    }
}