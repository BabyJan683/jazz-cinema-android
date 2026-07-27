package com.jazzcinema.app.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.jazzcinema.app.adapter.MovieGridAdapter
import com.jazzcinema.app.databinding.FragmentSearchBinding
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.ui.detail.MovieDetailActivity
import com.jazzcinema.app.ui.home.HomeUiState
import com.jazzcinema.app.ui.home.HomeViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var movieAdapter: MovieGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeState()

        binding.etSearch.doAfterTextChanged { text ->
            val q = text?.toString()?.trim() ?: ""
            if (q.length >= 2) viewModel.search(requireContext(), q)
            else if (q.isEmpty()) {
                movieAdapter.submitList(emptyList())
                binding.tvHint.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieGridAdapter(::onMovieClick)
        binding.rvResults.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = movieAdapter
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is HomeUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvHint.visibility      = View.GONE
                }
                is HomeUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val allMovies = state.categories.flatMap { it.movies }.distinctBy { it.id }
                    movieAdapter.submitList(allMovies)
                    binding.tvHint.visibility =
                        if (allMovies.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvHint.text =
                        if (allMovies.isEmpty()) "No results found" else ""
                }
                is HomeUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun onMovieClick(movie: Movie) {
        MovieDetailActivity.start(requireContext(), movie)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
