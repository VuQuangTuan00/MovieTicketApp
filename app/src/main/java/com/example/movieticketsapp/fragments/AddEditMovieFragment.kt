package com.example.movieticketsapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.movieticketsapp.databinding.FragmentAddEditMovieLayoutBinding
import com.example.movieticketsapp.model.MovieDetail
import com.google.firebase.firestore.FirebaseFirestore

class AddEditMovieFragment : Fragment() {

    private var _binding: FragmentAddEditMovieLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var movie: MovieDetail? = null
    private var isEditMode = false
    private var movieId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditMovieLayoutBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isEditMode = arguments?.getBoolean("isEditMode") ?: false
        movieId = arguments?.getString("movieId")
        isEditMode = movieId != null

        loadGenresAndCasts()

        if (isEditMode && movieId != null) {
            loadMovieDetails(movieId!!)
        }

        binding.btnSave.setOnClickListener {
            saveMovie()
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun loadGenresAndCasts() {
        val genreNames = mutableListOf<String>()
        val castNames = mutableListOf<String>()

        db.collection("gener").get()
            .addOnSuccessListener { genreSnapshot ->
                for (doc in genreSnapshot) {
                    val genreName = doc.getString("name") ?: ""
                    genreNames.add(genreName)
                }
                // Populate genre spinner
                val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genreNames)
                genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spGenres.adapter = genreAdapter
            }

        db.collection("cast").get()
            .addOnSuccessListener { castSnapshot ->
                for (doc in castSnapshot) {
                    val castName = doc.getString("name") ?: ""
                    castNames.add(castName)
                }
                // Populate cast spinner
                val castAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, castNames)
                castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spCasts.adapter = castAdapter
            }
    }

    private fun loadMovieDetails(movieId: String) {
        db.collection("movie").document(movieId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    movie = document.toObject(MovieDetail::class.java) // Changed to MovieDetail
                    movie?.let { m ->
                        binding.etMovieTitle.setText(m.title)
                        binding.etDirector.setText(m.director)
                        binding.etDuration.setText(m.duration.toString())
                        binding.etSynopsis.setText(m.synopsis)

                        // Set the selected genre and cast from movie data
                        setSpinnerSelection(binding.spGenres, m.gener_movie)
                        setSpinnerSelection(binding.spCasts, m.list_casts)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading movie details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSpinnerSelection(spinner: Spinner, selectedIds: List<String>) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i) in selectedIds) {
                spinner.setSelection(i)
            }
        }
    }

    private fun saveMovie() {
        val movieTitle = binding.etMovieTitle.text.toString()
        val director = binding.etDirector.text.toString()
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        val genre = binding.spGenres.selectedItem.toString() // Get selected genre from Spinner
        val cast = binding.spCasts.selectedItem.toString() // Get selected cast from Spinner
        val synopsis = binding.etSynopsis.text.toString()

        val genreId = getGenreId(genre)
        val castId = getCastId(cast)

        val movieData = MovieDetail(
            title = movieTitle,
            director = director,
            duration = duration,
            gener_movie = listOf(genreId),
            list_casts = listOf(castId),
            synopsis = synopsis
        )

        if (isEditMode) {
            // Update movie
            db.collection("movie").document(movieId!!).set(movieData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Movie updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error updating movie", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Add new movie
            db.collection("movie").add(movieData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Movie added successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error adding movie", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getGenreId(genreName: String): String {
        val genreMap = mapOf("Action" to "id1", "Comedy" to "id2", "Drama" to "id3")
        return genreMap[genreName] ?: ""
    }

    private fun getCastId(castName: String): String {
        val castMap = mapOf("Actor 1" to "castId1", "Actor 2" to "castId2")
        return castMap[castName] ?: ""
    }
}