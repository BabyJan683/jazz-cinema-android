package com.jazzcinema.app.database

import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.model.MovieCategory

object CategoryBuilder {

    private val FIXED_ORDER = listOf(
        "South", "Bollywood", "Hollywood",
        "Pakistani", "Turkish", "Urdu Dubbed", "Trending"
    )

    fun build(movies: List<Movie>): List<MovieCategory> {
        if (movies.isEmpty()) return emptyList()

        val byCategory = movies.groupBy { it.category.trim() }
        val result     = mutableListOf<MovieCategory>()

        // Recently Added — newest 20 by created_at
        val recent = movies.sortedByDescending { it.createdAt }.take(20)
        if (recent.isNotEmpty()) result += MovieCategory("Recently Added", recent)

        // Latest Movies — highest release_year, top 20
        val latest = movies.filter { it.releaseYear > 0 }
                           .sortedByDescending { it.releaseYear }
                           .take(20)
        if (latest.isNotEmpty()) result += MovieCategory("Latest Movies", latest)

        // Fixed category order
        for (cat in FIXED_ORDER) {
            val key  = byCategory.keys.firstOrNull { it.equals(cat, ignoreCase = true) } ?: continue
            val list = byCategory[key] ?: continue
            if (list.isNotEmpty() && result.none { it.name.equals(cat, ignoreCase = true) }) {
                result += MovieCategory(key, list)
            }
        }

        // Any remaining categories not already added
        for ((name, list) in byCategory) {
            if (result.none { it.name.equals(name, ignoreCase = true) } && list.isNotEmpty()) {
                result += MovieCategory(name, list)
            }
        }

        return result
    }
}
