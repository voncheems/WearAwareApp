package com.example.wearawarer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.wearawarer.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private var activeTimeTab: TextView? = null
    private var activeFilterTab: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTimeTabs()
        setupFilterTabs()
        fetchHistory()
        fetchStats()
    }

    private fun fetchHistory() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDetections("Bearer $token")
                if (response.isSuccessful) {
                    val detections = response.body() ?: emptyList()
                    binding.tvRecentActivity.text = "Recent Activity (${detections.size})"
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error fetching history", e)
            }
        }
    }

    private fun fetchStats() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getStats("Bearer $token")
                if (response.isSuccessful) {
                    val stats = response.body()
                    if (stats != null) {
                        binding.tvTotalInspections.text = stats.total.toString()
                        binding.tvPassed.text = stats.compliant.toString()
                        binding.tvFailed.text = stats.violations.toString()
                        binding.tvPassRate.text = "${stats.compliance_rate}%"
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error fetching stats", e)
            }
        }
    }

    private fun setupTimeTabs() {
        val tabs = listOf(binding.tabThisWeek, binding.tabThisMonth, binding.tabThisYear)
        setActiveTimeTab(binding.tabThisWeek)
        tabs.forEach { tab ->
            tab.setOnClickListener { setActiveTimeTab(tab) }
        }
    }

    private fun setActiveTimeTab(tab: TextView) {
        activeTimeTab?.apply {
            setTextColor(Color.parseColor("#64748B"))
            background = null
        }
        tab.setTextColor(Color.WHITE)
        tab.background = makeRoundedBackground("#6366F1", 22f)
        activeTimeTab = tab
    }

    private fun setupFilterTabs() {
        val tabs = listOf(binding.filterAll, binding.filterPassed, binding.filterFailed)
        setActiveFilterTab(binding.filterAll)
        tabs.forEach { tab ->
            tab.setOnClickListener { setActiveFilterTab(tab) }
        }
    }

    private fun setActiveFilterTab(tab: TextView) {
        activeFilterTab?.apply {
            setTextColor(Color.parseColor("#64748B"))
            background = null
        }
        tab.setTextColor(Color.WHITE)
        tab.background = makeRoundedBackground("#6366F1", 20f)
        activeFilterTab = tab
    }

    private fun makeRoundedBackground(colorHex: String, radiusDp: Float): GradientDrawable {
        val radius = radiusDp * resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.parseColor(colorHex))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}