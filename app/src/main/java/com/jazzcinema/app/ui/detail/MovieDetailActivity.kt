package com.jazzcinema.app.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jazzcinema.app.R
import com.jazzcinema.app.databinding.ActivityMovieDetailBinding
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.repository.MovieRepository
import com.jazzcinema.app.ui.player.PlayerActivity
import com.jazzcinema.app.util.Constants
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private var movie: Movie? = null

    companion object {
        fun start(context: Context, movie: Movie) {
            context.startActivity(Intent(context, MovieDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_MOVIE_ID,        movie.id)
                putExtra(Constants.EXTRA_MOVIE_TITLE,     movie.title)
                putExtra(Constants.EXTRA_MOVIE_CATEGORY,  movie.category)
                putExtra(Constants.EXTRA_MOVIE_THUMB,     movie.thumbnailUrl)
                putExtra(Constants.EXTRA_MOVIE_DRIVE_URL, movie.driveUrl)
                putExtra(Constants.EXTRA_MOVIE_YEAR,      movie.releaseYear)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movie = Movie(
            id           = intent.getIntExtra(Constants.EXTRA_MOVIE_ID, 0),
            title        = intent.getStringExtra(Constants.EXTRA_MOVIE_TITLE)     ?: "",
            category     = intent.getStringExtra(Constants.EXTRA_MOVIE_CATEGORY)  ?: "",
            thumbnailUrl = intent.getStringExtra(Constants.EXTRA_MOVIE_THUMB)     ?: "",
            driveUrl     = intent.getStringExtra(Constants.EXTRA_MOVIE_DRIVE_URL) ?: "",
            releaseYear  = intent.getIntExtra(Constants.EXTRA_MOVIE_YEAR, 0)
        )

        setupToolbar()
        populateUi()
        binding.btnPlay.setOnClickListener { resolveAndPlay() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun populateUi() {
        val m = movie ?: return
        binding.tvTitle.text    = m.title
        binding.tvCategory.text = m.category
        binding.tvYear.text     = if (m.releaseYear > 0) m.releaseYear.toString() else "—"

        Glide.with(this)
            .load(m.thumbnailUrl.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.bg_placeholder)
            .error(R.drawable.bg_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivThumbnail)
    }

    private fun resolveAndPlay() {
        val m        = movie ?: return
        val driveUrl = m.driveUrl.takeIf { it.isNotBlank() } ?: run {
            Toast.makeText(this, "No video link available", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPlay.isEnabled          = false
        binding.progressResolve.visibility  = View.VISIBLE
        binding.tvResolving.visibility      = View.VISIBLE

        lifecycleScope.launch {
            try {
                val playUrl = MovieRepository.resolveJazzDrive(driveUrl)
                saveToHistory(m)
                PlayerActivity.start(this@MovieDetailActivity, playUrl, m.title)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MovieDetailActivity,
                    "Could not fetch stream: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnPlay.isEnabled          = true
                binding.progressResolve.visibility  = View.GONE
                binding.tvResolving.visibility      = View.GONE
            }
        }
    }

    private fun saveToHistory(movie: Movie) {
        val prefs   = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json    = prefs.getString(Constants.PREF_WATCH_HISTORY, "[]") ?: "[]"
        val type    = object : TypeToken<MutableList<Movie>>() {}.type
        val history: MutableList<Movie> = Gson().fromJson(json, type) ?: mutableListOf()

        history.removeIf { it.id == movie.id }
        history.add(0, movie)
        if (history.size > 50) history.subList(50, history.size).clear()

        prefs.edit()
            .putString(Constants.PREF_WATCH_HISTORY, Gson().toJson(history))
            .apply()
    }
}
