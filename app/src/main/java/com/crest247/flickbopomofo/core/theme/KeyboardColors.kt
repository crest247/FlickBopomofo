package com.crest247.flickbopomofo.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.crest247.flickbopomofo.core.models.KeyBackgroundType

@Immutable
data class KeyboardColors(
    val background: Color,
    val keyBackground: Color,
    val keyText: Color,
    val funcKeyBackground: Color,
    val funcKeyText: Color,
    val candidateBackground: Color,
    val candidateUnselectedBackground: Color,
    val candidateText: Color,
    val candidateSelectedBackground: Color,
    val candidateSelectedBorder: Color,
    val radicalPanelBackground: Color,
    val radicalText: Color,
    val previewBackground: Color,
    val previewText: Color,
    val scrollBarThumb: Color
)

val LocalKeyboardColors = staticCompositionLocalOf<KeyboardColors> {
    error("No KeyboardColors provided")
}

fun getLightKeyboardColors(scheme: ColorScheme): KeyboardColors {
    return KeyboardColors(
        background = scheme.surfaceContainerLow,

        keyBackground = scheme.surfaceContainerLowest,
        keyText = scheme.onSurface,

        funcKeyBackground = scheme.secondaryContainer,
        funcKeyText = scheme.onSecondaryContainer,

        candidateBackground = scheme.surfaceContainerLow,
        candidateUnselectedBackground = scheme.surfaceContainerHigh,
        candidateText = scheme.onSurface,
        candidateSelectedBackground = scheme.secondaryContainer,
        candidateSelectedBorder = scheme.outline,

        radicalPanelBackground = scheme.secondaryContainer.copy(alpha = 0.6f),
        radicalText = scheme.onSecondaryContainer,

        previewBackground = scheme.surfaceVariant,
        previewText = scheme.onSurfaceVariant,
        scrollBarThumb = scheme.primary
    )
}

fun getDarkKeyboardColors(scheme: ColorScheme): KeyboardColors {
    return KeyboardColors(
        background = scheme.surfaceContainerLow,

        keyBackground = scheme.surfaceContainerHigh,
        keyText = scheme.onSurface,

        funcKeyBackground = scheme.secondaryContainer,
        funcKeyText = scheme.onSecondaryContainer,

        candidateBackground = scheme.surfaceContainerLow,
        candidateUnselectedBackground = scheme.surfaceContainerHigh,
        candidateText = scheme.onSurface,
        candidateSelectedBackground = scheme.secondaryContainer,
        candidateSelectedBorder = scheme.outline,

        radicalPanelBackground = scheme.surfaceContainerHigh.copy(alpha = 0.6f),
        radicalText = scheme.onSurface,

        previewBackground = scheme.surfaceContainerHighest,
        previewText = scheme.onSurface,
        scrollBarThumb = scheme.primary
    )
}

@Composable
fun KeyBackgroundType.resolveColor(): Color {
    val colors = LocalKeyboardColors.current

    return when (this) {
        KeyBackgroundType.NORMAL -> colors.keyBackground
        KeyBackgroundType.FUNCTIONAL -> colors.funcKeyBackground
    }
}
