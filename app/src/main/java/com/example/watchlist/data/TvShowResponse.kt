package com.example.watchlist.data

import com.google.gson.annotations.SerializedName

data class TvShowResponse(
    val page: Int,
    val results: List<TvShow>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)
