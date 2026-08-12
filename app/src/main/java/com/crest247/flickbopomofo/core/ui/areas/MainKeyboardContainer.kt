package com.crest247.flickbopomofo.core.ui.areas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.crest247.flickbopomofo.core.state.KeyboardState
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens
import com.crest247.flickbopomofo.core.ui.components.KeyPreviewOverlay
import com.crest247.flickbopomofo.core.ui.components.LocalKeyboardLayoutCoordinates
import com.crest247.flickbopomofo.core.ui.components.LocalPreviewHandler
import com.crest247.flickbopomofo.core.ui.components.keyboardBackgroundStyle

@Composable
fun MainKeyboardContainer(
    state: KeyboardState,
    onKeyboardLayoutChanged: (LayoutCoordinates) -> Unit,
    onRadicalLayoutChanged: (LayoutCoordinates?) -> Unit
) {
    val previewHandler = LocalPreviewHandler.current
    val dimens = LocalKeyboardDimens.current
    val transparentTopPadding = dimens.previewTransparentPadding

    var stableCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(transparentTopPadding)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        if (coordinates.size.height > 0) {
                            onRadicalLayoutChanged(coordinates)
                        } else {
                            onRadicalLayoutChanged(null)
                        }
                    }
                ) {
                    state.currentModule.SpellingAreaLayout()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .keyboardBackgroundStyle()
            ) {

                CompositionLocalProvider(LocalKeyboardLayoutCoordinates provides stableCoordinates) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                stableCoordinates = coordinates
                                onKeyboardLayoutChanged(coordinates)
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            state.currentModule.SuggestionAreaLayout()
                            state.currentModule.KeyAreaLayout()
                        }

                        previewHandler.activePreviews.values.forEach { previewState ->
                            KeyPreviewOverlay(state = previewState)
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                )
            }
        }
    }
}