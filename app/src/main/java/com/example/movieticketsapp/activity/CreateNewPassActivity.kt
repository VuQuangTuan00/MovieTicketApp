package com.example.movieticketsapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.CreateNewPassLayoutBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class CreateNewPassActivity : AppCompatActivity() {
    private lateinit var binding: CreateNewPassLayoutBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateNewPassLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateNewPassword.setOnClickListener {
            val password = binding.edtPassword.text.toString().trim()
            val newPassword = binding.edtNewPassword.text.toString().trim()
            val confirmPassword = binding.edtCfPassword.text.toString().trim()

            if (password.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu mới không trùng khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val email = user?.email

            if (user != null && email != null) {
                val credential = EmailAuthProvider.getCredential(email, password)

                // Xác thực lại người dùng
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Cập nhật mật khẩu mới
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()

                                // Đăng xuất ngươời dùng
                                FirebaseAuth.getInstance().signOut()

                                // Chuyển về màn hình đăng nhập
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finishAffinity()
                            } else {
                                Toast.makeText(this, "Thất bại khi đổi mật khẩu", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show()
            }
        }
    }
}