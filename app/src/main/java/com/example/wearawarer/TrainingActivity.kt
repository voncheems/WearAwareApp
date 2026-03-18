package com.example.wearawarer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wearawarer.databinding.ActivityTrainingBinding

class TrainingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        setupModuleClicks()
    }

    private fun setupModuleClicks() {
        binding.moduleHardHat.setOnClickListener {
            openQuiz("Hard Hat Safety", "HARD_HAT")
        }
        binding.moduleSafetyVest.setOnClickListener {
            openQuiz("Safety Vest Requirements", "SAFETY_VEST")
        }
        binding.moduleGloves.setOnClickListener {
            openQuiz("Hand Protection & Gloves", "GLOVES")
        }
        binding.moduleBoots.setOnClickListener {
            openQuiz("Safety Boots & Footwear", "BOOTS")
        }
        binding.moduleEyeProtection.setOnClickListener {
            openQuiz("Eye & Face Protection", "EYE_PROTECTION")
        }
        binding.moduleRespiratory.setOnClickListener {
            openQuiz("Respiratory Protection", "RESPIRATORY")
        }
    }

    private fun openQuiz(title: String, moduleKey: String) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("MODULE_TITLE", title)
            putExtra("MODULE_KEY", moduleKey)
        }
        startActivity(intent)
    }
}