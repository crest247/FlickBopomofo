package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.crest247.flickbopomofo.core.theme.KeyboardDimens

data class PreviewBounds(
    val width: Dp,
    val height: Dp,
    val x: Dp,
    val y: Dp,
    val scaledCornerRadius: Dp,
    val scaledTextSize: TextUnit,
    val scaledIconSize: Dp
)

object PreviewGeometryCalculator {

    fun calculateBounds(
        state: KeyPreviewState,
        dimens: KeyboardDimens,
        density: Density
    ): PreviewBounds {
        return with(density) {
            val scale = dimens.previewScale

            val bubbleWidth = (state.keyWidth * scale).toDp()
            val bubbleHeight = (state.keyHeight * scale).toDp()

            val offsetX = state.keyPosition.x.toDp()
            val offsetY = state.keyPosition.y.toDp()
            val keyWidth = state.keyWidth.toDp()

            val finalX = offsetX + (keyWidth / 2) - (bubbleWidth / 2)
            val finalY = offsetY - bubbleHeight - dimens.previewGap

            val scaledRadius = dimens.previewCornerRadius * scale
            val scaledText = (dimens.previewTextSize.value * scale).sp
            val scaledIcon = dimens.previewIconSize * scale

            PreviewBounds(
                width = bubbleWidth,
                height = bubbleHeight,
                x = finalX,
                y = finalY,
                scaledCornerRadius = scaledRadius,
                scaledTextSize = scaledText,
                scaledIconSize = scaledIcon
            )
        }
    }
}