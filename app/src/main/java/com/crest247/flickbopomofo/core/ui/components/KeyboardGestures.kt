package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import com.crest247.flickbopomofo.core.models.KeyBackgroundType
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

val LocalKeyboardLayoutCoordinates = compositionLocalOf<LayoutCoordinates?> { null }

fun Modifier.keyGestures(
    onDown: (() -> Unit)? = null,
    onClick: ((Int) -> Unit)? = null,
    onFlick: ((Int) -> Unit)? = null,
    onLongPress: ((Int) -> Unit)? = null,
    onRepeat: ((Int) -> Unit)? = null,
    onUp: ((Int) -> Unit)? = null,
    longPressTimeout: Long = 500L,
    repeatInterval: Long = 50L,
    directionCount: Int,
    flickThresholdPx: Float
): Modifier = composed {
    val currentOnDown by rememberUpdatedState(onDown)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnFlick by rememberUpdatedState(onFlick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    val currentOnUp by rememberUpdatedState(onUp)

    var timerTrigger by remember { mutableStateOf(false) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isLongPressTriggered by remember { mutableStateOf(false) }

    var trackedDirection by remember { mutableStateOf(0) }

    LaunchedEffect(isTimerRunning, timerTrigger) {
        if (isTimerRunning) {
            delay(longPressTimeout)
            isLongPressTriggered = true
            currentOnLongPress?.invoke(trackedDirection)

            if (currentOnRepeat != null) {
                while (isTimerRunning) {
                    currentOnRepeat?.invoke(trackedDirection)
                    delay(repeatInterval)
                }
            }
        }
    }

    this.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Main)
            val targetPointerId = down.id

            val startPosition = down.position
            var currentDirection = 0
            trackedDirection = 0

            isLongPressTriggered = false
            currentOnDown?.invoke()

            isTimerRunning = true
            timerTrigger = !timerTrigger

            var keepGoing = true

            while (keepGoing) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == targetPointerId }

                if (change == null || !change.pressed) {
                    keepGoing = false
                } else {
                    val newDirection = calculateFlickDirection(
                        startPosition, change.position, directionCount, flickThresholdPx
                    )

                    if (newDirection != currentDirection) {
                        currentDirection = newDirection
                        trackedDirection = newDirection

                        currentOnFlick?.invoke(newDirection)
                        change.consume()

                        isLongPressTriggered = false
                        timerTrigger = !timerTrigger
                    }
                }
            }

            isTimerRunning = false

            if (!isLongPressTriggered) {
                currentOnClick?.invoke(currentDirection)
            }

            currentOnUp?.invoke(currentDirection)
        }
    }
}

private fun calculateFlickDirection(
    start: Offset,
    current: Offset,
    directionCount: Int,
    threshold: Float
): Int {
    val deltaX = -(current.x - start.x)
    val deltaY = -(current.y - start.y)
    val absX = abs(deltaX)
    val absY = abs(deltaY)
    val deltaAngle = atan2(deltaY, deltaX).let { if (it < 0) it + 2 * Math.PI.toFloat() else it }

    val judgeAngle = (deltaAngle - PI / 2 + PI / directionCount.toFloat()) / (2 * PI)

    if (absX < threshold && absY < threshold)
        return 0
    else {
        if (judgeAngle < 0)
            return directionCount
        for (i in 1..directionCount) {
            if (judgeAngle < i.toFloat() / directionCount.toFloat())
                return i
        }
        return directionCount
    }
}

