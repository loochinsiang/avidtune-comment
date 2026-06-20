package com.cgens67.innertube.models.response

import com.cgens67.innertube.models.Runs
import com.cgens67.innertube.models.Thumbnails
import com.cgens67.innertube.models.Thumbnail
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
    val content: EngagementPanelContent? = null
)

@Serializable
data class EngagementPanelContent(
    val sectionListRenderer: CommentsSectionListRenderer? = null
)

@Serializable
data class CommentsSectionListRenderer(
    val contents: List<CommentsSectionListContent>? = null
)

@Serializable
data class CommentsSectionListContent(
    val itemSectionRenderer: CommentsItemSectionRenderer? = null,
    val continuationItemRenderer: CommentsContinuationItemRenderer? = null
)

@Serializable
data class CommentsItemSectionRenderer(
    val contents: List<CommentsItemSectionContent>? = null
)

@Serializable
data class CommentsItemSectionContent(
    val continuationItemRenderer: CommentsContinuationItemRenderer? = null,
    val commentThreadRenderer: CommentThreadRenderer? = null
)

@Serializable
data class OnResponseReceivedEndpoint(
    val appendContinuationItemsAction: ContinuationItemsAction? = null,
    val reloadContinuationItemsCommand: ContinuationItemsAction? = null
)

@Serializable
data class ContinuationItemsAction(
    val continuationItems: List<ContinuationItem>? = null
)

@Serializable
data class ContinuationItem(
    val commentThreadRenderer: CommentThreadRenderer? = null,
    val continuationItemRenderer: CommentsContinuationItemRenderer? = null
)

@Serializable
data class CommentsContinuationItemRenderer(
    val continuationEndpoint: CommentsContinuationEndpoint? = null
)

@Serializable
data class CommentsContinuationEndpoint(
    val continuationCommand: CommentsContinuationCommand? = null
)

@Serializable
data class CommentsContinuationCommand(
    val token: String? = null
)

@Serializable
data class CommentThreadRenderer(
    val comment: Comment? = null
)

@Serializable
data class Comment(
    val commentRenderer: CommentRenderer? = null,
    val commentViewModel: CommentViewModel? = null
)

@Serializable
data class CommentViewModel(
    val commentViewModel: CommentViewModelData? = null
)

@Serializable
data class CommentViewModelData(
    val authorName: String? = null,
    val publishedTimeText: String? = null,
    val content: CommentContent? = null,
    val avatar: AvatarOuter? = null,
    val voteCount: VoteCountOuter? = null
)

@Serializable
data class CommentContent(
    val contentAsText: ContentAsText? = null
)

@Serializable
data class ContentAsText(
    val content: String? = null
)

@Serializable
data class AvatarOuter(
    val avatar: AvatarInner? = null
)

@Serializable
data class AvatarInner(
    val image: ImageOuter? = null
)

@Serializable
data class ImageOuter(
    val sources: List<Thumbnail>? = null
)

@Serializable
data class VoteCountOuter(
    val voteCountAsText: ContentAsText? = null
)

@Serializable
data class CommentRenderer(
    val authorText: Runs? = null,
    val authorThumbnail: Thumbnails? = null,
    val contentText: Runs? = null,
    val publishedTimeText: Runs? = null,
    val voteCount: Runs? = null
)

@Serializable
data class CommentItemUi(
    val authorName: String,
    val authorThumbnailUrl: String?,
    val content: String,
    val publishedTime: String,
    val voteCount: String
)
