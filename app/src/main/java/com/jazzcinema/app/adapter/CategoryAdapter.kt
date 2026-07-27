package com.jazzcinema.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jazzcinema.app.databinding.ItemCategoryRowBinding
import com.jazzcinema.app.model.Movie
import com.jazzcinema.app.model.MovieCategory

class CategoryAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<MovieCategory, CategoryAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MovieCategory>() {
            override fun areItemsTheSame(o: MovieCategory, n: MovieCategory) = o.name == n.name
            override fun areContentsTheSame(o: MovieCategory, n: MovieCategory) = o == n
        }
    }

    // Pool is shared across all horizontal RecyclerViews for performance
    private val recycledViewPool = RecyclerView.RecycledViewPool()

    inner class ViewHolder(private val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val movieAdapter = MovieAdapter(onMovieClick)

        init {
            binding.rvMovies.apply {
                adapter = movieAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setRecycledViewPool(recycledViewPool)
                setHasFixedSize(true)
            }
        }

        fun bind(category: MovieCategory) {
            binding.tvCategoryName.text = category.name
            movieAdapter.submitList(category.movies)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemCategoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
