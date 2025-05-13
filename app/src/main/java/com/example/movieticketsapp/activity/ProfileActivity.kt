package com.example.movieticketsapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.databinding.ProfieLayoutBinding
import com.example.movieticketsapp.model.User
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ProfieLayoutBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding = ProfieLayoutBinding.inflate(layoutInflater)
        db = FirebaseFirestore.getInstance()
        setContentView(binding.root)
        setEvent()
        setImagePicker()
        openGallery()
    }

    private fun setImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        binding.imgAvatar.setImageURI(selectedImageUri)
                    }
                }
            }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun setEvent() {
        binding.apply {
            imgBack.setOnClickListener {
                finish()
            }
            imgAvatar.setOnClickListener {
                openGallery()
            }
            btnFinish.setOnClickListener {
                sendData()
            }
        }
    }

    private fun sendData() {
        val name = binding.edtFullname.text.toString().trim()
        val phone = binding.edtPhoneNumber.text.toString().trim()
        val dob = binding.edtDateOfBirth.text.toString().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (name.isEmpty() || phone.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }


        if (selectedImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("avatars/$userId.jpg")

            storageRef.putFile(selectedImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: Exception("Upload failed")
                    }
                    storageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    val user = User(
                        name = name,
                        phone = phone,
                        dob = dob,
                        avatar = uri.toString(),
                        email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                        role = "user",
                    )
                    saveUserToFirestore(user)
                }
                .addOnFailureListener {e->
                    Toast.makeText(this, "Lỗi firebase ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            val user = User(
                name = name,
                phone = phone,
                dob = dob,
                avatar = "",
                email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                role = "user",
            )
            saveUserToFirestore(user)
        }
    }
    private fun saveUserToFirestore(user: User) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
               navigateTo(HomaPageActivity::class.java,flag = false)
                finish()
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(this, "Lỗi khi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}