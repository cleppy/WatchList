// MovieViewModel.kt
package com.example.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.data.RetrofitClient
import com.example.watchlist.data.AppDatabase
import com.example.watchlist.data.WatchedMovie
import com.example.watchlist.data.WatchlistMovie
import com.example.watchlist.data.Movie
import com.example.watchlist.data.TvShow // Import the new TvShow data class
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val API_KEY = "YOUR_API_KEY"
    private val movieDao = AppDatabase.getDatabase(application).movieDao()

    // Movie related states
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()

    private val _movieSearchResults = MutableStateFlow<List<Movie>>(emptyList())
    val movieSearchResults: StateFlow<List<Movie>> = _movieSearchResults.asStateFlow() // Renamed for clarity

    // TV Show related states
    private val _popularTvShows = MutableStateFlow<List<TvShow>>(emptyList())
    val popularTvShows: StateFlow<List<TvShow>> = _popularTvShows.asStateFlow()

    private val _tvShowSearchResults = MutableStateFlow<List<TvShow>>(emptyList())
    val tvShowSearchResults: StateFlow<List<TvShow>> = _tvShowSearchResults.asStateFlow()

    // Combined search results (for UI to display both)
    private val _combinedSearchResults = MutableStateFlow<List<Any>>(emptyList()) // Can hold both Movie and TvShow
    val combinedSearchResults: StateFlow<List<Any>> = _combinedSearchResults.asStateFlow()


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Local database states
    private val _watchedMovies = MutableStateFlow<List<WatchedMovie>>(emptyList())
    val watchedMovies: StateFlow<List<WatchedMovie>> = _watchedMovies.asStateFlow()

    private val _watchlistMovies = MutableStateFlow<List<WatchlistMovie>>(emptyList())
    val watchlistMovies: StateFlow<List<WatchlistMovie>> = _watchlistMovies.asStateFlow()

    init {
        fetchPopularMovies()
        fetchPopularTvShows() // Fetch popular TV shows on init

        viewModelScope.launch {
            movieDao.getAllWatchedMovies().collectLatest { _watchedMovies.value = it }
        }
        viewModelScope.launch {
            movieDao.getAllWatchlistMovies().collectLatest { _watchlistMovies.value = it }
        }
    }

    private fun fetchPopularMovies() {
        viewModelScope.launch {
            try {
                _popularMovies.value = RetrofitClient.apiService.getPopularMovies(API_KEY).results
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchPopularTvShows() {
        viewModelScope.launch {
            try {
                _popularTvShows.value = RetrofitClient.apiService.getPopularTvShows(API_KEY).results
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun searchAll(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _combinedSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val movieResponse = RetrofitClient.apiService.searchMovies(API_KEY, query)
                val tvShowResponse = RetrofitClient.apiService.searchTvShows(API_KEY, query)

                // Combine results from movies and TV shows
                val combinedList = mutableListOf<Any>().apply {
                    addAll(movieResponse.results)
                    addAll(tvShowResponse.results)
                    // Optional: Sort or filter results as needed
                }
                _combinedSearchResults.value = combinedList
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Handle error
            }
        }
    }

    // You can keep the separate searchMovies if you want to distinguish between movie and TV show searches in UI
    fun searchMoviesOnly(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _movieSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _movieSearchResults.value = RetrofitClient.apiService.searchMovies(API_KEY, query).results
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun searchTvShowsOnly(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _tvShowSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _tvShowSearchResults.value = RetrofitClient.apiService.searchTvShows(API_KEY, query).results
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Local Database Functions (unchanged, but now need to handle both Movie and TvShow types) ---
    // This is a crucial point: Your WatchedMovie and WatchlistMovie entities currently only support Movie fields.
    // You'll need to decide how to store TV shows in your local database.

    // Option 1: Create separate WatchedTvShow and WatchlistTvShow entities/tables.
    // Option 2: Create a generic 'MediaItem' entity that can hold both movie and TV show data,
    //           and distinguish them by a 'type' field (e.g., "movie", "tv"). This is often more flexible.

    // For simplicity, let's go with Option 2 for now, or adapt existing for Movies only.
    // If you want to store TvShows in Watched/Watchlist, you'd need to modify WatchedMovie/WatchlistMovie
    // to accept either Movie or TvShow properties, or create new entities.

    // For the current structure, if you want to add a TvShow to "watched" or "watchlist",
    // you need to convert it into a WatchedMovie/WatchlistMovie format.
    // This means WatchedMovie/WatchlistMovie might need a 'mediaType' field (e.g., "movie" or "tv").

    // Let's modify add/remove functions to handle 'Any' type for now,
    // and then guide on updating the local database models.

    fun addMediaToWatched(mediaItem: Any) { // Can be Movie or TvShow
        viewModelScope.launch {
            when (mediaItem) {
                is Movie -> {
                    // mediaType'ı "movie" olarak belirtiyoruz
                    val watched = WatchedMovie(mediaItem.id, mediaItem.title, mediaItem.posterPath, "movie")
                    movieDao.insertWatchedMovie(watched)
                }
                is TvShow -> {
                    val watched = WatchedMovie(mediaItem.id, mediaItem.name, mediaItem.posterPath, "tv")
                    movieDao.insertWatchedMovie(watched)
                }
            }
        }
    }

    fun removeMediaFromWatched(mediaId: Int) { // Need to know if it's movie or TV to remove correctly if using different tables
        viewModelScope.launch {
            movieDao.deleteWatchedMovie(mediaId) // Assumes single table for simplicity
        }
    }

    fun addMediaToWatchlist(mediaItem: Any) {
        viewModelScope.launch {
            when (mediaItem) {
                is Movie -> {
                    val watchlist = WatchlistMovie(mediaItem.id, mediaItem.title, mediaItem.posterPath, "movie")
                    movieDao.insertWatchlistMovie(watchlist)
                }
                is TvShow -> {
                    val watchlist = WatchlistMovie(mediaItem.id, mediaItem.name, mediaItem.posterPath, "tv")
                    movieDao.insertWatchlistMovie(watchlist)
                }
            }
        }
    }

    fun removeMediaFromWatchlist(mediaId: Int) {
        viewModelScope.launch {
            movieDao.deleteWatchlistMovie(mediaId) // Assumes single table for simplicity
        }
    }

    suspend fun isMediaWatched(mediaId: Int): Boolean { // Renamed from isMovieWatched
        return movieDao.isMovieWatched(mediaId) // Still checks movie table for now
    }

    suspend fun isMediaOnWatchlist(mediaId: Int): Boolean { // Renamed from isMovieOnWatchlist
        return movieDao.isMovieOnWatchlist(mediaId) // Still checks watchlist table for now
    }
}