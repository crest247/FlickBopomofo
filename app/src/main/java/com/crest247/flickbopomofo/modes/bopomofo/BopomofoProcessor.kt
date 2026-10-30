package com.crest247.flickbopomofo.modes.bopomofo

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
import com.crest247.flickbopomofo.core.extension.sendDownUpKeyEvents
import com.crest247.flickbopomofo.core.extension.sendKeyDownEvent
import com.crest247.flickbopomofo.core.extension.sendKeyUpEvent
import com.crest247.flickbopomofo.core.models.KeyboardAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BopomofoProcessor : InputProcessor {
    private var inputConnection: InputConnection? = null
    var editorInfo: EditorInfo? = null; private set
    val radicals = mutableStateListOf<String>()
    val selectedCandidates = mutableStateListOf<BopomofoCandidate>()
    private val selectedTokens = mutableListOf<List<String>>()
    var candidates by mutableStateOf<List<List<BopomofoCandidate>>>(emptyList()); private set
    var selectedCandidateIndex by mutableIntStateOf(0); private set
    var pageRequest by mutableStateOf<PageRequest?>(null); private set
    private var pageRequestId = 0
    var metaCode by mutableIntStateOf(0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var decodeJob: Job? = null
    private var requestId = 0

    @Volatile
    private var latestRequestId = 0
    private var candidatesInput = ""
    private var pendingSelect = false

    override fun updateConnection(inputConnection: InputConnection, editorInfo: EditorInfo) {
        this.inputConnection = inputConnection
        this.editorInfo = editorInfo
    }

    data class PageRequest(val id: Int, val forward: Boolean)

    fun updateSelectedCandidateIndex(index: Int) {
        if (index in candidates.indices) selectedCandidateIndex = index
    }

    override fun resetStates() {
        clearState()
    }

    override fun onAction(action: KeyboardAction): KeyboardAction? {
        if (action !is BopomofoAction) return action

        when (action) {
            is BopomofoAction.InputSymbolUp ->
                inputConnection?.commitText(action.symbol, 1)

            is BopomofoAction.InputSymbolClick ->
                inputConnection?.commitText(action.symbol, 1)

            is BopomofoAction.InputRadicalUp -> {
                radicals.add(action.radical)
                updateCandidates()
            }

            is BopomofoAction.DirectionalPad -> {
                if (metaCode == KeyEvent.KEYCODE_CTRL_LEFT)
                    when (action.direction) {
                        1 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON
                        )

                        2 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_V, KeyEvent.META_CTRL_ON
                        )

                        3 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_Y, KeyEvent.META_CTRL_ON
                        )

                        4 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON
                        )

                        5 -> inputConnection?.sendDownUpKeyEvents(
                            KeyEvent.KEYCODE_C, KeyEvent.META_CTRL_ON
                        )
                    }
                else if (!moveSelection(action.direction) && !isComposing())
                    when (action.direction) {
                        1 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP, 0)
                        2 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT, 0)
                        3 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN, 0)
                        4 -> inputConnection?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                    }
            }

            is BopomofoAction.PhysicalModifierFlick -> {
                val newMetaCode = when (action.direction) {
                    1 -> KeyEvent.KEYCODE_SHIFT_LEFT
                    2 -> KeyEvent.KEYCODE_CTRL_LEFT
                    else -> 0
                }
                if (metaCode != newMetaCode) {
                    inputConnection?.sendKeyUpEvent(metaCode, 0)
                    inputConnection?.sendKeyDownEvent(newMetaCode, 0)
                    metaCode = newMetaCode
                }
            }

            is BopomofoAction.PhysicalModifierUp -> {
                inputConnection?.sendKeyUpEvent(metaCode, 0)
                metaCode = 0
            }

            is BopomofoAction.Enter -> {
                if (isDecoding()) pendingSelect = true
                else if (candidates.isNotEmpty()) selectCandidate(candidates[selectedCandidateIndex])
                else if (!isComposing())
                    when (metaCode) {
                        KeyEvent.KEYCODE_CTRL_LEFT -> {
                            inputConnection?.sendDownUpKeyEvents(
                                KeyEvent.KEYCODE_ENTER, KeyEvent.META_CTRL_ON
                            )
                        }

                        KeyEvent.KEYCODE_SHIFT_LEFT ->
                            inputConnection?.sendDownUpKeyEvents(
                                KeyEvent.KEYCODE_ENTER, KeyEvent.META_SHIFT_ON
                            )

                        else -> return SystemAction.Enter
                    }
            }

            is BopomofoAction.Backspace -> {
                if (radicals.isEmpty()) return SystemAction.Backspace
                radicals.removeAt(radicals.lastIndex)
                if (radicals.isEmpty() && selectedCandidates.isNotEmpty()) {
                    selectedCandidates.removeAt(selectedCandidates.lastIndex)
                    radicals.addAll(selectedTokens.removeAt(selectedTokens.lastIndex))
                }
                updateCandidates()
            }

            is BopomofoAction.Space -> {
                if (isDecoding()) pendingSelect = true
                else if (candidates.isNotEmpty()) selectCandidate(candidates[selectedCandidateIndex])
                else if (!isComposing()) return SystemAction.Space
            }
        }
        return null
    }

    private fun isComposing() = radicals.isNotEmpty() || selectedCandidates.isNotEmpty()

    private fun updateCandidates() {
        val input = radicals.joinToString("")
        val selectedUnitIds = selectedCandidates.map { it.unitId }.toIntArray()
        val currentRequestId = ++requestId
        latestRequestId = currentRequestId
        decodeJob?.cancel()
        if (input.isEmpty()) {
            setCandidates("", emptyList())
            return
        }
        decodeJob = scope.launch {
            val result = withContext(Dispatchers.Default) {
                BopomofoDecoder.decode(
                    input,
                    selectedUnitIds
                ) { latestRequestId != currentRequestId }
            } ?: return@launch
            if (currentRequestId != requestId) return@launch
            val sentence = result.sentences.firstOrNull()?.units
            setCandidates(
                input, listOfNotNull(sentence) + result.candidates
                    .filterNot {
                        sentence?.singleOrNull()
                            ?.let { sentenceUnit -> sentenceUnit.text == it.text && sentenceUnit.consumed == it.consumed } == true
                    }
                    .map { listOf(it) })
            if (pendingSelect) {
                pendingSelect = false
                candidates.firstOrNull()?.let { selectCandidate(it) }
            }
        }
    }

    private fun setCandidates(input: String, list: List<List<BopomofoCandidate>>) {
        candidatesInput = input
        candidates = list
        selectedCandidateIndex = 0
    }

    private fun isDecoding() = decodeJob?.isActive == true

    fun selectCandidate(units: List<BopomofoCandidate>) {
        val end = units.last().consumed
        val input = radicals.joinToString("")
        if (end > input.length || !input.regionMatches(0, candidatesInput, 0, end)) return
        var start = 0
        for (unit in units) {
            selectedCandidates.add(unit)
            selectedTokens.add(radicals.subList(start, unit.consumed).toList())
            start = unit.consumed
        }
        repeat(start) { radicals.removeAt(0) }

        if (radicals.isEmpty()) {
            inputConnection?.commitText(selectedCandidates.joinToString("") { it.text }, 1)
            clearState()
        } else
            updateCandidates()
    }

    private fun clearState() {
        decodeJob?.cancel()
        latestRequestId = ++requestId
        pendingSelect = false
        candidatesInput = ""
        radicals.clear()
        selectedCandidates.clear()
        selectedTokens.clear()
        candidates = emptyList()
        selectedCandidateIndex = 0
    }

    private fun moveSelection(direction: Int): Boolean {
        if (candidates.isEmpty()) return false
        when (direction) {
            1 -> pageRequest = PageRequest(++pageRequestId, forward = false)
            2 -> selectedCandidateIndex =
                (selectedCandidateIndex + 1).coerceAtMost(candidates.lastIndex)

            3 -> pageRequest = PageRequest(++pageRequestId, forward = true)
            4 -> selectedCandidateIndex = (selectedCandidateIndex - 1).coerceAtLeast(0)
        }
        return true
    }

    override fun onHardwareKeyDown(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> return moveSelection(1) || isComposing()
            KeyEvent.KEYCODE_DPAD_RIGHT -> return moveSelection(2) || isComposing()
            KeyEvent.KEYCODE_DPAD_DOWN -> return moveSelection(3) || isComposing()
            KeyEvent.KEYCODE_DPAD_LEFT -> return moveSelection(4) || isComposing()

            KeyEvent.KEYCODE_DEL -> return onAction(BopomofoAction.Backspace)?.let { false } ?: true

            KeyEvent.KEYCODE_SPACE -> {
                if (radicals.isNotEmpty() && radicals.last()[0] !in BopomofoRules.MARK_TONE) {
                    radicals.add("ˉ")
                    updateCandidates()
                    return true
                }
                return onAction(BopomofoAction.Space)?.let { false } ?: true
            }

            KeyEvent.KEYCODE_ENTER -> return onAction(BopomofoAction.Enter)?.let { false } ?: true
        }

        val radical = radicalKeyMap[event.unicodeChar.toChar()] ?: return false
        radicals.add(radical)
        updateCandidates()
        return true
    }

    private val radicalKeyMap = mapOf(
        '1' to "ㄅ", 'q' to "ㄆ", 'a' to "ㄇ", 'z' to "ㄈ",
        '2' to "ㄉ", 'w' to "ㄊ", 's' to "ㄋ", 'x' to "ㄌ",
        'e' to "ㄍ", 'd' to "ㄎ", 'c' to "ㄏ",
        'r' to "ㄐ", 'f' to "ㄑ", 'v' to "ㄒ",
        '5' to "ㄓ", 't' to "ㄔ", 'g' to "ㄕ", 'b' to "ㄖ",
        'y' to "ㄗ", 'h' to "ㄘ", 'n' to "ㄙ",
        'u' to "ㄧ", 'j' to "ㄨ", 'm' to "ㄩ",
        '8' to "ㄚ", 'i' to "ㄛ", 'k' to "ㄜ", ',' to "ㄝ",
        '9' to "ㄞ", 'o' to "ㄟ", 'l' to "ㄠ", '.' to "ㄡ",
        '0' to "ㄢ", 'p' to "ㄣ", ';' to "ㄤ", '/' to "ㄥ", '-' to "ㄦ",
        '6' to "ˊ", '3' to "ˇ", '4' to "ˋ", '7' to "˙",
    )
}
