package com.crest247.flickbopomofo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crest247.flickbopomofo.core.theme.LocalKeyboardColors
import com.crest247.flickbopomofo.core.theme.LocalKeyboardDimens

@Composable
fun StandardCategoryTabs(
    tabs: List<KeyContent>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalKeyboardColors.current
    val dimens = LocalKeyboardDimens.current

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = colors.candidateBackground,
        contentColor = colors.candidateText,
        indicator = { tabPositions ->
            if (selectedIndex in tabPositions.indices) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedIndex])
                        .fillMaxHeight()
                ) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = colors.candidateSelectedBorder
                    )
                }
            }
        },
        divider = {},
        modifier = modifier.height(dimens.candidateHeight)
    ) {
        tabs.forEachIndexed { index, tabContent ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.fillMaxHeight()
            ) {
                when (tabContent) {
                    is KeyContent.Text -> {
                        KeyboardText(text = tabContent.text, fontSize = dimens.keyTextSize)
                    }

                    is KeyContent.Icon -> {
                        androidx.compose.material3.Icon(
                            imageVector = tabContent.icon,
                            contentDescription = null,
                            tint = colors.candidateText
                        )
                    }
                }
            }
        }
    }
}