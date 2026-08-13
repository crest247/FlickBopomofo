package com.crest247.flickbopomofo.modes.bopomofo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens

@Composable
fun BopomofoSpellingLayout(processor: BopomofoProcessor) {
    val dimens = LocalKeyboardDimens.current
    val colors = LocalKeyboardColors.current

    val displayText = processor.displayTokens.joinToString("")
    val hasTokens = processor.displayTokens.isNotEmpty()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(dimens.radicalHeight)
            .wrapContentWidth()
            .clip(
                RoundedCornerShape(
                    topStart = dimens.keyRadius,
                    topEnd = dimens.keyRadius,
                    bottomEnd = 0.dp,
                    bottomStart = 0.dp
                )
            )
            .background(colors.radicalPanelBackground)
            .padding(horizontal = if (hasTokens) dimens.radicalPadding else 0.dp)
    ) {
        Text(
            text = displayText,
            fontSize = dimens.radicalTextSize,
            color = colors.radicalText
        )
    }
}