@Composable
fun Modifier.tapWithPreview(
    keyId: Any,
    content: KeyContent,
    backgroundType: KeyBackgroundType,
    onDown: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null,
    onUp: (() -> Unit)? = null
): Modifier = composed {
    val density = LocalDensity.current
    val dimens = LocalKeyboardDimens.current
    val previewHandler = LocalPreviewHandler.current
    val keyboardAncestor = LocalKeyboardLayoutCoordinates.current

    val threshold = with(density) { dimens.tapCancelThreshold.toPx() }

    var keyPosition by remember { mutableStateOf(Offset.Zero) }
    var keyWidth by remember { mutableFloatStateOf(0f) }
    var keyHeight by remember { mutableFloatStateOf(0f) }

    this
        .onGloballyPositioned { coordinates ->
            keyPosition = coordinates.positionInRoot()
            keyWidth = coordinates.size.width.toFloat()
            keyHeight = coordinates.size.height.toFloat()
            keyPosition = keyboardAncestor?.localPositionOf(coordinates, Offset.Zero) ?: Offset.Zero
        }
        .keyGestures(
            onDown = {
                previewHandler.show(
                    keyId,
                    TapPreview(content, keyPosition, keyWidth, keyHeight, backgroundType)
                )
                onDown?.invoke()
            },
            onClick = { direction ->
                if (direction == 0) onClick?.invoke()
            },
            onFlick = { direction ->
                if (direction == 0)
                    previewHandler.show(
                        keyId,
                        TapPreview(content, keyPosition, keyWidth, keyHeight, backgroundType)
                    )
                else
                    previewHandler.hide(keyId)
            },
            onLongPress = { direction ->
                if (direction == 0) onLongPress?.invoke()
            },
            onRepeat = { direction ->
                if (direction == 0) onRepeat?.invoke()
            },
            onUp = { direction ->
                previewHandler.hide(keyId)
                if (direction == 0) onUp?.invoke()
            },
            directionCount = 1,
            flickThresholdPx = threshold
        )
}

@Composable
fun Modifier.flickWithPreview(
    keyId: Any,
    popupContents: List<KeyContent?>,
    backgroundType: KeyBackgroundType,
    onDown: (() -> Unit)? = null,
    onClick: ((Int) -> Unit)? = null,
    onFlick: ((Int) -> Unit)? = null,
    onLongPress: ((Int) -> Unit)? = null,
    onRepeat: ((Int) -> Unit)? = null,
    onUp: ((Int) -> Unit)? = null
): Modifier = composed {
    val density = LocalDensity.current
    val dimens = LocalKeyboardDimens.current
    val previewHandler = LocalPreviewHandler.current
    val threshold = with(density) { dimens.flickThreshold.toPx() }

    var keyPosition by remember { mutableStateOf(Offset.Zero) }
    var keyWidth by remember { mutableFloatStateOf(0f) }
    var keyHeight by remember { mutableFloatStateOf(0f) }

    val keyboardAncestor = LocalKeyboardLayoutCoordinates.current

    this
        .onGloballyPositioned { coordinates ->
            keyPosition = coordinates.positionInRoot()
            keyWidth = coordinates.size.width.toFloat()
            keyHeight = coordinates.size.height.toFloat()
            keyPosition = keyboardAncestor?.localPositionOf(coordinates, Offset.Zero) ?: Offset.Zero
        }
        .keyGestures(
            onDown = {
                previewHandler.show(
                    keyId,
                    FlickPreview(popupContents, 0, keyPosition, keyWidth, keyHeight, backgroundType)
                )
                onDown?.invoke()
            },
            onClick = { direction ->
                onClick?.invoke(direction)
            },
            onFlick = { direction ->
                previewHandler.show(
                    keyId,
                    FlickPreview(
                        popupContents,
                        direction,
                        keyPosition,
                        keyWidth,
                        keyHeight,
                        backgroundType
                    )
                )
                onFlick?.invoke(direction)
            },
            onLongPress = { direction ->
                onLongPress?.invoke(direction)
            },
            onRepeat = { direction ->
                onRepeat?.invoke(direction)
            },
            onUp = { direction ->
                previewHandler.hide(keyId)
                onUp?.invoke(direction)
            },
            directionCount = popupContents.size - 1,
            flickThresholdPx = threshold
        )
}