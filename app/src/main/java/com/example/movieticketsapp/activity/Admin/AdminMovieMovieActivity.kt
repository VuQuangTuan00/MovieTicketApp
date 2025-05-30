package com.example.movieticketsapp.activity.Admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieticketsapp.R
import com.example.movieticketsapp.adapter.ItemMovieAdminAdapter
import com.example.movieticketsapp.databinding.AdminMovieMovieLayoutBinding
import com.example.movieticketsapp.fragments.AddEditMovieFragment
import com.example.movieticketsapp.model.MovieAdmin
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class AdminMovieMovieActivity : AppCompatActivity() {
    private lateinit var binding: AdminMovieMovieLayoutBinding
    private lateinit var adapter: ItemMovieAdminAdapter
    private val movieList = mutableListOf<MovieAdmin>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminMovieMovieLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Movie List"

        // Đăng ký lắng nghe kết quả từ fragment
        supportFragmentManager.setFragmentResultListener("movie_saved", this) { _, result ->
            val isUpdated = result.getBoolean("isUpdated", false)
            if (isUpdated) {
                loadMoviesFromFirestore()
            }
        }

        adapter = ItemMovieAdminAdapter(
            movieList,
            onItemClick = { movie ->
                // Chuyển sang Activity quản lý showtime của movie
                val intent = Intent(this, AdminShowtimesActivity::class.java)
                intent.putExtra("movieId", movie.id)
                startActivity(intent)
            },
            onEdit = { movie -> openAddEditMovieFragment(true, movie.id) },
            onDelete = { movie -> confirmDelete(movie) }
        )

        binding.listMovies.layoutManager = LinearLayoutManager(this)
        binding.listMovies.adapter = adapter

        binding.fabAddMovie.setOnClickListener {
            openAddEditMovieFragment(false, null)
        }

        loadMoviesFromFirestore()
    }

    private fun loadMoviesFromFirestore() {
        val genreNamesMap = mutableMapOf<String, String>()
        val castNamesMap = mutableMapOf<String, String>()

        val genreTask = db.collection("gener").get()
        val castTask = db.collection("cast").get()

        Tasks.whenAllSuccess<Any>(genreTask, castTask)
            .addOnSuccessListener { results ->
                val genreResult = results[0] as QuerySnapshot
                val castResult = results[1] as QuerySnapshot

                genreResult.forEach { genreDoc ->
                    val genreName = genreDoc.getString("name") ?: ""
                    genreNamesMap[genreDoc.id] = genreName
                }

                castResult.forEach { castDoc ->
                    val actorName = castDoc.getString("name") ?: ""
                    castNamesMap[castDoc.id] = actorName
                }

                db.collection("movie").get()
                    .addOnSuccessListener { movieResult ->
                        movieList.clear()
                        for (document in movieResult) {
                            val movie = document.toObject(MovieAdmin::class.java)

                            val genreNames = movie.gener_movie.mapNotNull { genreNamesMap[it] }
                            val castNames = movie.list_casts.mapNotNull { castNamesMap[it] }

                            val updatedMovie = movie.copy(
                                genreNames = genreNames,
                                castNames = castNames
                            )

                            movieList.add(updatedMovie)
                        }

                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error loading movies: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading genres or casts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openAddEditMovieFragment(isEditMode: Boolean, movieId: String?) {
        val fragment = AddEditMovieFragment()
        val bundle = Bundle()
        bundle.putBoolean("isEditMode", isEditMode)
        movieId?.let { bundle.putString("movieId", it) }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(movie: MovieAdmin) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Movie")
            .setMessage("Are you sure you want to delete ${movie.title}?")
            .setPositiveButton("Yes") { _, _ ->
                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(movie.img_movie)

                imageRef.delete()
                    .addOnSuccessListener {
                        db.collection("movie").document(movie.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Movie deleted successfully!", Toast.LENGTH_SHORT).show()
                                loadMoviesFromFirestore() // Reload the movie list after deletion
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error deleting movie from Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error deleting movie image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() } // Do nothing on cancel
            .create()
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
