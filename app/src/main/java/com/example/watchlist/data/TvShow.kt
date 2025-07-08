package com.example.watchlist.data
// TvShow.kt

import com.google.gson.annotations.SerializedName

data class TvShow(
    val id: Int,
    val name: String, // TV shows use 'name' instead of 'title'
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("overview")
    val description: String,
    @SerializedName("first_air_date") // TV shows use 'first_air_date' instead of 'release_date'
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double
    // Add more fields as needed, similar to Movie
)