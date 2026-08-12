package com.crest247.flickbopomofo.modes.shared.array

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.crest247.flickbopomofo.core.engine.InputProcessor
import com.crest247.flickbopomofo.core.engine.SystemAction
import com.crest247.flickbopomofo.core.models.KeyboardAction

open class ArrayProcessor : InputProcessor {
    protected var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null; private set
    val displayTokens = mutableStateListOf<String>()
    protected val lookupTokens = mutableStateListOf<String>()
    var candidates by mutableStateOf<List<String>>(emptyList()); private set
    var selectedCandidateIndex by mutableIntStateOf(0); private set
    private var visibleCandidateCount by mutableIntStateOf(5)

    override fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.inputConnection = inputConnection
        this.editorInfo = editorInfo
    }

    fun updateVisibleCandidateCount(count: Int) {
        if (count > 0) visibleCandidateCount = count
    }

    override fun resetStates() {
        displayTokens.clear()
        lookupTokens.clear()
        candidates = emptyList()
        selectedCandidateIndex = 0
    }

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is ArrayAction) return action

        when (action) {
            is ArrayAction.InputRadical -> {
                if (isInvalidArrayRadical(action.lookupStr))
                    return null
                displayTokens.add(action.displayStr)
                lookupTokens.add(action.lookupStr)
                updateCandidates()
            }

            is ArrayAction.Backspace -> {
                if (displayTokens.isNotEmpty()) {
                    displayTokens.removeAt(displayTokens.lastIndex)
                    lookupTokens.removeAt(lookupTokens.lastIndex)
                    if (displayTokens.isEmpty())
                        candidates = emptyList()
                    else
                        updateCandidates()
                } else
                    return SystemAction.Backspace
            }

            is ArrayAction.Space -> {
                if (candidates.isNotEmpty()) commitCandidate(candidates.first())
                else if (displayTokens.isEmpty()) return SystemAction.Space
            }

            is ArrayAction.Enter -> {
                if (candidates.isNotEmpty()) commitCandidate(candidates.first())
                else if (displayTokens.isEmpty()) return SystemAction.Enter
            }
        }
        return null
    }

    private fun updateCandidates() {
        val lookupString = lookupTokens.joinToString("")
        candidates = ArrayDecoder.decode(lookupString)
        selectedCandidateIndex = 0
    }

    fun commitCandidate(word: String) {
        inputConnection?.commitText(word, 1)
        clearState()
    }

    private fun clearState() {
        displayTokens.clear()
        lookupTokens.clear()
        candidates = emptyList()
        selectedCandidateIndex = 0
    }

    private fun isInvalidArrayRadical(nextLookupStr: String): Boolean {
        return lookupTokens.size > 4 ||
                (lookupTokens.size == 4 && nextLookupStr != "i") ||
                (lookupTokens.size >= 2 && lookupTokens[0] == "w" && lookupTokens[1] in "0".."9")
    }

    override fun onHardwareKeyDown(event: KeyEvent): Boolean {
        when (val keyCode = event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (candidates.isNotEmpty()) {
                    selectedCandidateIndex =
                        (selectedCandidateIndex - visibleCandidateCount).coerceAtLeast(0)
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (candidates.isNotEmpty()) {
                    selectedCandidateIndex =
                        (selectedCandidateIndex + visibleCandidateCount).coerceAtMost(candidates.lastIndex)
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (candidates.isNotEmpty()) {
                    if (selectedCandidateIndex > 0) selectedCandidateIndex--
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (candidates.isNotEmpty()) {
                    if (selectedCandidateIndex < candidates.lastIndex) selectedCandidateIndex++
                    return true
                }
                return false
            }

            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 ->
                if (candidates.isNotEmpty() && !(lookupTokens.size == 1 && lookupTokens[0] == "w")) {
                    val pageStart =
                        (selectedCandidateIndex / visibleCandidateCount) * visibleCandidateCount
                    val relativeIndex =
                        if (keyCode == KeyEvent.KEYCODE_0) 9 else keyCode - KeyEvent.KEYCODE_1
                    val targetIndex = pageStart + relativeIndex

                    if (targetIndex in candidates.indices) {
                        commitCandidate(candidates[targetIndex])
                    }
                    return true
                }

            in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9 ->
                if (candidates.isNotEmpty() && !(lookupTokens.size == 1 && lookupTokens[0] == "w")) {
                    val pageStart =
                        (selectedCandidateIndex / visibleCandidateCount) * visibleCandidateCount
                    val relativeIndex =
                        if (keyCode == KeyEvent.KEYCODE_NUMPAD_0) 9 else keyCode - KeyEvent.KEYCODE_NUMPAD_1
                    val targetIndex = pageStart + relativeIndex

                    if (targetIndex in candidates.indices) {
                        commitCandidate(candidates[targetIndex])
                    }
                    return true
                }

            KeyEvent.KEYCODE_DEL -> {
                if (displayTokens.isNotEmpty()) {
                    displayTokens.removeAt(displayTokens.lastIndex)
                    lookupTokens.removeAt(lookupTokens.lastIndex)
                    if (displayTokens.isEmpty())
                        candidates = emptyList()
                    else
                        updateCandidates()
                    return true
                }
                return false
            }

            KeyEvent.KEYCODE_SPACE -> {
                if (candidates.isNotEmpty()) {
                    commitCandidate(candidates[selectedCandidateIndex])
                    return true
                } else if (displayTokens.isEmpty()) {
                    return false
                }
                return true
            }

            KeyEvent.KEYCODE_ENTER -> {
                if (candidates.isNotEmpty()) {
                    commitCandidate(candidates[selectedCandidateIndex])
                    return true
                } else if (displayTokens.isEmpty()) {
                    return false
                }
                return true
            }
        }

        val unicode = event.unicodeChar
        if (unicode != 0) {
            val char = unicode.toChar().toString()

            if (isInvalidArrayRadical(char)) return true

            displayTokens.add(char)
            lookupTokens.add(char)
            updateCandidates()
            return true
        }

        return false
    }
}