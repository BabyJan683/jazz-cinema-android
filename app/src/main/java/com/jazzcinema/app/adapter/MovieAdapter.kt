package com.jazzcinema.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.jazzcinema.app.R
import com.jazzcinema.app.databinding.ItemMovieHorizontalBinding
import com.jazzcinema.app.model.Movie

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(o: Movie, n: Movie) = o.id == n.id
            override fun areContentsTheSame(o: Movie, n: Movie) = o == n
        }
    }

    inner class ViewHolder(private val binding: ItemMovieHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvYear.text = if (movie.releaseYear > 0) movie.releaseYear.toString() else ""

            Glide.with(binding.root.context)
                .load(movie.thumbnailUrl.takeIf { it.isNotBlank() })
                .placeholder(R.drawable.bg_placeholder)
                .error(R.drawable.bg_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.ivThumbnail)

            binding.root.setOnClickListener { onMovieClick(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemMovieHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
