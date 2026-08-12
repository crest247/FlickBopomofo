package com.crest247.flickbopomofo.modes.shared.array

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens

@Composable
fun ArraySuggestionLayout(processor: ArrayProcessor) {
    val dimens = LocalKeyboardDimens.current
    val colors = LocalKeyboardColors.current
    val lazyListState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val availableWidth = screenWidth - dimens.candidateHorizontalPadding
    val singleCandidateStepWidth = dimens.candidateHeight + dimens.candidateHorizontalPadding
    val calculatedCount = (availableWidth / singleCandidateStepWidth).toInt().coerceAtLeast(1)

    LaunchedEffect(calculatedCount) {
        processor.updateVisibleCandidateCount(calculatedCount)
    }

    val targetPageStartIndex =
        (processor.selectedCandidateIndex / calculatedCount) * calculatedCount

    LaunchedEffect(targetPageStartIndex) {
        if (processor.candidates.isNotEmpty()) {
            lazyListState.animateScrollToItem(targetPageStartIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.candidateHeight)
            .background(colors.candidateBackground)
    ) {
        if (processor.candidates.isNotEmpty()) {
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = dimens.candidateHorizontalPadding)
            ) {
                itemsIndexed(processor.candidates) { index, candidate ->
                    val isSelected = (index == processor.selectedCandidateIndex)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(
                                end = dimens.candidateHorizontalPadding,
                                top = dimens.candidateVerticalPadding,
                                bottom = dimens.candidateVerticalPadding
                            )
                            .aspectRatio(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(dimens.keyRadius))
                            .background(
                                if (isSelected) colors.candidateSelectedBackground
                                else colors.candidateUnselectedBackground
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    width = dimens.candidateSelectedBorder,
                                    color = colors.candidateSelectedBorder,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                        dimens.keyRadius
                                    )
                                )
                                else Modifier.Companion
                            )
                            .clickable {
                                processor.commitCandidate(candidate)
                            }
                    ) {
                        Text(
                            text = candidate,
                            fontSize = dimens.candidateTextSize,
                            color = colors.candidateText
                        )
                    }
                }
                item {
                    val totalCandidates = processor.candidates.size
                    val absoluteLastPageStart =
                        (totalCandidates - 1) / calculatedCount * calculatedCount
                    val lastPageRealCount = totalCandidates - absoluteLastPageStart
                    val actualItemWidth =
                        (dimens.candidateHeight - (dimens.candidateVerticalPadding * 2)) + dimens.candidateHorizontalPadding
                    val exactSpacerWidth = screenWidth - (actualItemWidth * lastPageRealCount)
                    if (exactSpacerWidth > 0.dp)
                        Spacer(modifier = Modifier.width(exactSpacerWidth))
                }
            }
        }
    }
}