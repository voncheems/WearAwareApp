package com.example.wearawarer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.wearawarer.databinding.FragmentHomeBinding
import android.content.Intent
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayUserInfo()
        fetchStats()
        setupDrawer()
        setupQuickActions()
    }

    private fun displayUserInfo() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Inspector")
        binding.tvUserName.text = userName
    }

    private fun fetchStats() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getStats("Bearer $token")
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null) updateStatsUI(stats)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching stats", e)
            }
        }
    }

    private fun updateStatsUI(stats: DetectionStats) {
        binding.tvSafetyScore.text = calculateGrade(stats.compliance_rate)
        binding.tvCompliancePercentage.text = "${stats.compliance_rate}%"
        binding.tvComplianceStatus.text = when {
            stats.compliance_rate >= 90 -> "Excellent Compliance"
            stats.compliance_rate >= 75 -> "Good Compliance"
            else -> "Needs Attention"
        }
    }

    private fun calculateGrade(rate: Int): String {
        return when {
            rate >= 95 -> "A+"
            rate >= 90 -> "A"
            rate >= 80 -> "B+"
            rate >= 70 -> "B"
            else -> "C"
        }
    }

    private fun setupDrawer() {
        binding.layoutUserName.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }
    }

    private fun setupQuickActions() {
        binding.cardInspections.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_inspections)
        }
        binding.cardHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_history)
        }
        binding.cardTeam.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_team)
        }
        binding.cardTraining.setOnClickListener {
            startActivity(Intent(requireContext(), TrainingActivity::class.java))
        }
        binding.cardCompliance.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_alerts)
        }
        binding.cardSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}