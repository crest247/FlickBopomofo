package com.crest247.flickbopomofo.modes.english

import android.view.inputmethod.EditorInfo
import com.crest247.flickbopomofo.core.models.FuncType
import com.crest247.flickbopomofo.core.models.KeyData
import com.crest247.flickbopomofo.core.models.SpacerData
import com.crest247.flickbopomofo.core.models.SystemKeyFactory
import com.crest247.flickbopomofo.core.models.TapKeyData

enum class EnglishVariant { Default, Email, Url }

object EnglishLayoutProvider {

    fun createKeys(
        shiftState: ShiftState,
        variant: EnglishVariant,
        editorInfo: EditorInfo?
    ): List<List<KeyData>> {
        val isUpper = shiftState != ShiftState.LOWERCASE
        fun charKey(lower: String, upper: String): TapKeyData<EnglishAction> {
            val text = if (isUpper) upper else lower
            return TapKeyData(
                text = text,
                action = EnglishAction.InputChar(text)
            )
        }

        val variantKey = when (variant) {
            EnglishVariant.Email -> charKey("@", "@")
            EnglishVariant.Url -> charKey("/", "/")
            EnglishVariant.Default -> charKey(",", "<")
        }

        return listOf(
            listOf(
                charKey("1", "!"), charKey("2", "@"), charKey("3", "#"),
                charKey("4", "$"), charKey("5", "%"), charKey("6", "^"),
                charKey("7", "&"), charKey("8", "*"), charKey("9", "("),
                charKey("0", ")")
            ),
            listOf(
                charKey("q", "Q"), charKey("w", "W"), charKey("e", "E"),
                charKey("r", "R"), charKey("t", "T"), charKey("y", "Y"),
                charKey("u", "U"), charKey("i", "I"), charKey("o", "O"),
                charKey("p", "P")
            ),
            listOf(
                SpacerData(0.5f),
                charKey("a", "A"), charKey("s", "S"), charKey("d", "D"),
                charKey("f", "F"), charKey("g", "G"), charKey("h", "H"),
                charKey("j", "J"), charKey("k", "K"), charKey("l", "L"),
                SpacerData(0.5f)
            ),
            listOf(
                SystemKeyFactory.create(FuncType.SHIFT, 1.5f, EnglishAction.ToggleShift),
                charKey("z", "Z"), charKey("x", "X"), charKey("c", "C"),
                charKey("v", "V"), charKey("b", "B"), charKey("n", "N"),
                charKey("m", "M"),
                SystemKeyFactory.create(FuncType.BACKSPACE, 1.5f)
            ),
            listOf(
                SystemKeyFactory.create(FuncType.LANGUAGE, 1.5f),
                variantKey,
                SystemKeyFactory.create(FuncType.SPACE, 5.0f),
                charKey(".", ">"),
                SystemKeyFactory.createEnterKey(editorInfo, 1.5f)
            )
        )
    }
}