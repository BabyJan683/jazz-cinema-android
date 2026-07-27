package com.jazzcinema.app.util

object Constants {
    // Intent extras
    const val EXTRA_MOVIE_ID        = "extra_movie_id"
    const val EXTRA_MOVIE_TITLE     = "extra_movie_title"
    const val EXTRA_MOVIE_CATEGORY  = "extra_movie_category"
    const val EXTRA_MOVIE_THUMB     = "extra_movie_thumb"
    const val EXTRA_MOVIE_DRIVE_URL = "extra_movie_drive_url"
    const val EXTRA_MOVIE_YEAR      = "extra_movie_year"
    const val EXTRA_PLAY_URL        = "extra_play_url"

    // SharedPreferences
    const val PREFS_NAME            = "jazz_cinema_prefs"
    const val PREF_WATCH_HISTORY    = "watch_history"

    // Offline movie cache
    const val PREF_MOVIES_CACHE     = "movies_cache_json"
    const val PREF_CACHE_TIMESTAMP  = "movies_cache_ts"
    const val CACHE_TTL_MS          = 3_600_000L   // 1 hour

    // DB info (for display only)
    const val DB_HOST               = "sql12.freesqldatabase.com"
    const val DB_NAME               = "sql12824264"
}
