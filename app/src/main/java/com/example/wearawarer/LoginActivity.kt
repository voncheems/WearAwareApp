package com.example.wearawarer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<CardView>(R.id.btnLogin)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val cbRememberMe = findViewById<MaterialCheckBox>(R.id.cbRememberMe)

        // Pre-fill email and password if "Remember Me" was previously checked
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("remembered_email", null)
        val savedPassword = prefs.getString("remembered_password", null)
        
        if (savedEmail != null) {
            etEmail.setText(savedEmail)
            cbRememberMe.isChecked = true
        }
        if (savedPassword != null) {
            etPassword.setText(savedPassword)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    Log.d("LOGIN", "Attempting login with email: $email")

                    val response = RetrofitClient.instance.login(
                        LoginRequest(email, password)
                    )

                    val errorBodyString = response.errorBody()?.string()

                    Log.d("LOGIN", "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val body = response.body()!!

                        prefs.edit {
                            putString("token", body.token)
                            putInt("user_id", body.user.id)
                            putString("user_name", body.user.full_name) // Match backend 'full_name'
                            putString("user_email", body.user.email)
                            putString("user_role", body.user.role) // Match backend 'role'
                            putBoolean("is_logged_in", true)
                            
                            if (cbRememberMe.isChecked) {
                                putString("remembered_email", email)
                                putString("remembered_password", password)
                            } else {
                                remove("remembered_email")
                                remove("remembered_password")
                            }
                        }

                        Log.d("LOGIN", "Login successful, starting ViolationService")
                        startViolationService()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    } else {
                        val errorMsg = errorBodyString ?: "Unknown error"
                        Log.e("LOGIN", "Login failed: $errorMsg")
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    Log.e("LOGIN", "Exception: ${e.message}", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Connection error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun startViolationService() {
        val serviceIntent = Intent(this, ViolationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
