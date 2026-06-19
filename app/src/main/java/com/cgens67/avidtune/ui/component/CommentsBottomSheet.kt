package com.cgens67.avidtune.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cgens67.avidtune.R
import com.cgens67.avidtune.viewmodels.CommentsViewModel
import com.cgens67.innertube.models.response.CommentRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    videoId: String,
    onDismiss: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isError by viewModel.isError.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(videoId) {
        viewModel.loadComments(videoId)
    }

    // Detect scroll to bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= comments.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Message,
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (isLoading && comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (isError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load comments",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No comments found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                    item {
                        Spacer(Modifier.height(32.dp)) // bottom padding
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentRenderer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.authorThumbnail?.thumbnails?.lastOrNull()?.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = comment.authorText?.runs?.joinToString("") { it.text } ?: "Unknown",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = comment.publishedTimeText?.runs?.joinToString("") { it.text } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.contentText?.runs?.joinToString("") { it.text } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!comment.voteCount?.runs.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.favorite_border),
                        contentDescription = "Likes",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comment.voteCount?.runs?.joinToString("") { it.text } ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
