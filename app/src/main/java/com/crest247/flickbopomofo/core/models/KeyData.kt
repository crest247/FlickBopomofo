package com.crest247.flickbopomofo.core.models

import com.crest247.flickbopomofo.core.ui.components.KeyContent

enum class FuncType {
    ENTER, SPACE, LANGUAGE, TAB, BACKSPACE, SHIFT, DELETE, DPAD
}

enum class KeyBackgroundType {
    NORMAL,
    FUNCTIONAL
}

sealed interface KeyData {
    val weight: Float
}

data class SpacerData(override val weight: Float) : KeyData

sealed interface VisibleKeyData<T> : KeyData {
    val content: KeyContent
    val action: T
    val backgroundType: KeyBackgroundType
}

data class TapKeyData<T>(
    override val content: KeyContent,
    override val action: T,
    override val weight: Float = 1.0f,
    override val backgroundType: KeyBackgroundType = KeyBackgroundType.NORMAL
) : VisibleKeyData<T> {
    constructor(
        text: String,
        action: T,
        weight: Float = 1.0f,
        backgroundType: KeyBackgroundType = KeyBackgroundType.NORMAL
    ) : this(
        content = KeyContent.Text(text),
        action = action,
        weight = weight,
        backgroundType = backgroundType
    )
}

data class FlickKeyData<T>(
    override val content: KeyContent,
    val popupContents: List<KeyContent?>,
    val directionActions: Map<Int, T>,
    override val weight: Float = 1.0f,
    override val backgroundType: KeyBackgroundType = KeyBackgroundType.NORMAL
) : VisibleKeyData<Map<Int, T>> {
    override val action = directionActions
}

