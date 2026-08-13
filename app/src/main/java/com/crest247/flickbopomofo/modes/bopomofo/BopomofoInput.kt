package com.crest247.flickbopomofo.modes.bopomofo

import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.Flickable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.LongPressable
import com.crest247.flickbopomofo.core.models.Repeatable
import com.crest247.flickbopomofo.core.models.UpTriggerable

sealed interface BopomofoAction : KeyboardAction {
    data class InputRadical(
        val displayStr: String?,
        val lookupStr: String?,
        val direction: Int
    ) : KeyboardAction, UpTriggerable, LongPressable {
        override val upAction: KeyboardAction
            get() = if (displayStr != null && lookupStr != null)
                InputRadicalUp(displayStr, lookupStr)
            else super.upAction
    }

    data class InputRadicalUp(
        val displayStr: String,
        val lookupStr: String,
    ) : BopomofoAction, Clickable

    data class PhysicalModifier(
        val direction: Int,
    ) : KeyboardAction, Flickable, UpTriggerable {
        override val flickAction: KeyboardAction = PhysicalModifierFlick(direction)
        override val upAction: KeyboardAction = PhysicalModifierUp
    }

    data class PhysicalModifierFlick(
        val direction: Int,
    ) : BopomofoAction, Flickable

    object PhysicalModifierUp : BopomofoAction, UpTriggerable

    data class DirectionalPad(
        val direction: Int
    ) : BopomofoAction, UpTriggerable

    object Backspace : BopomofoAction, Clickable, Repeatable
    object Space : BopomofoAction, UpTriggerable
    object Enter : BopomofoAction, UpTriggerable
}