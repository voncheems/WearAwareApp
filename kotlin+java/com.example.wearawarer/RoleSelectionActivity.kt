package com.example.wearawarer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class RoleSelectionActivity : AppCompatActivity() {

    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_role_selection)

        setupRoleCards()
        setupContinueButton()
    }

    private fun setupRoleCards() {
        val cardWorker = findViewById<MaterialCardView>(R.id.cardWorker)
        val cardInspector = findViewById<MaterialCardView>(R.id.cardInspector)
        val cardManager = findViewById<MaterialCardView>(R.id.cardManager)
        val cardAdmin = findViewById<MaterialCardView>(R.id.cardAdmin)

        cardWorker.setOnClickListener {
            selectRole("WORKER", cardWorker)
        }

        cardInspector.setOnClickListener {
            selectRole("INSPECTOR", cardInspector)
        }

        cardManager.setOnClickListener {
            selectRole("MANAGER", cardManager)
        }

        cardAdmin.setOnClickListener {
            selectRole("ADMIN", cardAdmin)
        }
    }

    private fun selectRole(role: String, selectedCard: MaterialCardView) {
        selectedRole = role
        Log.d("RoleSelection", "Selected role: $role")

        // Reset all cards
        listOf(
            findViewById<MaterialCardView>(R.id.cardWorker),
            findViewById<MaterialCardView>(R.id.cardInspector),
            findViewById<MaterialCardView>(R.id.cardManager),
            findViewById<MaterialCardView>(R.id.cardAdmin)
        ).forEach { card ->
            card.strokeWidth = 0
        }

        // Highlight selected card with orange border
        selectedCard.strokeWidth = 8
        selectedCard.strokeColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark)

        // Update radio button states
        updateRadioButtons(role)
    }

    private fun updateRadioButtons(selectedRole: String) {
        findViewById<android.view.View>(R.id.radioWorker).isSelected = selectedRole == "WORKER"
        findViewById<android.view.View>(R.id.radioInspector).isSelected = selectedRole == "INSPECTOR"
        findViewById<android.view.View>(R.id.radioManager).isSelected = selectedRole == "MANAGER"
        findViewById<android.view.View>(R.id.radioAdmin).isSelected = selectedRole == "ADMIN"
    }

    private fun setupContinueButton() {
        val btnContinue = findViewById<MaterialCardView>(R.id.btnContinue)

        btnContinue.setOnClickListener {
            if (selectedRole != null) {
                Log.d("RoleSelection", "Continue clicked with role: $selectedRole")

                // Save the selected role
                val prefs = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("user_role", selectedRole)
                    .apply()

                // Navigate to LOGIN activity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Please select a role to continue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
