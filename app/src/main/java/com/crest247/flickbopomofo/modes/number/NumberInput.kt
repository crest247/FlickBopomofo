package com.crest247.flickbopomofo.modes.number

import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.UpTriggerable

sealed interface NumberAction : KeyboardAction {
    data class InputChar(val char: String) : NumberAction, UpTriggerable
    data class InputSymbolUp(val symbol: String) : NumberAction, UpTriggerable
    data class InputSymbolClick(val symbol: String) : NumberAction, Clickable
}
