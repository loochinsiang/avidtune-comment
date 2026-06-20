package com.cgens67.avidtune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cgens67.innertube.YouTube
import com.cgens67.innertube.models.response.CommentItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor() : ViewModel() {
    private val _comments = MutableStateFlow<List<CommentItemUi>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _isError = MutableStateFlow(false)
    val isError = _isError.asStateFlow()

    private var continuationToken: String? = null
    private var isFetching = false

    fun loadComments(videoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _isError.value = false
            _comments.value = emptyList()
            YouTube.commentsInitial(videoId).onSuccess { (initialComments, nextToken) ->
                _comments.value = initialComments
                continuationToken = nextToken
            }.onFailure {
                _isError.value = true
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (isFetching || continuationToken == null) return
        isFetching = true
        val token = continuationToken ?: return
        viewModelScope.launch(Dispatchers.IO) {
            YouTube.commentsContinuation(token).onSuccess { (moreComments, nextToken) ->
                _comments.value = _comments.value + moreComments
                continuationToken = nextToken
            }
            isFetching = false
        }
    }
}
