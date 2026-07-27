package com.jazzcinema.app.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")         val id: Int = 0,
    @SerializedName("title")      val title: String = "",
    @SerializedName("category")   val category: String = "",
    @SerializedName("thumbnailUrl") val thumbnailUrl: String = "",
    @SerializedName("driveUrl")   val driveUrl: String = "",
    @SerializedName("playUrl")    val playUrl: String = "",
    @SerializedName("releaseYear") val releaseYear: Int = 0,
    @SerializedName("createdAt")  val createdAt: String = ""
)
