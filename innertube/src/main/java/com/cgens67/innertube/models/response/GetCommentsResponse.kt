package com.cgens67.innertube.models.response

import com.cgens67.innertube.models.Runs
import com.cgens67.innertube.models.Thumbnails
import kotlinx.serialization.Serializable

@Serializable
data class GetCommentsResponse(
    val engagementPanels: List<EngagementPanel>? = null,
    val onResponseReceivedEndpoints: List<OnResponseReceivedEndpoint>? = null
)

@Serializable
data class EngagementPanel(
    val engagementPanelSectionListRenderer: EngagementPanelSectionListRenderer? = null
)

@Serializable
data class EngagementPanelSectionListRenderer(
    val panelIdentifier: String?,
    val content: EngagementPanelContent?
)

@Serializable
data class EngagementPanelContent(
    val sectionListRenderer: CommentsSectionListRenderer?
)

@Serializable
data class CommentsSectionListRenderer(
    val contents: List<CommentsSectionListContent>?
)

@Serializable
data class CommentsSectionListContent(
    val itemSectionRenderer: CommentsItemSectionRenderer?
)

@Serializable
data class CommentsItemSectionRenderer(
    val contents: List<CommentsItemSectionContent>?
)

@Serializable
data class CommentsItemSectionContent(
    val continuationItemRenderer: CommentsContinuationItemRenderer?
)

@Serializable
data class OnResponseReceivedEndpoint(
    val appendContinuationItemsAction: ContinuationItemsAction? = null,
    val reloadContinuationItemsCommand: ContinuationItemsAction? = null
)

@Serializable
data class ContinuationItemsAction(
    val continuationItems: List<ContinuationItem>?
)

@Serializable
data class ContinuationItem(
    val commentThreadRenderer: CommentThreadRenderer? = null,
    val continuationItemRenderer: CommentsContinuationItemRenderer? = null
)

@Serializable
data class CommentsContinuationItemRenderer(
    val continuationEndpoint: CommentsContinuationEndpoint?
)

@Serializable
data class CommentsContinuationEndpoint(
    val continuationCommand: CommentsContinuationCommand?
)

@Serializable
data class CommentsContinuationCommand(
    val token: String?
)

@Serializable
data class CommentThreadRenderer(
    val comment: Comment?
)

@Serializable
data class Comment(
    val commentRenderer: CommentRenderer?
)

@Serializable
data class CommentRenderer(
    val authorText: Runs? = null,
    val authorThumbnail: Thumbnails? = null,
    val contentText: Runs? = null,
    val publishedTimeText: Runs? = null,
    val voteCount: Runs? = null
)
