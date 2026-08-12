package com.crest247.flickbopomofo.modes.array30

import android.view.inputmethod.EditorInfo
import com.crest247.flickbopomofo.core.models.FuncType
import com.crest247.flickbopomofo.core.models.KeyData
import com.crest247.flickbopomofo.core.models.SystemKeyFactory
import com.crest247.flickbopomofo.core.models.TapKeyData
import com.crest247.flickbopomofo.modes.shared.array.ArrayAction

object Array30LayoutProvider {
    fun createKeys(editorInfo: EditorInfo?): List<List<KeyData>> {
        return listOf(
            listOf(
                TapKeyData<ArrayAction>(
                    text = "1",
                    action = ArrayAction.InputRadical(displayStr = "1", lookupStr = "1")
                ),
                TapKeyData<ArrayAction>(
                    text = "2",
                    action = ArrayAction.InputRadical(displayStr = "2", lookupStr = "2")
                ),
                TapKeyData<ArrayAction>(
                    text = "3",
                    action = ArrayAction.InputRadical(displayStr = "3", lookupStr = "3")
                ),
                TapKeyData<ArrayAction>(
                    text = "4",
                    action = ArrayAction.InputRadical(displayStr = "4", lookupStr = "4")
                ),
                TapKeyData<ArrayAction>(
                    text = "5",
                    action = ArrayAction.InputRadical(displayStr = "5", lookupStr = "5")
                ),
                TapKeyData<ArrayAction>(
                    text = "6",
                    action = ArrayAction.InputRadical(displayStr = "6", lookupStr = "6")
                ),
                TapKeyData<ArrayAction>(
                    text = "7",
                    action = ArrayAction.InputRadical(displayStr = "7", lookupStr = "7")
                ),
                TapKeyData<ArrayAction>(
                    text = "8",
                    action = ArrayAction.InputRadical(displayStr = "8", lookupStr = "8")
                ),
                TapKeyData<ArrayAction>(
                    text = "9",
                    action = ArrayAction.InputRadical(displayStr = "9", lookupStr = "9")
                ),
                TapKeyData<ArrayAction>(
                    text = "0",
                    action = ArrayAction.InputRadical(displayStr = "0", lookupStr = "0")
                )
            ),
            listOf(
                TapKeyData<ArrayAction>(
                    text = "1^",
                    action = ArrayAction.InputRadical(displayStr = "1^", lookupStr = "q")
                ),
                TapKeyData<ArrayAction>(
                    text = "2^",
                    action = ArrayAction.InputRadical(displayStr = "2^", lookupStr = "w")
                ),
                TapKeyData<ArrayAction>(
                    text = "3^",
                    action = ArrayAction.InputRadical(displayStr = "3^", lookupStr = "e")
                ),
                TapKeyData<ArrayAction>(
                    text = "4^",
                    action = ArrayAction.InputRadical(displayStr = "4^", lookupStr = "r")
                ),
                TapKeyData<ArrayAction>(
                    text = "5^",
                    action = ArrayAction.InputRadical(displayStr = "5^", lookupStr = "t")
                ),
                TapKeyData<ArrayAction>(
                    text = "6^",
                    action = ArrayAction.InputRadical(displayStr = "6^", lookupStr = "y")
                ),
                TapKeyData<ArrayAction>(
                    text = "7^",
                    action = ArrayAction.InputRadical(displayStr = "7^", lookupStr = "u")
                ),
                TapKeyData<ArrayAction>(
                    text = "8^",
                    action = ArrayAction.InputRadical(displayStr = "8^", lookupStr = "i")
                ),
                TapKeyData<ArrayAction>(
                    text = "9^",
                    action = ArrayAction.InputRadical(displayStr = "9^", lookupStr = "o")
                ),
                TapKeyData<ArrayAction>(
                    text = "0^",
                    action = ArrayAction.InputRadical(displayStr = "0^", lookupStr = "p")
                )
            ),
            listOf(
                TapKeyData<ArrayAction>(
                    text = "1-",
                    action = ArrayAction.InputRadical(displayStr = "1-", lookupStr = "a")
                ),
                TapKeyData<ArrayAction>(
                    text = "2-",
                    action = ArrayAction.InputRadical(displayStr = "2-", lookupStr = "s")
                ),
                TapKeyData<ArrayAction>(
                    text = "3-",
                    action = ArrayAction.InputRadical(displayStr = "3-", lookupStr = "d")
                ),
                TapKeyData<ArrayAction>(
                    text = "4-",
                    action = ArrayAction.InputRadical(displayStr = "4-", lookupStr = "f")
                ),
                TapKeyData<ArrayAction>(
                    text = "5-",
                    action = ArrayAction.InputRadical(displayStr = "5-", lookupStr = "g")
                ),
                TapKeyData<ArrayAction>(
                    text = "6-",
                    action = ArrayAction.InputRadical(displayStr = "6-", lookupStr = "h")
                ),
                TapKeyData<ArrayAction>(
                    text = "7-",
                    action = ArrayAction.InputRadical(displayStr = "7-", lookupStr = "j")
                ),
                TapKeyData<ArrayAction>(
                    text = "8-",
                    action = ArrayAction.InputRadical(displayStr = "8-", lookupStr = "k")
                ),
                TapKeyData<ArrayAction>(
                    text = "9-",
                    action = ArrayAction.InputRadical(displayStr = "9-", lookupStr = "l")
                ),
                TapKeyData<ArrayAction>(
                    text = "0-",
                    action = ArrayAction.InputRadical(displayStr = "0-", lookupStr = ";")
                )
            ),
            listOf(
                TapKeyData<ArrayAction>(
                    text = "1v",
                    action = ArrayAction.InputRadical(displayStr = "1v", lookupStr = "z")
                ),
                TapKeyData<ArrayAction>(
                    text = "2v",
                    action = ArrayAction.InputRadical(displayStr = "2v", lookupStr = "x")
                ),
                TapKeyData<ArrayAction>(
                    text = "3v",
                    action = ArrayAction.InputRadical(displayStr = "3v", lookupStr = "c")
                ),
                TapKeyData<ArrayAction>(
                    text = "4v",
                    action = ArrayAction.InputRadical(displayStr = "4v", lookupStr = "v")
                ),
                TapKeyData<ArrayAction>(
                    text = "5v",
                    action = ArrayAction.InputRadical(displayStr = "5v", lookupStr = "b")
                ),
                TapKeyData<ArrayAction>(
                    text = "6v",
                    action = ArrayAction.InputRadical(displayStr = "6v", lookupStr = "n")
                ),
                TapKeyData<ArrayAction>(
                    text = "7v",
                    action = ArrayAction.InputRadical(displayStr = "7v", lookupStr = "m")
                ),
                TapKeyData<ArrayAction>(
                    text = "8v",
                    action = ArrayAction.InputRadical(displayStr = "8v", lookupStr = ",")
                ),
                TapKeyData<ArrayAction>(
                    text = "9v",
                    action = ArrayAction.InputRadical(displayStr = "9v", lookupStr = ".")
                ),
                TapKeyData<ArrayAction>(
                    text = "0v",
                    action = ArrayAction.InputRadical(displayStr = "0v", lookupStr = "/")
                )
            ),
            listOf(
                SystemKeyFactory.create(FuncType.LANGUAGE, 1.5f),
                SystemKeyFactory.create(FuncType.DPAD, 1.5f),
                SystemKeyFactory.create(FuncType.SPACE, 4.0f, ArrayAction.Space),
                SystemKeyFactory.create(FuncType.BACKSPACE, 1.5f, ArrayAction.Backspace),
                SystemKeyFactory.createEnterKey(editorInfo, 1.5f, ArrayAction.Enter)
            )
        )
    }
}