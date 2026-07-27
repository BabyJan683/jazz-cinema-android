package com.jazzcinema.app.model

import com.google.gson.annotations.SerializedName

data class MovieCategory(
    @SerializedName("name")   val name: String = "",
    @SerializedName("movies") val movies: List<Movie> = emptyList()
)
