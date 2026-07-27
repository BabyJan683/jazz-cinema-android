package com.jazzcinema.app.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.jazzcinema.app.databinding.ActivityPlayerBinding
import com.jazzcinema.app.util.Constants

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var isPlaying = true

    companion object {
        fun start(context: Context, playUrl: String, title: String = "") {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(Constants.EXTRA_PLAY_URL,    playUrl)
                putExtra(Constants.EXTRA_MOVIE_TITLE, title)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on and go full-screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val title = intent.getStringExtra(Constants.EXTRA_MOVIE_TITLE) ?: ""
        binding.tvTitle.text = title

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun initPlayer() {
        val url = intent.getStringExtra(Constants.EXTRA_PLAY_URL) ?: run { finish(); return }

        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(url))
            exo.seekTo(playbackPosition)
            exo.playWhenReady = isPlaying
            exo.prepare()

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    binding.progressBuffering.visibility =
                        if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    override fun onStop() {
        super.onStop()
        player?.let {
            playbackPosition = it.currentPosition
            isPlaying = it.playWhenReady
            it.release()
        }
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
