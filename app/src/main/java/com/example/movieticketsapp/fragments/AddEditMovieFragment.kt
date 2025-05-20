package com.example.movieticketsapp.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.FragmentAddEditMovieLayoutBinding
import com.example.movieticketsapp.model.MovieDetail
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import java.io.File
import java.util.*
import androidx.lifecycle.lifecycleScope
import id.zelory.compressor.constraint.format
import kotlinx.coroutines.launch

class AddEditMovieFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddEditMovieLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var movie: MovieDetail? = null
    private var isEditMode = false
    private var movieId: String? = null

    private val genreMap = mutableMapOf<String, String>()
    private val castMap = mutableMapOf<String, String>()

    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 101

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

        isEditMode = arguments?.getString("movieId") != null
        movieId = arguments?.getString("movieId")

        loadGenresAndCasts()

        if (isEditMode && movieId != null) {
            loadMovieDetails(movieId!!)
        }

        binding.imgMovie.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.btnSave.setOnClickListener {
            saveMovie()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.imgMovie.setImageURI(selectedImageUri)
        }
    }

    private fun loadGenresAndCasts() {
        val genreNames = mutableListOf<String>()
        val castNames = mutableListOf<String>()

        db.collection("gener").get()
            .addOnSuccessListener { genreSnapshot ->
                for (doc in genreSnapshot) {
                    val id = doc.id
                    val name = doc.getString("name") ?: continue
                    genreNames.add(name)
                    genreMap[name] = id
                }
                val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genreNames)
                genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spGenres.adapter = genreAdapter
            }

        db.collection("cast").get()
            .addOnSuccessListener { castSnapshot ->
                for (doc in castSnapshot) {
                    val id = doc.id
                    val name = doc.getString("name") ?: continue
                    castNames.add(name)
                    castMap[name] = id
                }
                val castAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, castNames)
                castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spCasts.adapter = castAdapter
            }
    }

    private fun loadMovieDetails(movieId: String) {
        db.collection("movie").document(movieId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    movie = document.toObject(MovieDetail::class.java)
                    movie?.let { m ->
                        binding.etMovieTitle.setText(m.title)
                        binding.etDirector.setText(m.director)
                        binding.etDuration.setText(m.duration.toString())
                        binding.etTrailer.setText(m.trailer)
                        binding.etSynopsis.setText(m.synopsis)

                        Glide.with(requireContext()).load(m.img_movie).into(binding.imgMovie)

                        setSpinnerSelection(binding.spGenres, m.gener_movie.firstOrNull())
                        setSpinnerSelection(binding.spCasts, m.list_casts.firstOrNull())
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading movie details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSpinnerSelection(spinner: Spinner, selectedId: String?) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        if (selectedId == null) return

        for (i in 0 until adapter.count) {
            val itemName = adapter.getItem(i)
            val idMap = if (spinner == binding.spGenres) genreMap else castMap
            if (idMap[itemName] == selectedId) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun saveMovie() {
        val movieTitle = binding.etMovieTitle.text.toString()
        val director = binding.etDirector.text.toString()
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        val trailer = binding.etTrailer.text.toString()
        val genreName = binding.spGenres.selectedItem?.toString() ?: ""
        val castName = binding.spCasts.selectedItem?.toString() ?: ""
        val synopsis = binding.etSynopsis.text.toString()

        val genreId = genreMap[genreName] ?: ""
        val castId = castMap[castName] ?: ""

        if (selectedImageUri == null && !isEditMode) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            val fileName = "${UUID.randomUUID()}_${movieTitle}.jpg"

            lifecycleScope.launch {
                try {
                    val ctx = context ?: return@launch
                    val tempFile = File(ctx.cacheDir, "temp_image.jpg").apply {
                        ctx.contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                            outputStream().use { output -> input.copyTo(output) }
                        }
                    }

                    val compressedFile = Compressor.compress(ctx, tempFile) {
                        quality(50)
                        format(Bitmap.CompressFormat.JPEG)
                    }

                    val storageRef = FirebaseStorage.getInstance().reference.child("movies/$fileName")
                    storageRef.putFile(Uri.fromFile(compressedFile))
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                upsertMovie(movieTitle, director, duration, trailer, genreId, castId, synopsis, imageUrl)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error compressing image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            upsertMovie(
                movieTitle,
                director,
                duration,
                trailer,
                genreId,
                castId,
                synopsis,
                movie?.img_movie ?: ""
            )
        }
    }

    private fun upsertMovie(
        title: String,
        director: String,
        duration: Int,
        trailer: String,
        genreId: String,
        castId: String,
        synopsis: String,
        imageUrl: String
    ) {
        val movieData = MovieDetail(
            id = movieId ?: FirebaseFirestore.getInstance().collection("movie").document().id,
            title = title,
            director = director,
            duration = duration,
            trailer = trailer,
            gener_movie = listOf(genreId),
            list_casts = listOf(castId),
            synopsis = synopsis,
            img_movie = imageUrl
        )

        val docRef = db.collection("movie").document(movieData.id)
        docRef.set(movieData)
            .addOnSuccessListener {
                Toast.makeText(context, if (isEditMode) "Movie Updated!" else "Movie Added!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.setFragmentResult("movie_saved", Bundle().apply {
                    putBoolean("isUpdated", true)
                })
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error saving movie", Toast.LENGTH_SHORT).show()
            }
    }
}
