package com.example.movieticketsapp.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieticketsapp.R
import com.example.movieticketsapp.databinding.SignInLayoutBinding
import com.example.movieticketsapp.utils.navigateTo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: SignInLayoutBinding
    private lateinit var dialog:Dialog
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialog = Dialog(this)
        auth = Firebase.auth

        binding = SignInLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRememberedLogin()
        setEvent()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setEvent(){
        binding.apply {
            imBack.setOnClickListener {finish()}
            tvForgotPass.setOnClickListener { navigateTo(ResetPassWordActivity::class.java,flag = false)}
            tvSignUp.setOnClickListener {navigateTo(SignUpActivity::class.java,flag = false)}
            btnSignIn.setOnClickListener {
                when{
                    edtMail.text.toString().isEmpty() -> edtMail.error = "Vui lòng nhập email"
                    edtPass.text.toString().isEmpty() -> edtPass.error = "Vui lòng nhập mật khẩu"
                    else -> signInToFirebase(edtMail, edtPass)
                }
            }
        }
    }

    private fun signInToFirebase(mail: EditText, pass: EditText) {
        val email = mail.text.toString()
        val password = pass.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.reload()?.addOnSuccessListener {
                        if (user.isEmailVerified) {
                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    val role = document.getString("role") ?: "User"
                                    Log.d("TAG_ROLE", "User role: $role")

                                    // Ghi nhớ nếu được chọn
                                    if (binding.checkBoxRemember.isChecked) {
                                        val pref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                                        with(pref.edit()) {
                                            putString("email", email)
                                            putString("password", password)
                                            putBoolean("remember", true)
                                            apply()
                                        }
                                    }

                                    showSuccessDialogThenNavigate(if (role == "Admin") AdminHomeActivity::class.java else HomaPageActivity::class.java)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Vui lòng xác thực email để đăng nhập.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    }
                } else {
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Đăng nhập thất bại: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showSuccessDialogThenNavigate(destination: Class<*>) {
        dialog.setContentView(R.layout.popup_sign_in_success_layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        if (dialog.isShowing) {
            Handler(Looper.getMainLooper()).postDelayed({
                navigateTo(destination, flag = true)
            }, 1400)
        }
    }

    private fun checkRememberedLogin() {
        val pref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val remember = pref.getBoolean("remember", false)
        if (remember) {
            val email = pref.getString("email", null)
            val password = pref.getString("password", null)
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                binding.edtMail.setText(email)
                binding.edtPass.setText(password)
                binding.checkBoxRemember.isChecked = true
                signInToFirebase(binding.edtMail, binding.edtPass)
            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}

