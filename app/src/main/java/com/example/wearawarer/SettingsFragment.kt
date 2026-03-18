package com.example.wearawarer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditName: Button
    private lateinit var btnChangePassword: LinearLayout
    private lateinit var switchNotifications: Switch
    private lateinit var btnLogout: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        bindViews()
        setupBackButton()
        loadProfile()
        setupNotificationToggle()
        setupEditName()
        setupChangePassword()
        setupLogout()
    }

    private fun bindViews() {
        tvUserName          = findViewById(R.id.tvUserName)
        tvUserEmail         = findViewById(R.id.tvUserEmail)
        btnEditName         = findViewById(R.id.btnEditName)
        btnChangePassword   = findViewById(R.id.btnChangePassword)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnLogout           = findViewById(R.id.btnLogout)
        progressBar         = findViewById(R.id.progressBar)
    }

    private fun setupBackButton() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun loadProfile() {
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        tvUserName.text  = prefs.getString("user_name", "—")
        tvUserEmail.text = prefs.getString("user_email", "—")

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    val profile = response.body() ?: return@launch
                    tvUserName.text  = profile.full_name
                    tvUserEmail.text = profile.email
                    prefs.edit().putString("user_name", profile.full_name).apply()
                }
            } catch (e: Exception) {
                // Silently fall back to cached values already shown
            }
        }
    }

    private fun setupNotificationToggle() {
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupEditName() {
        btnEditName.setOnClickListener {
            val prefs   = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
            val token   = prefs.getString("token", null) ?: return@setOnClickListener
            val current = tvUserName.text.toString()

            val input = EditText(this).apply {
                hint = "New name"
                setText(current)
                setSelection(current.length)
                setPadding(48, 32, 48, 32)
            }

            AlertDialog.Builder(this)
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (newName == current) return@setPositiveButton

                    setLoading(true)
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.instance.updateProfile(
                                "Bearer $token",
                                UpdateProfileRequest(full_name = newName)
                            )
                            if (response.isSuccessful) {
                                val updated = response.body()?.user
                                tvUserName.text = updated?.full_name ?: newName
                                prefs.edit().putString("user_name", updated?.full_name ?: newName).apply()
                                Toast.makeText(this@SettingsActivity, "Name updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingsActivity, "Failed to update name", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@SettingsActivity, "Network error", Toast.LENGTH_SHORT).show()
                        } finally {
                            setLoading(false)
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupChangePassword() {
        btnChangePassword.setOnClickListener {
            val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token", null) ?: return@setOnClickListener

            val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
            val etCurrent  = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
            val etNew      = dialogView.findViewById<EditText>(R.id.etNewPassword)
            val etConfirm  = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

            AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update") { _, _ ->
                    val current = etCurrent.text.toString()
                    val new     = etNew.text.toString()
                    val confirm = etConfirm.text.toString()

                    when {
                        current.isEmpty() || new.isEmpty() || confirm.isEmpty() ->
                            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                        new.length < 8 ->
                            Toast.makeText(this, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        new != confirm ->
                            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        else -> {
                            setLoading(true)
                            lifecycleScope.launch {
                                try {
                                    val response = RetrofitClient.instance.updateProfile(
                                        "Bearer $token",
                                        UpdateProfileRequest(
                                            current_password = current,
                                            new_password     = new
                                        )
                                    )
                                    when (response.code()) {
                                        200  -> Toast.makeText(this@SettingsActivity, "Password updated!", Toast.LENGTH_SHORT).show()
                                        401  -> Toast.makeText(this@SettingsActivity, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                                        else -> Toast.makeText(this@SettingsActivity, "Failed to update password", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@SettingsActivity, "Network error", Toast.LENGTH_SHORT).show()
                                } finally {
                                    setLoading(false)
                                }
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ -> performLogout() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove("token")
            remove("user_id")
            remove("user_name")
            remove("user_email")
            putBoolean("is_logged_in", false)
            apply()
        }
        stopService(Intent(this, ViolationService::class.java))
        Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility      = if (loading) View.VISIBLE else View.GONE
        btnEditName.isEnabled       = !loading
        btnChangePassword.isEnabled = !loading
        btnLogout.isEnabled         = !loading
    }
}