package com.jazzcinema.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jazzcinema.app.MainActivity
import com.jazzcinema.app.databinding.ActivitySplashBinding
import com.jazzcinema.app.repository.MovieRepository
import com.jazzcinema.app.util.Constants
import kotlinx.coroutines.launch

/**
 * Splash screen shown on every cold start.
 *
 * Strategy:
 *  - If the local cache is fresh (< 1 h old): start MainActivity immediately.
 *    A silent background refresh is kicked off so data stays current.
 *  - If the cache is stale / missing: fetch all movies from MySQL, save to cache,
 *    then start MainActivity.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        val cached = MovieRepository.loadCache(prefs)

        if (cached != null) {
            // Cache is fresh — go immediately, refresh silently in background
            goToMain()
        } else {
            // Cache is stale or missing — show progress and fetch from DB
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.visibility    = View.VISIBLE

            lifecycleScope.launch {
                try {
                    binding.tvStatus.text = "Connecting to database…"
                    MovieRepository.preloadAndCache(this@SplashActivity)
                    binding.tvStatus.text = "Done!"
                } catch (e: Exception) {
                    // Even on error, continue to main so the user can retry via pull-to-refresh
                    binding.tvStatus.text = "Could not reach database. Continuing offline…"
                    kotlinx.coroutines.delay(1500)
                } finally {
                    goToMain()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
