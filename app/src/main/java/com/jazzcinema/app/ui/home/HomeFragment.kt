package com.jazzcinema.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jazzcinema.app.adapter.CategoryAdapter
import com.jazzcinema.app.databinding.FragmentHomeBinding
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.ui.detail.MovieDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeState()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh(requireContext())
        }

        // Load movies — cache is already populated by SplashActivity
        viewModel.loadMovies(requireContext())
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(::onMovieClick)
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            val query = text?.toString()?.trim() ?: ""
            if (query.isEmpty()) viewModel.loadMovies(requireContext())
            else viewModel.search(requireContext(), query)
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is HomeUiState.Loading -> {
                    binding.progressBar.visibility  = View.VISIBLE
                    binding.rvCategories.visibility = View.GONE
                    binding.layoutError.visibility  = View.GONE
                }
                is HomeUiState.Success -> {
                    binding.progressBar.visibility  = View.GONE
                    binding.layoutError.visibility  = View.GONE
                    binding.rvCategories.visibility = View.VISIBLE
                    categoryAdapter.submitList(state.categories)
                }
                is HomeUiState.Error -> {
                    binding.progressBar.visibility  = View.GONE
                    binding.rvCategories.visibility = View.GONE
                    binding.layoutError.visibility  = View.VISIBLE
                    binding.tvError.text            = state.message
                    binding.btnRetry.setOnClickListener {
                        viewModel.refresh(requireContext())
                    }
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
