package com.example.movieticketsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieticketsapp.adapter.ImagePhotoAdminAdapter
import com.example.movieticketsapp.databinding.ItemMovieAdminLayoutBinding
import com.example.movieticketsapp.model.MovieAdmin

class ItemMovieAdminAdapter(
    private val listMovie: List<MovieAdmin>,
    private val onItemClick: (MovieAdmin) -> Unit
) : RecyclerView.Adapter<ItemMovieAdminAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemMovieAdminLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Lắng nghe sự kiện click vào item movie
            binding.root.setOnClickListener {
                onItemClick(listMovie[adapterPosition])
            }
        }
    }

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

        // Hiển thị thể loại
        holder.binding.tvGenresMovie.text = "Genres: ${movie.genreNames.joinToString()}"

        // Hiển thị diễn viên
        holder.binding.tvCastMovie.text = "Cast: ${movie.castNames.joinToString()}"

        // Cập nhật sự kiện cho nút Show More / Show Less
        holder.binding.btnExpandDetails.setOnClickListener {
            // Thay đổi trạng thái mở rộng/thu gọn
            if (holder.binding.expandableLayout.visibility == android.view.View.VISIBLE) {
                holder.binding.expandableLayout.visibility = android.view.View.GONE
                holder.binding.btnExpandDetails.text = "Show More"
            } else {
                holder.binding.expandableLayout.visibility = android.view.View.VISIBLE
                holder.binding.btnExpandDetails.text = "Show Less"
            }
        }

        // Hiển thị thông tin chi tiết nếu cần
        holder.binding.tvRatingMovie.text = "Rating: ${movie.rating}/5"
        holder.binding.tvSynopsisMovie.text = "Synopsis: ${movie.synopsis}"

        // Hiển thị danh sách ảnh
        val imageAdapter = ImagePhotoAdminAdapter(movie.list_photos)
        holder.binding.recyclerViewPhotos.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        holder.binding.recyclerViewPhotos.adapter = imageAdapter
    }
}
