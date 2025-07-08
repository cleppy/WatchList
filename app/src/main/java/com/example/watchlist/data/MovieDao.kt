package com.example.watchlist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Watched Movies
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchedMovie(movie: WatchedMovie)

    @Query("DELETE FROM watched_movies WHERE movieId = :movieId")
    suspend fun deleteWatchedMovie(movieId: Int)

    @Query("SELECT * FROM watched_movies ORDER BY timestamp DESC")
    fun getAllWatchedMovies(): Flow<List<WatchedMovie>>

    @Query("SELECT EXISTS(SELECT 1 FROM watched_movies WHERE movieId = :movieId LIMIT 1)")
    suspend fun isMovieWatched(movieId: Int): Boolean

    // Watchlist Movies
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistMovie(movie: WatchlistMovie)

    @Query("DELETE FROM watchlist_movies WHERE movieId = :movieId")
    suspend fun deleteWatchlistMovie(movieId: Int)

    @Query("SELECT * FROM watchlist_movies ORDER BY timestamp DESC")
    fun getAllWatchlistMovies(): Flow<List<WatchlistMovie>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE movieId = :movieId LIMIT 1)")
    suspend fun isMovieOnWatchlist(movieId: Int): Boolean
}