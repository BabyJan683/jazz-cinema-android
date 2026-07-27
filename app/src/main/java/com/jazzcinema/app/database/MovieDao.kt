package com.jazzcinema.app.database

import com.jazzcinema.app.model.Movie
import java.sql.DriverManager
import java.sql.PreparedStatement

/**
 * Direct MySQL queries — always call from a background thread (Dispatchers.IO).
 */
object MovieDao {

    init {
        // Register MySQL JDBC driver once at class load
        try {
            Class.forName("com.mysql.jdbc.Driver")
        } catch (_: ClassNotFoundException) { /* already registered */ }
    }

    private fun connection() =
        DriverManager.getConnection(DbConfig.JDBC_URL, DbConfig.USER, DbConfig.PASSWORD)

    /** Fetch all movies, optionally filtered by title. */
    fun fetchAll(search: String? = null): List<Movie> {
        val sql = if (search.isNullOrBlank()) {
            "SELECT id, category, title, thumbnail_url, drive_url, play_url, release_year, created_at " +
            "FROM Movies ORDER BY created_at DESC"
        } else {
            "SELECT id, category, title, thumbnail_url, drive_url, play_url, release_year, created_at " +
            "FROM Movies WHERE title LIKE ? ORDER BY created_at DESC"
        }

        connection().use { conn ->
            val stmt: java.sql.Statement
            val rs: java.sql.ResultSet

            if (search.isNullOrBlank()) {
                stmt = conn.createStatement()
                rs   = stmt.executeQuery(sql)
            } else {
                val ps: PreparedStatement = conn.prepareStatement(sql)
                ps.setString(1, "%${search.trim()}%")
                stmt = ps
                rs   = ps.executeQuery()
            }

            rs.use {
                val list = mutableListOf<Movie>()
                while (it.next()) {
                    list += Movie(
                        id           = it.getInt("id"),
                        title        = it.getString("title")        ?: "",
                        category     = it.getString("category")     ?: "",
                        thumbnailUrl = it.getString("thumbnail_url") ?: "",
                        driveUrl     = it.getString("drive_url")    ?: "",
                        playUrl      = it.getString("play_url")     ?: "",
                        releaseYear  = it.getInt("release_year"),
                        createdAt    = it.getString("created_at")   ?: ""
                    )
                }
                return list
            }
        }
    }

    /** Fetch the N most recently added movies. */
    fun fetchRecent(limit: Int = 20): List<Movie> {
        val sql =
            "SELECT id, category, title, thumbnail_url, drive_url, play_url, release_year, created_at " +
            "FROM Movies ORDER BY created_at DESC LIMIT ?"

        connection().use { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setInt(1, limit)
                ps.executeQuery().use { rs ->
                    val list = mutableListOf<Movie>()
                    while (rs.next()) {
                        list += Movie(
                            id           = rs.getInt("id"),
                            title        = rs.getString("title")        ?: "",
                            category     = rs.getString("category")     ?: "",
                            thumbnailUrl = rs.getString("thumbnail_url") ?: "",
                            driveUrl     = rs.getString("drive_url")    ?: "",
                            playUrl      = rs.getString("play_url")     ?: "",
                            releaseYear  = rs.getInt("release_year"),
                            createdAt    = rs.getString("created_at")   ?: ""
                        )
                    }
                    return list
                }
            }
        }
    }
}
