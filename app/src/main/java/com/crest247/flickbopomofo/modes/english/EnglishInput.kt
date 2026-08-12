package com.crest247.flickbopomofo.modes.english

import com.crest247.flickbopomofo.core.models.DownTriggerable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.UpTriggerable

enum class ShiftState {
    LOWERCASE,
    UPPERCASE,
    CAPS_LOCK
}

sealed interface EnglishAction : KeyboardAction {
    data class InputChar(val char: String) : EnglishAction, UpTriggerable
    object ToggleShift : KeyboardAction, DownTriggerable, UpTriggerable {
        override val downAction: KeyboardAction = ToggleShiftDown
        override val upAction: KeyboardAction = ToggleShiftUp
    }
    object ToggleShiftDown : EnglishAction, DownTriggerable
    object ToggleShiftUp : EnglishAction, UpTriggerable
}
