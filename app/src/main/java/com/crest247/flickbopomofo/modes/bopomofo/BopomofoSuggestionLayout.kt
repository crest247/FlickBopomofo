package com.crest247.flickbopomofo.modes.bopomofo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens

@Composable
fun BopomofoSuggestionLayout(processor: BopomofoProcessor) {
    val dimens = LocalKeyboardDimens.current
    val colors = LocalKeyboardColors.current
    val lazyListState = rememberLazyListState()
    val pageStarts = remember(processor.candidates) { mutableListOf<Int>() }

    fun fullyVisibleIndices(): List<Int> {
        val layoutInfo = lazyListState.layoutInfo
        return layoutInfo.visibleItemsInfo
            .filter { it.offset >= layoutInfo.viewportStartOffset && it.offset + it.size <= layoutInfo.viewportEndOffset }
            .map { it.index }
    }

    LaunchedEffect(processor.pageRequest) {
        val request = processor.pageRequest ?: return@LaunchedEffect
        val candidates = processor.candidates
        if (candidates.isEmpty()) return@LaunchedEffect
        val visible = fullyVisibleIndices()
        if (request.forward) {
            val target = (visible.lastOrNull() ?: processor.selectedCandidateIndex) + 1
            if (target > candidates.lastIndex) {
                processor.updateSelectedCandidateIndex(candidates.lastIndex)
                return@LaunchedEffect
            }
            pageStarts.add(visible.firstOrNull() ?: processor.selectedCandidateIndex)
            lazyListState.animateScrollToItem(target)
            processor.updateSelectedCandidateIndex(target)
        } else {
            val previous = pageStarts.removeLastOrNull()
            if (previous != null) {
                lazyListState.animateScrollToItem(previous)
                processor.updateSelectedCandidateIndex(previous)
            } else {
                val layoutInfo = lazyListState.layoutInfo
                lazyListState.animateScrollBy(-(layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat())
                processor.updateSelectedCandidateIndex(fullyVisibleIndices().firstOrNull() ?: 0)
            }
        }
    }

    LaunchedEffect(processor.selectedCandidateIndex, processor.candidates) {
        if (processor.candidates.isEmpty()) return@LaunchedEffect
        val index = processor.selectedCandidateIndex
        val layoutInfo = lazyListState.layoutInfo
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        when {
            item == null -> lazyListState.animateScrollToItem(index)
            item.offset < layoutInfo.viewportStartOffset ->
                lazyListState.animateScrollBy((item.offset - layoutInfo.viewportStartOffset).toFloat())

            item.offset + item.size > layoutInfo.viewportEndOffset ->
                lazyListState.animateScrollBy((item.offset + item.size - layoutInfo.viewportEndOffset).toFloat())
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
                            .fillMaxHeight()
                            .widthIn(min = dimens.candidateHeight - dimens.candidateVerticalPadding * 2)
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
                                processor.selectCandidate(candidate)
                            }
                            .padding(horizontal = dimens.candidateHorizontalPadding)
                    ) {
                        Text(
                            text = candidate.joinToString("") { it.text },
                            fontSize = dimens.candidateTextSize,
                            color = colors.candidateText
                        )
                    }
                }
            }
        }
    }
}