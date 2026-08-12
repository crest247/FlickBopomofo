package com.crest247.flickbopomofo.core.models

interface KeyboardAction

interface DownTriggerable : KeyboardAction {
    val downAction: KeyboardAction get() = this
}
interface Clickable : KeyboardAction {
    val clickAction: KeyboardAction get() = this
}
interface Flickable : KeyboardAction {
    val flickAction: KeyboardAction get() = this
}
interface LongPressable : KeyboardAction {
    val longPressAction: KeyboardAction get() = this
}
interface Repeatable : KeyboardAction {
    val repeatAction: KeyboardAction get() = this
}
interface UpTriggerable : KeyboardAction {
    val upAction: KeyboardAction get() = this
}