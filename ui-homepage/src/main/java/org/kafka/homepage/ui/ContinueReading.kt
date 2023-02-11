package org.kafka.homepage.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kafka.data.entities.Item
import com.kafka.data.entities.ItemWithRecentItem
import org.kafka.common.ImmutableList
import org.kafka.common.image.Icons
import org.kafka.common.shadowMaterial
import org.kafka.homepage.R
import ui.common.theme.theme.Dimens
import ui.common.theme.theme.textPrimary
import ui.common.theme.theme.textSecondary

@Composable
internal fun ContinueReading(
    readingList: ImmutableList<ItemWithRecentItem>,
    modifier: Modifier = Modifier,
    openItemDetail: (String) -> Unit,
    removeRecentItem: (String) -> Unit
) {
    var isInEditMode by remember { mutableStateOf(false) }

    if (readingList.items.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = stringResource(id = R.string.continue_reading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.textSecondary,
                modifier = Modifier.padding(horizontal = Dimens.Spacing20)
            )

            LazyRow(
                contentPadding = PaddingValues(end = 60.dp)
            ) {
                items(readingList.items, key = { it.item.itemId }) { continueReading ->
                    ContinueReadingItem(
                        continueReading = continueReading.item,
                        onItemClicked = { openItemDetail(continueReading.item.itemId) },
                        onItemRemoved = { removeRecentItem(it) },
                        modifier = Modifier.animateItemPlacement(),
                        isInEditMode = isInEditMode,
                        changeEditMode = { isInEditMode = it }
                    )
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.height(Dimens.Spacing12))
    }
}

@Composable
private fun ContinueReadingItem(
    continueReading: Item,
    modifier: Modifier = Modifier,
    isInEditMode: Boolean = false,
    changeEditMode: (Boolean) -> Unit = {},
    onItemRemoved: (String) -> Unit,
    onItemClicked: () -> Unit
) {

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(Dimens.Spacing12)
                .widthIn(100.dp, 286.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.Spacing08))
                    .combinedClickable(
                        onLongClick = { changeEditMode(!isInEditMode) },
                        onClick = {
                            changeEditMode(false)
                            onItemClicked()
                        }
                    )
                    .padding(Dimens.Spacing12),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CoverImage(continueReading)
                Description(continueReading, Modifier.width(286.dp))
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing12))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Spacing12)
                    .padding(horizontal = 4.dp)
                    .shadowMaterial(Dimens.Spacing12, clip = false)
                    .clip(RoundedCornerShape(Dimens.Spacing02))
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }

        RemoveRecentItemButton(isInEditMode, onItemRemoved, continueReading)
    }
}

@Composable
private fun BoxScope.RemoveRecentItemButton(
    isInEditMode: Boolean,
    onItemRemoved: (String) -> Unit,
    continueReading: Item
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = .85f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    if (isInEditMode) {
        IconButton(
            modifier = Modifier
                .size(Dimens.Spacing44)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                },
            onClick = { onItemRemoved(continueReading.itemId) }) {
            Icon(
                imageVector = Icons.XCircle,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun CoverImage(item: Item) {
    Box(
        modifier = Modifier.shadowMaterial(
            elevation = Dimens.Spacing08,
            shape = RoundedCornerShape(Dimens.Spacing04)
        )
    ) {
        AsyncImage(
            model = item.coverImage,
            contentDescription = stringResource(id = R.string.cd_cover_image),
            modifier = Modifier
                .size(64.dp, 76.dp)
                .background(MaterialTheme.colorScheme.surface),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun Description(continueReading: Item, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = continueReading.title.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(Dimens.Spacing02))
        Text(
            text = continueReading.mediaType.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Dimens.Spacing08))

        Progress()
    }
}

@Composable
private fun Progress() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = 0.2f,
            modifier = Modifier
                .height(Dimens.Spacing04)
                .width(116.dp)
                .clip(RoundedCornerShape(50))
        )
        Text(
            text = "20%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.textSecondary
        )
    }
}

