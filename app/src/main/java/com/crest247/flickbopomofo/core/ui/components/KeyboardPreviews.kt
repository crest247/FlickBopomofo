package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import com.crest247.flickbopomofo.core.models.KeyBackgroundType
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.theme.resolveColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

sealed interface KeyPreviewState {
    val keyPosition: Offset
    val keyWidth: Float
    val keyHeight: Float
    val backgroundType: KeyBackgroundType
}

data class TapPreview(
    val content: KeyContent,
    override val keyPosition: Offset,
    override val keyWidth: Float,
    override val keyHeight: Float,
    override val backgroundType: KeyBackgroundType
) : KeyPreviewState

data class FlickPreview(
    val popupContents: List<KeyContent?>,
    val direction: Int,
    override val keyPosition: Offset,
    override val keyWidth: Float,
    override val keyHeight: Float,
    override val backgroundType: KeyBackgroundType
) : KeyPreviewState

class PreviewHandler {
    private val _activePreviews = mutableStateMapOf<Any, KeyPreviewState>()
    val activePreviews: Map<Any, KeyPreviewState> get() = _activePreviews
    fun show(keyId: Any, state: KeyPreviewState) {
        _activePreviews[keyId] = state
    }

    fun hide(keyId: Any) {
        _activePreviews.remove(keyId)
    }
}

val LocalPreviewHandler = compositionLocalOf<PreviewHandler> {
    error("PreviewHandler not provided")
}

@Composable
fun KeyPreviewOverlay(state: KeyPreviewState) {
    val density = LocalDensity.current
    val dimens = LocalKeyboardDimens.current
    val colors = LocalKeyboardColors.current

    val bgColor = state.backgroundType.resolveColor()

    val bounds = remember(state, dimens, density) {
        PreviewGeometryCalculator.calculateBounds(state, dimens, density)
    }

    val bubbleShape = RoundedCornerShape(bounds.scaledCornerRadius)

    Box(
        modifier = Modifier
            .offset(x = bounds.x, y = bounds.y)
            .size(width = bounds.width, height = bounds.height)
            .border(
                width = dimens.previewBorderWidth,
                color = colors.keyText.copy(alpha = 0.1f),
                shape = bubbleShape
            )
            .clip(bubbleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is TapPreview -> {
                state.content.Display(
                    textSize = bounds.scaledTextSize,
                    iconSize = bounds.scaledIconSize,
                    color = colors.previewText
                )
            }

            is FlickPreview -> {
                FlickContent(state.popupContents, state.direction)
            }
        }
    }
}

@Composable
fun BoxScope.FlickContent(
    popupContents: List<KeyContent?>,
    selectDirection: Int
) {
    if (selectDirection > 0) {
        popupContents[selectDirection]?.Display(
            textSize = LocalKeyboardDimens.current.previewTextSize,
            iconSize = LocalKeyboardDimens.current.previewIconSize,
            modifier = Modifier.align(Alignment.Center),
            color = LocalKeyboardColors.current.previewText
        )
    } else {
        popupContents[0]?.Display(
            textSize = LocalKeyboardDimens.current.previewTextSize,
            iconSize = LocalKeyboardDimens.current.previewIconSize,
            modifier = Modifier.align(BiasAlignment(0f, 0f)),
            color = LocalKeyboardColors.current.previewText
        )

        val directionCount = popupContents.size - 1
        for (i in 0 until directionCount) {
            popupContents[i + 1]?.Display(
                textSize = LocalKeyboardDimens.current.previewSmallTextSize,
                iconSize = LocalKeyboardDimens.current.previewSmallIconSize,
                modifier = Modifier.align(
                    BiasAlignment(
                        cos(-PI / 2 + 2 * PI * i.toFloat() / directionCount.toFloat()).toFloat(),
                        sin(-PI / 2 + 2 * PI * i.toFloat() / directionCount.toFloat()).toFloat(),
                    )
                ),
                color = LocalKeyboardColors.current.previewText
            )
        }
    }
}
