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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieticketsapp.adapter.ItemPhotosApdater
import com.example.movieticketsapp.databinding.FragmentAddEditMovieLayoutBinding
import com.example.movieticketsapp.model.MovieDetail
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*

class AddEditMovieFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddEditMovieLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ItemPhotosApdater
    private val db = FirebaseFirestore.getInstance()

    private var isEditMode = false
    private var movieId: String? = null
    private var selectedImageUri: Uri? = null // main image
    private val photoUriList = mutableListOf<Uri>() // selected sub-photo URIs
    private val photoUrlList = mutableListOf<String>() // URLs for display in adapter

    private val genreMap = mutableMapOf<String, String>()
    private val castMap = mutableMapOf<String, String>()

    private val selectedGenres = mutableSetOf<String>()
    private val selectedCasts = mutableSetOf<String>()

    private val IMAGE_MAIN_CODE = 101
    private val IMAGE_PICK_CODE = 102

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentAddEditMovieLayoutBinding.inflate(inflater, container, false)
            .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isEditMode = arguments?.getString("movieId") != null
        movieId = arguments?.getString("movieId")

        loadGenresAndCasts {
            if (isEditMode && movieId != null) loadMovieDetails(movieId!!)
        }

        binding.imgMovie.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                IMAGE_MAIN_CODE
            )
        }

        binding.btnAddPhotos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        binding.btnSave.setOnClickListener { saveMovie() }
        binding.btnCancel.setOnClickListener { dismiss() }

        // Adapter for preview images
        adapter = ItemPhotosApdater(photoUrlList)
        binding.rvPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPhotos.adapter = adapter

        // Multi-select dialogs
        binding.tvGenres.setOnClickListener { showMultiSelectDialog("Chọn thể loại", genreMap, selectedGenres) { updateGenreText() } }
        binding.tvCasts.setOnClickListener { showMultiSelectDialog("Chọn diễn viên", castMap, selectedCasts) { updateCastText() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            IMAGE_MAIN_CODE -> {
                selectedImageUri = data.data
                binding.imgMovie.setImageURI(selectedImageUri)
            }

            IMAGE_PICK_CODE -> {
                val selectedUris = mutableListOf<Uri>()
                if (data.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        selectedUris.add(data.clipData!!.getItemAt(i).uri)
                    }
                } else {
                    data.data?.let { selectedUris.add(it) }
                }

                photoUriList.addAll(selectedUris)
                photoUrlList.addAll(selectedUris.map { it.toString() }) // local previews
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadGenresAndCasts(onComplete: () -> Unit) {
        db.collection("gener").get().addOnSuccessListener { genres ->
            genreMap.clear()
            genres.forEach { genre ->
                genreMap[genre.getString("name")!!] = genre.id
            }
            updateGenreText()

            db.collection("cast").get().addOnSuccessListener { casts ->
                castMap.clear()
                casts.forEach { cast ->
                    castMap[cast.getString("name")!!] = cast.id
                }
                updateCastText()
                onComplete()
            }
        }
    }

    private fun loadMovieDetails(id: String) {
        db.collection("movie").document(id).get().addOnSuccessListener {
            val movie = it.toObject(MovieDetail::class.java) ?: return@addOnSuccessListener
            binding.apply {
                etMovieTitle.setText(movie.title)
                etDirector.setText(movie.director)
                etDuration.setText(movie.duration.toString())
                etTrailer.setText(movie.trailer)
                etSynopsis.setText(movie.synopsis)
                Glide.with(requireContext()).load(movie.img_movie).into(imgMovie)

                photoUrlList.clear()
                photoUrlList.addAll(movie.list_photos ?: emptyList())
                adapter.notifyDataSetChanged()

                selectedGenres.clear()
                selectedGenres.addAll(movie.gener_movie)
                updateGenreText()

                selectedCasts.clear()
                selectedCasts.addAll(movie.list_casts)
                updateCastText()
            }
        }
    }

    private fun saveMovie() {
        val title = binding.etMovieTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter movie title", Toast.LENGTH_SHORT).show()
            return
        }

        val director = binding.etDirector.text.toString().trim()
        val duration = binding.etDuration.text.toString().toIntOrNull() ?: 0
        val trailer = binding.etTrailer.text.toString().trim()
        val synopsis = binding.etSynopsis.text.toString().trim()

        if (selectedGenres.isEmpty()) {
            Toast.makeText(context, "Please select at least one genre", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCasts.isEmpty()) {
            Toast.makeText(context, "Please select at least one cast", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val imgUrl = if (selectedImageUri != null) {
                uploadImage(selectedImageUri!!, title)
            } else {
                if (isEditMode) {
                    val doc = db.collection("movie").document(movieId!!).get().await()
                    doc.getString("img_movie") ?: ""
                } else {
                    Toast.makeText(context, "Please select a main image", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val uploadedPhotoUrls = if (photoUriList.isNotEmpty()) {
                uploadPhotoList(title)
            } else {
                if (isEditMode) {
                    val doc = db.collection("movie").document(movieId!!).get().await()
                    doc.get("list_photos") as? List<String> ?: emptyList()
                } else {
                    emptyList()
                }
            }

            upsertMovie(
                movieId ?: db.collection("movie").document().id,
                title, director, duration, trailer, synopsis,
                selectedGenres.toList(), selectedCasts.toList(), imgUrl, uploadedPhotoUrls
            )
        }
    }

    private fun showMultiSelectDialog(
        title: String,
        map: Map<String, String>,
        selectedSet: MutableSet<String>,
        onSelectedChanged: () -> Unit
    ) {
        val names = map.keys.sorted().toTypedArray()
        val checked = names.map { map[it] in selectedSet }.toBooleanArray()

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                val id = map[names[which]]
                if (id != null) {
                    if (isChecked) selectedSet.add(id) else selectedSet.remove(id)
                }
            }
            .setPositiveButton("OK") { _, _ -> onSelectedChanged() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateGenreText() {
        val selectedNames = genreMap.filterValues { selectedGenres.contains(it) }.keys
        binding.tvGenres.text = if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ") else "Chọn thể loại"
    }

    private fun updateCastText() {
        val selectedNames = castMap.filterValues { selectedCasts.contains(it) }.keys
        binding.tvCasts.text = if (selectedNames.isNotEmpty()) selectedNames.joinToString(", ") else "Chọn diễn viên"
    }

    private suspend fun uploadImage(uri: Uri, title: String): String {
        val ctx = requireContext()
        val tempFile = File(ctx.cacheDir, "main.jpg").apply {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                outputStream().use { output -> input.copyTo(output) }
            }
        }

        val compressed = Compressor.compress(ctx, tempFile) {
            quality(50)
            format(Bitmap.CompressFormat.JPEG)
        }

        val fileName = "${UUID.randomUUID()}_$title.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child("movies/$fileName")
        storageRef.putFile(Uri.fromFile(compressed)).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun uploadPhotoList(title: String): List<String> = coroutineScope {
        val ctx = requireContext()
        val deferredList = photoUriList.mapIndexed { i, uri ->
            async {
                try {
                    val tempFile = File(ctx.cacheDir, "sub_$i.jpg").apply {
                        ctx.contentResolver.openInputStream(uri)?.use { input ->
                            outputStream().use { output -> input.copyTo(output) }
                        }
                    }

                    val compressed = Compressor.compress(ctx, tempFile) {
                        quality(50)
                        format(Bitmap.CompressFormat.JPEG)
                    }

                    val fileName = "sub_${UUID.randomUUID()}_$title.jpg"
                    val storageRef =
                        FirebaseStorage.getInstance().reference.child("movies/$fileName")
                    storageRef.putFile(Uri.fromFile(compressed)).await()
                    storageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    ""
                }
            }
        }
        deferredList.awaitAll()
    }

    private fun upsertMovie(
        id: String, title: String, director: String, duration: Int, trailer: String,
        synopsis: String, genreIds: List<String>, castIds: List<String>,
        imageUrl: String, subPhotoUrls: List<String>
    ) {
        val movie = MovieDetail(
            id, title, director, duration, genreIds, imageUrl, castIds,
            list_photos = subPhotoUrls, trailer = trailer, rating = 0.0, synopsis = synopsis
        )
        db.collection("movie").document(id).set(movie)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    if (isEditMode) "Movie Updated!" else "Movie Added!",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.setFragmentResult(
                    "movie_saved",
                    Bundle().apply { putBoolean("isUpdated", true) })
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error saving movie", Toast.LENGTH_SHORT).show()
            }
    }
}
