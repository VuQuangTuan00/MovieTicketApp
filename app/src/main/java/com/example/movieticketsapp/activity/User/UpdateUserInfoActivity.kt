package com.example.movieticketsapp.activity.User

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.movieticketsapp.databinding.UpdateUserInfoLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpdateUserInfoActivity : AppCompatActivity() {

    private lateinit var binding: UpdateUserInfoLayoutBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var avatarUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UpdateUserInfoLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserInfo()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgAvatar.setOnClickListener {
            openImageChooser()
        }

        binding.btnUpdate.setOnClickListener {
            updateUserInfo()
        }
    }

    private fun loadUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val avatar = document.getString("avatar")
                    val name = document.getString("name")
                    val phone = document.getString("phone")
                    val dob = document.getString("dob")

                    avatar?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .circleCrop()
                            .into(binding.imgAvatar)
                    }

                    binding.edtFullname.setText(name)
                    binding.edtPhoneNumber.setText(phone)
                    binding.edtDateOfBirth.setText(dob)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            avatarUri = data.data
            binding.imgAvatar.setImageURI(avatarUri)
        }
    }

    private fun updateUserInfo() {
        val fullname = binding.edtFullname.text.toString().trim()
        val phoneNumber = binding.edtPhoneNumber.text.toString().trim()
        val dateOfBirth = binding.edtDateOfBirth.text.toString().trim()

        if (fullname.isEmpty() || phoneNumber.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        showLoading(true)

        firestore.collection("users").document(userId)
            .update("name", fullname, "phone", phoneNumber, "dob", dateOfBirth)
            .addOnSuccessListener {
                if (avatarUri != null) {
                    uploadAvatarToStorage(userId)
                } else {
                    showLoading(false)
                    Toast.makeText(this, "User info updated successfully!", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error updating user info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadAvatarToStorage(userId: String) {
        val avatarRef: StorageReference = storage.reference.child("avatars/$userId.jpg")

        avatarUri?.let { uri ->
            avatarRef.putFile(uri)
                .addOnSuccessListener {
                    avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        firestore.collection("users").document(userId)
                            .update("avatar", downloadUri.toString())
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(this, "User info updated successfully!", Toast.LENGTH_SHORT).show()
                                onBackPressed()
                            }
                            .addOnFailureListener {
                                showLoading(false)
                                Toast.makeText(this, "Error updating avatar URL", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Error uploading avatar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnUpdate.isEnabled = !show
    }
}
