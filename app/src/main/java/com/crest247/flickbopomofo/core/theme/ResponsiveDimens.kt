package com.crest247.flickbopomofo.core.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun rememberResponsiveDimens(): KeyboardDimens {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.smallestScreenWidthDp >= 600

    return remember(configuration) {
        val baseKeyHeight = when {
            isTablet && isLandscape -> 56.dp
            isTablet && !isLandscape -> 64.dp
            !isTablet && isLandscape -> 36.dp
            else -> 48.dp
        }

        val numPadKeyHeight = when {
            isTablet && isLandscape -> 64.dp
            isTablet && !isLandscape -> 72.dp
            !isTablet && isLandscape -> 42.dp
            else -> 56.dp
        }

        val padding = if (isTablet) 4.dp else 2.dp
        val candidateHeight = baseKeyHeight + padding * 4

        val keyTextSize = (baseKeyHeight.value * 0.45f).sp
        val keyIconSize = baseKeyHeight * 0.5f

        val previewTextSize = if (isLandscape && !isTablet) 20.sp else 24.sp
        val previewIconSize = if (isLandscape && !isTablet) 24.dp else 30.dp

        KeyboardDimens(
            keyTextSize = keyTextSize,
            keyIconSize = keyIconSize,
            smallKeyTextSize = keyTextSize / 2.0f,
            smallKeyIconSize = keyIconSize / 2.0f,
            candidateVerticalPadding = padding * 2,
            candidateHorizontalPadding = if (isLandscape || isTablet) padding * 2 else padding * 3,
            candidateHeight = candidateHeight,
            candidateTextSize = (candidateHeight.value * 0.4f).sp,
            candidateSelectedBorder = 2.dp,
            radicalHeight = if (isLandscape) baseKeyHeight * 0.7f else baseKeyHeight * 0.5f,
            radicalTextSize = (baseKeyHeight.value * 0.35f).sp,
            radicalPadding = padding * 4,
            keyPadding = padding,
            keyRadius = 8.dp,
            previewTextSize = previewTextSize,
            previewIconSize = previewIconSize,
            previewSmallTextSize = previewTextSize / 2.0f,
            previewSmallIconSize = previewIconSize / 2.0f,
            previewTransparentPadding = if (isLandscape && !isTablet) 60.dp else 80.dp,
            previewScale = 1.1f,
            previewGap = 4.dp,
            previewCornerRadius = 12.dp,
            previewBorderWidth = 0.5.dp,
            englishKeyHeight = baseKeyHeight,
            numberKeyHeight = numPadKeyHeight,
            bopomofoKeyHeight = numPadKeyHeight,
            symbolKeyHeight = baseKeyHeight,
            emojiKeyHeight = baseKeyHeight,
            flickThreshold = 10.dp,
            tapCancelThreshold = 50.dp,
            mainScreenPadding = 16.dp,
            mainScreenButtonSpacing = 8.dp,
            mainScreenBlockSpacing = 48.dp,
            flickIndicatorSize = if (isTablet) 6.dp else 4.dp,
            flickIndicatorPadding = if (isTablet) 8.dp else 5.dp
        )
    }
}