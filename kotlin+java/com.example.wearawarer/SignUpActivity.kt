package com.example.wearawarer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignUp: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Add input filters to prevent numbers in name fields
        val nameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!source[i].isLetter() && source[i] != ' ' && source[i] != '-' && source[i] != '\'') {
                    return@InputFilter ""
                }
            }
            null
        }

        etFirstName.filters = arrayOf(nameFilter)
        etLastName.filters = arrayOf(nameFilter)

        // Add input filter to phone field to only allow digits
        val phoneFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!source[i].isDigit()) {
                    return@InputFilter ""
                }
            }
            null
        }

        val phoneLengthFilter = InputFilter.LengthFilter(15)
        etPhone.filters = arrayOf(phoneFilter, phoneLengthFilter)

        // Initially disable the button
        updateButtonState()

        // Add text watchers to all fields
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateButtonState()
            }
        }

        etFirstName.addTextChangedListener(textWatcher)
        etLastName.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        // Button click listener
        btnSignUp.setOnClickListener {
            if (validateForm()) {
                // Proceed with sign up
                performSignUp()
            }
        }
    }

    private fun updateButtonState() {
        val isFormValid = etFirstName.text?.isNotBlank() == true &&
                etLastName.text?.isNotBlank() == true &&
                etEmail.text?.isNotBlank() == true &&
                etPhone.text?.isNotBlank() == true &&
                etPassword.text?.isNotBlank() == true &&
                etConfirmPassword.text?.isNotBlank() == true

        btnSignUp.isEnabled = isFormValid

        // Optional: Change button appearance when disabled
        if (isFormValid) {
            btnSignUp.alpha = 1f
        } else {
            btnSignUp.alpha = 0.5f
        }
    }

    private fun isValidName(name: String): Boolean {
        // Check if name contains only letters, spaces, hyphens, and apostrophes
        // No numbers or special characters allowed
        return name.matches(Regex("^[a-zA-Z\\s'-]+$"))
    }

    private fun isValidPhone(phone: String): Boolean {
        // Check if phone contains only digits and is between 10-15 characters
        // Adjust the length range based on your requirements
        return phone.matches(Regex("^[0-9]{10,15}$"))
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate first name
        if (etFirstName.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFirstName)
                .error = "Required"
            isValid = false
        } else if (!isValidName(etFirstName.text.toString())) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFirstName)
                .error = "Name should only contain letters"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFirstName)
                .error = null
        }

        // Validate last name
        if (etLastName.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLastName)
                .error = "Required"
            isValid = false
        } else if (!isValidName(etLastName.text.toString())) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLastName)
                .error = "Name should only contain letters"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLastName)
                .error = null
        }

        // Validate email
        if (etEmail.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)
                .error = "Required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)
                .error = "Invalid email"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail)
                .error = null
        }

        // Validate phone
        if (etPhone.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPhone)
                .error = "Required"
            isValid = false
        } else if (!isValidPhone(etPhone.text.toString())) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPhone)
                .error = "Phone number must be 10-15 digits"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPhone)
                .error = null
        }

        // Validate password
        if (etPassword.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword)
                .error = "Required"
            isValid = false
        } else if (etPassword.text.toString().length < 6) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword)
                .error = "Password must be at least 6 characters"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword)
                .error = null
        }

        // Validate confirm password
        if (etConfirmPassword.text.isNullOrBlank()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)
                .error = "Required"
            isValid = false
        } else if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)
                .error = "Passwords do not match"
            isValid = false
        } else {
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)
                .error = null
        }

        return isValid
    }

    private fun performSignUp() {
        // Get user data
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()
        val password = etPassword.text.toString()

        // TODO: Call your API or Firebase authentication here
        // For now, we'll simulate successful signup

        // Save user data and login status
        val prefs = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_first_name", firstName)
            .putString("user_last_name", lastName)
            .putString("user_phone", phone)
            .apply()

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
