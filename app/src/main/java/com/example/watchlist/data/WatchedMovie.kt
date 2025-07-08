package com.example.watchlist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_movies")
data class WatchedMovie(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterPath: String?,
    val mediaType: String,
    val timestamp: Long = System.currentTimeMillis()
)