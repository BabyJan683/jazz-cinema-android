package com.jazzcinema.app.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jazzcinema.app.BuildConfig
import com.jazzcinema.app.databinding.FragmentProfileBinding
import com.jazzcinema.app.util.Constants

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvVersion.text = "Jazz Cinema v${BuildConfig.VERSION_NAME}"

        // Show database connection info (no API server)
        val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val cacheTs = prefs.getLong(Constants.PREF_CACHE_TIMESTAMP, 0L)
        val cacheAge = if (cacheTs > 0L) {
            val mins = ((System.currentTimeMillis() - cacheTs) / 60_000).toInt()
            when {
                mins < 1   -> "just now"
                mins < 60  -> "$mins min ago"
                else       -> "${mins / 60} h ago"
            }
        } else "not cached yet"

        binding.tvApiUrl.text =
            "DB Host: ${Constants.DB_HOST}\n" +
            "Database: ${Constants.DB_NAME}\n" +
            "Cache refreshed: $cacheAge"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
