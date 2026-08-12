package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.crest247.flickbopomofo.core.models.Clickable
import com.crest247.flickbopomofo.core.models.DownTriggerable
import com.crest247.flickbopomofo.core.models.FlickKeyData
import com.crest247.flickbopomofo.core.models.Flickable
import com.crest247.flickbopomofo.core.models.KeyData
import com.crest247.flickbopomofo.core.models.KeyboardKeyEvent
import com.crest247.flickbopomofo.core.models.LongPressable
import com.crest247.flickbopomofo.core.models.Repeatable
import com.crest247.flickbopomofo.core.models.SpacerData
import com.crest247.flickbopomofo.core.models.TapKeyData
import com.crest247.flickbopomofo.core.models.UpTriggerable
import com.crest247.flickbopomofo.core.models.VisibleKeyData
import com.crest247.flickbopomofo.core.theme.resolveColor

@Composable
fun StandardKeyboard(
    keyRows: List<List<KeyData>>,
    rowHeight: Dp,
    onKeyEvent: (KeyboardKeyEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        keyRows.forEach { rowKeys ->
            Row(modifier = Modifier.keyboardRowStyle(rowHeight)) {
                rowKeys.forEach { keyData ->
                    key(keyData) {
                        when (keyData) {
                            is SpacerData -> {
                                Spacer(modifier = Modifier.weight(keyData.weight))
                            }

                            is VisibleKeyData<*> -> {
                                val bgColor = keyData.backgroundType.resolveColor()
                                when (keyData) {
                                    is TapKeyData<*> -> {
                                        val action = keyData.action
                                        KeyButton(
                                            content = keyData.content,
                                            backgroundColor = bgColor,
                                            modifier = Modifier
                                                .weight(keyData.weight)
                                                .tapWithPreview(
                                                    keyId = keyData.content,
                                                    content = keyData.content,
                                                    backgroundType = keyData.backgroundType,
                                                    onDown = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Down(keyData)
                                                        )
                                                    }.takeIf { action is DownTriggerable },
                                                    onClick = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Click(keyData, null)
                                                        )
                                                    }.takeIf { action is Clickable },
                                                    onLongPress = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.LongPress(keyData)
                                                        )
                                                    }.takeIf { action is LongPressable },
                                                    onRepeat = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Repeat(keyData)
                                                        )
                                                    }.takeIf { action is Repeatable },
                                                    onUp = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Up(keyData)
                                                        )
                                                    }.takeIf { action is UpTriggerable }
                                                )
                                        )
                                    }

                                    is FlickKeyData<*> -> {
                                        val actions = keyData.directionActions.values
                                        KeyButton(
                                            content = keyData.content,
                                            backgroundColor = bgColor,
                                            showIndicator = true,
                                            modifier = Modifier
                                                .weight(keyData.weight)
                                                .flickWithPreview(
                                                    keyData.content,
                                                    keyData.popupContents,
                                                    keyData.backgroundType,
                                                    onDown = {
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Down(keyData)
                                                        )
                                                    }.takeIf { actions.any { it is DownTriggerable } },
                                                    onClick = { direction: Int ->
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Click(
                                                                keyData,
                                                                direction
                                                            )
                                                        )
                                                    }.takeIf { actions.any { it is Clickable } },
                                                    onFlick = { direction: Int ->
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Flick(
                                                                keyData,
                                                                direction
                                                            )
                                                        )
                                                    }.takeIf { actions.any { it is Flickable } },
                                                    onLongPress = { direction: Int ->
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.LongPress(
                                                                keyData,
                                                                direction
                                                            )
                                                        )
                                                    }.takeIf { actions.any { it is LongPressable } },
                                                    onRepeat = { direction: Int ->
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Repeat(
                                                                keyData,
                                                                direction
                                                            )
                                                        )
                                                    }.takeIf { actions.any { it is Repeatable } },
                                                    onUp = { direction: Int ->
                                                        onKeyEvent(
                                                            KeyboardKeyEvent.Up(
                                                                keyData,
                                                                direction
                                                            )
                                                        )
                                                    }.takeIf { actions.any { it is UpTriggerable } }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}