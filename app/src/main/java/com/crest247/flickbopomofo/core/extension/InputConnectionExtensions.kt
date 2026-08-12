package com.crest247.flickbopomofo.core.extension

import android.view.KeyEvent
import android.view.inputmethod.InputConnection

fun InputConnection.sendDownUpKeyEvents(keyCode: Int, metaCode: Int = 0) {
    val now = System.currentTimeMillis()
    sendKeyEvent(
        KeyEvent(
            now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaCode
        )
    )
    sendKeyEvent(
        KeyEvent(
            now, now, KeyEvent.ACTION_UP, keyCode, 0, metaCode
        )
    )
}

fun InputConnection.sendKeyUpEvent(keyCode: Int, metaCode: Int = 0) {
    val now = System.currentTimeMillis()
    sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaCode))
}

fun InputConnection.sendKeyDownEvent(keyCode: Int, metaCode: Int = 0) {
    val now = System.currentTimeMillis()
    sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaCode))
}