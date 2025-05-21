package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.ItemMovieAdminLayoutBinding
import com.example.movieticketsapp.model.MovieAdmin

class ItemMovieAdminAdapter(
    private var listMovie: List<MovieAdmin>,
    private val onItemClick: (MovieAdmin) -> Unit,
    private val onEdit: (MovieAdmin) -> Unit,
    private val onDelete: (MovieAdmin) -> Unit
) : RecyclerView.Adapter<ItemMovieAdminAdapter.MyViewHolder>() {

    private var expandedPosition = -1

    // Submit new data to the adapter
    fun submitList(newList: List<MovieAdmin>) {
        listMovie = newList
        notifyDataSetChanged()  // Notify the adapter to refresh with the new data
    }

    inner class MyViewHolder(val binding: ItemMovieAdminLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMovieAdminLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = listMovie.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val movie = listMovie[position]
        val context = holder.itemView.context

        Glide.with(context)
            .load(movie.img_movie)
            .into(holder.binding.imgMovie)

        holder.binding.tvTitleMovie.text = movie.title
        holder.binding.tvDirectorMovie.text = "Director: ${movie.director}"
        holder.binding.tvGenresMovie.text = "Genres: ${movie.genreNames.joinToString()}"
        holder.binding.tvCastMovie.text = "Cast: ${movie.castNames.joinToString()}"

        if (expandedPosition == position) {
            holder.binding.expandableLayout.visibility = View.VISIBLE
            holder.binding.btnExpandDetails.text = "Show Less"
        } else {
            holder.binding.expandableLayout.visibility = View.GONE
            holder.binding.btnExpandDetails.text = "Show More"
        }

        holder.binding.btnExpandDetails.setOnClickListener {
            expandedPosition = if (expandedPosition == position) {
                -1
            } else {
                position
            }
            notifyDataSetChanged()
        }

        holder.binding.tvRatingMovie.text = "Rating: ${movie.rating}/5"
        holder.binding.tvSynopsisMovie.text = "Synopsis: ${movie.synopsis}"

        val imageAdapter = ItemPhotosApdater(movie.list_photos)
        holder.binding.recyclerViewPhotos.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.binding.recyclerViewPhotos.adapter = imageAdapter

        holder.binding.root.setOnClickListener {
            onItemClick(movie)
        }

        holder.binding.btnEditMovie.setOnClickListener { onEdit(movie) }
        holder.binding.btnDeleteMovie.setOnClickListener { onDelete(movie) }
    }
}
