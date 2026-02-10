package com.example.wearawarer

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialCardView
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUp = findViewById(R.id.tvSignUp)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            email.isEmpty() -> {
                tilEmail.error = "Email or username is required"
                isValid = false
            }
            email.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                tilEmail.error = "Invalid email format"
                isValid = false
            }
            else -> {
                tilEmail.error = null
            }
        }

        when {
            password.isEmpty() -> {
                tilPassword.error = "Password is required"
                isValid = false
            }
            password.length < 6 -> {
                tilPassword.error = "Password must be at least 6 characters"
                isValid = false
            }
            else -> {
                tilPassword.error = null
            }
        }

        return isValid
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        btnLogin.isEnabled = false

        btnLogin.postDelayed({
            if (email.isNotEmpty() && password.isNotEmpty()) {
                val prefs = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_email", email)
                    .apply()

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                tilEmail.error = "Invalid credentials"
                tilPassword.error = "Invalid credentials"
                btnLogin.isEnabled = true
            }
        }, 1000)
    }
}
