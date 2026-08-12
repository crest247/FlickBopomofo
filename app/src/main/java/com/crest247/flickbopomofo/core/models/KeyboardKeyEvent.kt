package com.crest247.flickbopomofo.core.models

sealed interface KeyboardKeyEvent {
    val keyData: KeyData
    val direction: Int? get() = null

    data class Down(override val keyData: KeyData) : KeyboardKeyEvent
    data class Click(override val keyData: KeyData, override val direction: Int? = null) : KeyboardKeyEvent
    data class Flick(override val keyData: KeyData, override val direction: Int) : KeyboardKeyEvent
    data class LongPress(override val keyData: KeyData, override val direction: Int? = null) : KeyboardKeyEvent
    data class Repeat(override val keyData: KeyData, override val direction: Int? = null) : KeyboardKeyEvent
    data class Up(override val keyData: KeyData, override val direction: Int? = null) : KeyboardKeyEvent
}

fun KeyboardKeyEvent.resolveAction(): KeyboardAction? {
    return when (val keyData = this.keyData) {
        is FlickKeyData<*> -> keyData.directionActions[this.direction ?: 0] as? KeyboardAction
        is VisibleKeyData<*> -> keyData.action as? KeyboardAction
        else -> null
    }
}