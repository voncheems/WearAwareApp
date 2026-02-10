package com.example.wearawarer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashTimeOut)
    }

    private fun navigateToNextScreen() {
        // Use the SAME preference name as LoginActivity
        val sharedPreferences = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
        val hasCompletedOnboarding = sharedPreferences.getBoolean("has_completed_onboarding", false)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)

        val intent = when {
            !hasCompletedOnboarding -> Intent(this, OnboardingActivity::class.java)
            !isLoggedIn -> Intent(this, LoginActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
