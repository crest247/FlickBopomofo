package com.crest247.flickbopomofo.core.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

data class KeyboardDimens(
    val keyTextSize: TextUnit,
    val keyIconSize: Dp,
    val candidateVerticalPadding: Dp,
    val candidateHorizontalPadding: Dp,
    val candidateHeight: Dp,
    val candidateTextSize: TextUnit,
    val candidateSelectedBorder: Dp,
    val radicalHeight: Dp,
    val radicalTextSize: TextUnit,
    val radicalPadding: Dp,
    val keyPadding: Dp,
    val keyRadius: Dp,
    val previewTextSize: TextUnit,
    val previewSmallTextSize: TextUnit,
    val previewIconSize: Dp,
    val previewSmallIconSize: Dp,
    val previewScale: Float,
    val previewGap: Dp,
    val previewTransparentPadding: Dp,
    val previewCornerRadius: Dp,
    val previewBorderWidth: Dp,
    val englishKeyHeight: Dp,
    val array30KeyHeight: Dp,
    val numberKeyHeight: Dp,
    val arrayFlickKeyHeight: Dp,
    val symbolKeyHeight: Dp,
    val emojiKeyHeight: Dp,
    val flickThreshold: Dp,
    val tapCancelThreshold: Dp,
    val mainScreenPadding: Dp,
    val mainScreenButtonSpacing: Dp,
    val mainScreenBlockSpacing: Dp,
    val flickIndicatorSize: Dp,
    val flickIndicatorPadding: Dp
)

val LocalKeyboardDimens = staticCompositionLocalOf<KeyboardDimens> {
    error("KeyboardDimens not provided. Did you forget to wrap your UI in KeyboardTheme?")
}