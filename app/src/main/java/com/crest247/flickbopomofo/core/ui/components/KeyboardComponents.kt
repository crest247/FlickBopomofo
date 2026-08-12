package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.crest247.flickbopomofo.core.theme.KeyboardTheme
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.preview.KeyboardPreviewWrapper
import com.crest247.flickbopomofo.core.ui.preview.ThemePreviews

sealed interface KeyContent {
    data class Text(val text: String) : KeyContent
    data class Icon(val icon: ImageVector, val flipHorizontal: Boolean = false) : KeyContent
}

@Composable
fun Modifier.keyboardBackgroundStyle(): Modifier {
    val colors = LocalKeyboardColors.current
    return this.background(colors.background)
}

@Composable
fun Modifier.keyboardRowStyle(rowHeight: Dp): Modifier {
    return this
        .fillMaxWidth()
        .height(rowHeight)
}

@Composable
fun KeyboardIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp,
    tint: Color = LocalKeyboardColors.current.keyText
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(iconSize)
    )
}

@Composable
fun KeyboardText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    color: Color = LocalKeyboardColors.current.keyText
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun KeyBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalKeyboardColors.current.keyBackground,
    content: @Composable BoxScope.() -> Unit
) {
    val dimens = LocalKeyboardDimens.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .padding(dimens.keyPadding)
            .clip(RoundedCornerShape(dimens.keyRadius))
            .background(backgroundColor)
    ) {
        content()
    }
}

@Composable
fun KeyContent.Display(
    textSize: TextUnit,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    color: Color = LocalKeyboardColors.current.keyText
) {
    when (this) {
        is KeyContent.Text -> KeyboardText(
            text = this.text,
            fontSize = textSize,
            modifier = modifier,
            color = color
        )

        is KeyContent.Icon -> KeyboardIcon(
            icon = this.icon,
            iconSize = iconSize,
            modifier = if (this.flipHorizontal) modifier.graphicsLayer(scaleX = -1f) else modifier,
            tint = color
        )
    }
}

@Composable
fun KeyButton(
    content: KeyContent,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalKeyboardColors.current.keyBackground,
    contentColor: Color = LocalKeyboardColors.current.keyText,
    showIndicator: Boolean = false
) {
    val dimens = LocalKeyboardDimens.current

    KeyBox(
        backgroundColor = backgroundColor,
        modifier = modifier
    ) {
        content.Display(
            textSize = dimens.keyTextSize,
            iconSize = dimens.keyIconSize,
            color = contentColor
        )

        if (showIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = dimens.flickIndicatorPadding, top = dimens.flickIndicatorPadding)
                    .size(dimens.flickIndicatorSize)
                    .background(color = contentColor.copy(alpha = 0.4f), shape = CircleShape)
            )
        }
    }
}

@ThemePreviews
@Composable
fun PreviewKeyButton() {
    KeyboardTheme {
        val dimens = LocalKeyboardDimens.current
        KeyboardPreviewWrapper {
            Row(modifier = Modifier.keyboardRowStyle(dimens.englishKeyHeight)) {
                KeyButton(
                    content = KeyContent.Text("Q"),
                    modifier = Modifier.width(40.dp)
                )

                KeyButton(
                    content = KeyContent.Icon(Icons.AutoMirrored.Outlined.Backspace),
                    backgroundColor = LocalKeyboardColors.current.funcKeyBackground,
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}