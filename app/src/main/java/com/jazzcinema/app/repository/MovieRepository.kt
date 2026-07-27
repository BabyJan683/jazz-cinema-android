package com.jazzcinema.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jazzcinema.app.database.CategoryBuilder
import com.jazzcinema.app.database.MovieDao
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.model.MovieCategory
import com.jazzcinema.app.network.JazzDriveResolver
import com.jazzcinema.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MovieRepository {

    private val gson = Gson()
    private val movieListType = object : TypeToken<List<Movie>>() {}.type

    // ── Offline cache ──────────────────────────────────────────────────────

    /** Save a flat list of movies to SharedPreferences as JSON. */
    fun saveCache(prefs: SharedPreferences, movies: List<Movie>) {
        prefs.edit()
            .putString(Constants.PREF_MOVIES_CACHE, gson.toJson(movies))
            .putLong(Constants.PREF_CACHE_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Load movies from the local cache.
     * Returns null if the cache is empty or older than [Constants.CACHE_TTL_MS].
     */
    fun loadCache(prefs: SharedPreferences): List<Movie>? {
        val ts = prefs.getLong(Constants.PREF_CACHE_TIMESTAMP, 0L)
        if (System.currentTimeMillis() - ts > Constants.CACHE_TTL_MS) return null
        val json = prefs.getString(Constants.PREF_MOVIES_CACHE, null) ?: return null
        return runCatching<List<Movie>> { gson.fromJson(json, movieListType) }.getOrNull()
    }

    /**
     * Returns movies grouped by category.
     * Tries the local cache first; falls back to the live MySQL database.
     * Runs on the IO thread.
     */
    suspend fun getMovies(
        context: Context,
        search: String? = null,
        forceRefresh: Boolean = false
    ): List<MovieCategory> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        // For search queries always go live; never serve stale search results from cache
        val movies: List<Movie> = if (!search.isNullOrBlank()) {
            MovieDao.fetchAll(search)
        } else if (!forceRefresh) {
            loadCache(prefs) ?: MovieDao.fetchAll(null).also { saveCache(prefs, it) }
        } else {
            MovieDao.fetchAll(null).also { saveCache(prefs, it) }
        }

        CategoryBuilder.build(movies)
    }

    /**
     * Preload ALL movies from MySQL, persist to cache, and return them flat.
     * Called by SplashActivity once on first launch or when cache is stale.
     */
    suspend fun preloadAndCache(context: Context): List<Movie> =
        withContext(Dispatchers.IO) {
            val movies = MovieDao.fetchAll(null)
            val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            saveCache(prefs, movies)
            movies
        }

    /** Returns latest N movies flat. Runs on IO thread. */
    suspend fun getRecentMovies(limit: Int = 20): List<Movie> =
        withContext(Dispatchers.IO) {
            MovieDao.fetchRecent(limit)
        }

    /** Resolves a Jazz Drive share URL → direct video URL. Runs on IO thread. */
    suspend fun resolveJazzDrive(shareUrl: String): String =
        withContext(Dispatchers.IO) {
            JazzDriveResolver.resolve(shareUrl)
        }
}
