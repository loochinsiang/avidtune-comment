package com.cgens67.avidtune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cgens67.avidtune.db.MusicDatabase
import com.cgens67.avidtune.db.entities.Artist
import com.cgens67.avidtune.db.entities.Song
import com.cgens67.avidtune.db.entities.SongWithStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class InsightViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {
    private val _topSongs = MutableStateFlow<List<Song>>(emptyList())
    val topSongs = _topSongs.asStateFlow()

    private val _topSongStats = MutableStateFlow<SongWithStats?>(null)
    val topSongStats = _topSongStats.asStateFlow()

    private val _topArtists = MutableStateFlow<List<Artist>>(emptyList())
    val topArtists = _topArtists.asStateFlow()

    private val _totalMinutes = MutableStateFlow(0L)
    val totalMinutes = _totalMinutes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadInsightData()
    }

    private fun loadInsightData() {
        viewModelScope.launch {
            val oneYearAgo = LocalDateTime.now().minusYears(1).toInstant(ZoneOffset.UTC).toEpochMilli()
            
            // Get top 5 songs with their full details (so we can get artist names, album, etc.)
            val songs = database.mostPlayedSongs(fromTimeStamp = oneYearAgo, limit = 5).first()
            _topSongs.value = songs
            
            // Get stats for the top song (for play count)
            val stats = database.mostPlayedSongsStats(fromTimeStamp = oneYearAgo, limit = 5).first()
            _topSongStats.value = stats.firstOrNull()
            
            // Get top 5 artists
            val artists = database.mostPlayedArtists(fromTimeStamp = oneYearAgo, limit = 5).first()
            _topArtists.value = artists.filter { it.artist.isYouTubeArtist }
            
            // Get total listening time (sum of all top songs play times within the year)
            val allStats = database.mostPlayedSongsStats(fromTimeStamp = oneYearAgo, limit = 10000).first()
            val totalMillis = allStats.sumOf { it.timeListened ?: 0L }
            _totalMinutes.value = totalMillis / 60000L
            
            _isLoading.value = false
        }
    }
}