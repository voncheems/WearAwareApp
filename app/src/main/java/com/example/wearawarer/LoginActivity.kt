package com.example.wearawarer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin       = findViewById<CardView>(R.id.btnLogin)
        val etEmail        = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword     = findViewById<TextInputEditText>(R.id.etPassword)
        val cbRememberMe   = findViewById<MaterialCheckBox>(R.id.cbRememberMe)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // Pre-fill email and password if "Remember Me" was previously checked
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val savedEmail    = prefs.getString("remembered_email", null)
        val savedPassword = prefs.getString("remembered_password", null)

        if (savedEmail != null) {
            etEmail.setText(savedEmail)
            cbRememberMe.isChecked = true
        }
        if (savedPassword != null) {
            etPassword.setText(savedPassword)
        }

        // ── Login ────────────────────────────────────────────
        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
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
                            putString("user_name", body.user.full_name)
                            putString("user_email", body.user.email)
                            putString("user_role", body.user.role)
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

        // ── Forgot Password ──────────────────────────────────
        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val tilEmail   = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilForgotEmail)
        val tilReason  = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilForgotReason)
        val etEmail    = dialogView.findViewById<TextInputEditText>(R.id.etForgotEmail)
        val etReason   = dialogView.findViewById<TextInputEditText>(R.id.etForgotReason)

        // Pre-fill email if already typed on login screen
        val currentEmail = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
        if (currentEmail.isNotEmpty()) etEmail.setText(currentEmail)

        // Clear errors on input change
        etEmail.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { tilEmail.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        etReason.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { tilReason.error = null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("Enter your login email and an optional reason. An admin will reset your password.")
            .setView(dialogView)
            .setPositiveButton("Submit", null) // set null to override auto-dismiss
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val email  = etEmail.text.toString().trim()
                val reason = etReason.text.toString().trim()

                // ── Validation ──
                var hasError = false

                if (email.isEmpty()) {
                    tilEmail.error = "Email is required"
                    hasError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.error = "Enter a valid email address"
                    hasError = true
                } else if (!email.endsWith("@wearaware.ph")) {
                    tilEmail.error = "Must be a @wearaware.ph email address"
                    hasError = true
                }

                if (reason.isNotEmpty() && reason.length < 5) {
                    tilReason.error = "Reason is too short — add more detail or leave it blank"
                    hasError = true
                }

                if (hasError) return@setOnClickListener

                // ── Submit ──
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Submitting…"

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.forgotPassword(
                            ForgotPasswordRequest(email, reason.ifEmpty { null })
                        )

                        if (response.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(
                                this@LoginActivity,
                                "Request submitted! An admin will reset your password shortly.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val errorBody = response.errorBody()?.string() ?: ""
                            val errorMsg  = try {
                                org.json.JSONObject(errorBody).optString("error", "Something went wrong.")
                            } catch (e: Exception) { "Something went wrong." }

                            // Show specific errors inline
                            when {
                                errorMsg.contains("email", ignoreCase = true) ->
                                    tilEmail.error = errorMsg
                                errorMsg.contains("pending", ignoreCase = true) ->
                                    tilEmail.error = "A reset request is already pending for this email"
                                errorMsg.contains("not found", ignoreCase = true) ->
                                    tilEmail.error = "No account found with this email"
                                else ->
                                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                            }

                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Submit"
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Connection error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).text = "Submit"
                    }
                }
            }
        }

        dialog.show()
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