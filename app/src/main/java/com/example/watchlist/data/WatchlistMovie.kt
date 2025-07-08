package com.example.watchlist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_movies")
data class WatchlistMovie(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val timestamp: Long = System.currentTimeMillis()
)