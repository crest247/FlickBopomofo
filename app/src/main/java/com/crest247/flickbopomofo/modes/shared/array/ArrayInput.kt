package com.crest247.flickbopomofo.modes.shared.array

import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.Repeatable
import com.crest247.flickbopomofo.core.models.UpTriggerable

interface ArrayAction : KeyboardAction {
    data class InputRadical(
        val displayStr: String,
        val lookupStr: String
    ) : ArrayAction, UpTriggerable

    object Backspace : ArrayAction, Clickable, Repeatable
    object Space : ArrayAction, UpTriggerable
    object Enter : ArrayAction, UpTriggerable
}