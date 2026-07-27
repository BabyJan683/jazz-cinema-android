package com.jazzcinema.app.ui.library

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jazzcinema.app.adapter.MovieGridAdapter
import com.jazzcinema.app.databinding.FragmentLibraryBinding
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.ui.detail.MovieDetailActivity
import com.jazzcinema.app.util.Constants

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieGridAdapter(::onMovieClick)
        binding.rvHistory.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = movieAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(Constants.PREF_WATCH_HISTORY, "[]") ?: "[]"
        val type = object : TypeToken<List<Movie>>() {}.type
        val history: List<Movie> = Gson().fromJson(json, type) ?: emptyList()

        movieAdapter.submitList(history)
        binding.layoutEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        binding.rvHistory.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun onMovieClick(movie: Movie) {
        MovieDetailActivity.start(requireContext(), movie)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
