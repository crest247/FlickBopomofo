package com.crest247.flickbopomofo.modes.bopomofo

import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.Flickable
import com.crest247.flickbopomofo.core.models.KeyboardAction
import com.crest247.flickbopomofo.core.models.LongPressable
import com.crest247.flickbopomofo.core.models.Repeatable
import com.crest247.flickbopomofo.core.models.UpTriggerable

sealed interface BopomofoAction : KeyboardAction {
    data class InputRadical(
        val radical: String?,
        val direction: Int
    ) : KeyboardAction, UpTriggerable, LongPressable {
        override val upAction: KeyboardAction
            get() = if (radical != null)
                InputRadicalUp(radical)
            else super.upAction
    }

    data class InputRadicalUp(
        val radical: String,
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

    data class InputSymbolUp(val symbol: String) : BopomofoAction, UpTriggerable
    data class InputSymbolClick(val symbol: String) : BopomofoAction, Clickable

    object Backspace : BopomofoAction, Clickable, Repeatable
    object Space : BopomofoAction, UpTriggerable
    object Enter : BopomofoAction, UpTriggerable
}