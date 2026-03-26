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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wearawarer.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ── Detection model from API ──────────────────────────────────
data class Detection(
    val id: Int,
    val result: String,
    val missing_ppe: List<String>?,
    val detected_ppe: List<String>?,
    val station: String?,
    val location: String?,
    val date: String?,   // "YYYY-MM-DD" in Asia/Manila
    val time: String?    // "HH:MM AM/PM" in Asia/Manila
)

// ── Adapter ───────────────────────────────────────────────────
class DetectionAdapter(
    private var detections: List<Detection>
) : RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    class DetectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStation: TextView    = view.findViewById(R.id.tvStation)
        val tvStatus: TextView     = view.findViewById(R.id.tvStatus)
        val tvMissingPpe: TextView = view.findViewById(R.id.tvMissingPpe)
        val tvDateTime: TextView   = view.findViewById(R.id.tvDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detection, parent, false)
        return DetectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        val detection = detections[position]
        val isViolation = detection.result == "violation"

        holder.tvStation.text = detection.station ?: detection.location ?: "Unknown Station"

        if (isViolation) {
            holder.tvStatus.text = "⚠ Violation"
            holder.tvStatus.setTextColor(Color.parseColor("#991B1B"))
            setBadge(holder.tvStatus, "#FEE2E2")

            val missing = detection.missing_ppe?.joinToString(", ")
            if (!missing.isNullOrEmpty()) {
                holder.tvMissingPpe.text = "Missing: $missing"
                holder.tvMissingPpe.visibility = View.VISIBLE
            } else {
                holder.tvMissingPpe.visibility = View.GONE
            }
        } else {
            holder.tvStatus.text = "✓ Compliant"
            holder.tvStatus.setTextColor(Color.parseColor("#065F46"))
            setBadge(holder.tvStatus, "#D1FAE5")
            holder.tvMissingPpe.visibility = View.GONE
        }

        holder.tvDateTime.text = "${detection.date ?: ""} · ${detection.time ?: ""}"
    }

    private fun setBadge(view: View, colorHex: String) {
        val radius = 20f * view.resources.displayMetrics.density
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.parseColor(colorHex))
        }
    }

    override fun getItemCount() = detections.size

    fun updateData(newList: List<Detection>) {
        detections = newList
        notifyDataSetChanged()
    }
}

// ── HistoryFragment ───────────────────────────────────────────
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DetectionAdapter
    private var allDetections: List<Detection> = emptyList()

    private var activeTimeTab: TextView? = null
    private var activeFilterTab: TextView? = null
    private var activeTimePeriod = "week"   // "week" | "month" | "year"
    private var activeFilter     = "all"    // "all"  | "compliant" | "violation"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTimeTabs()
        setupFilterTabs()
        fetchHistory()
    }

    private fun setupRecyclerView() {
        adapter = DetectionAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun fetchHistory() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvHistory.visibility   = View.GONE
        binding.layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDetections("Bearer $token")
                if (response.isSuccessful) {
                    val raw = response.body() ?: emptyList()
                    allDetections = raw.map { map ->
                        Detection(
                            id           = (map["id"] as? Double)?.toInt() ?: 0,
                            result       = map["result"] as? String ?: "compliant",
                            missing_ppe  = (map["missing_ppe"] as? List<*>)?.filterIsInstance<String>(),
                            detected_ppe = (map["detected_ppe"] as? List<*>)?.filterIsInstance<String>(),
                            station      = map["station"] as? String,
                            location     = map["location"] as? String,
                            date         = map["date"] as? String,
                            time         = map["time"] as? String
                        )
                    }
                    applyFilters()
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error fetching history", e)
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun applyFilters() {
        val now = Calendar.getInstance()

        val timeFiltered = allDetections.filter { detection ->
            val dateStr = detection.date ?: return@filter false
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date   = parser.parse(dateStr) ?: return@filter false
                val cal    = Calendar.getInstance().apply { time = date }

                when (activeTimePeriod) {
                    "week"  -> cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
                            && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    "month" -> cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                            && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    "year"  -> cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    else    -> true
                }
            } catch (e: Exception) { false }
        }

        val filtered = when (activeFilter) {
            "compliant" -> timeFiltered.filter { it.result == "compliant" }
            "violation" -> timeFiltered.filter { it.result == "violation" }
            else        -> timeFiltered
        }

        // Update stats based on time-filtered data (not result-filtered)
        val total     = timeFiltered.size
        val compliant = timeFiltered.count { it.result == "compliant" }
        val violation = timeFiltered.count { it.result == "violation" }
        val rate      = if (total > 0) (compliant * 100 / total) else 0

        binding.tvTotalInspections.text = total.toString()
        binding.tvPassed.text           = compliant.toString()
        binding.tvFailed.text           = violation.toString()
        binding.tvPassRate.text         = "$rate%"
        binding.tvRecentActivity.text   = "Recent Activity (${filtered.size})"

        adapter.updateData(filtered)
        binding.layoutEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvHistory.visibility        = if (filtered.isEmpty()) View.GONE    else View.VISIBLE
    }

    private fun setupTimeTabs() {
        setActiveTimeTab(binding.tabThisWeek)
        binding.tabThisWeek.setOnClickListener  { activeTimePeriod = "week";  setActiveTimeTab(binding.tabThisWeek);  applyFilters() }
        binding.tabThisMonth.setOnClickListener { activeTimePeriod = "month"; setActiveTimeTab(binding.tabThisMonth); applyFilters() }
        binding.tabThisYear.setOnClickListener  { activeTimePeriod = "year";  setActiveTimeTab(binding.tabThisYear);  applyFilters() }
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
        setActiveFilterTab(binding.filterAll)
        binding.filterAll.setOnClickListener    { activeFilter = "all";       setActiveFilterTab(binding.filterAll);    applyFilters() }
        binding.filterPassed.setOnClickListener { activeFilter = "compliant"; setActiveFilterTab(binding.filterPassed); applyFilters() }
        binding.filterFailed.setOnClickListener { activeFilter = "violation"; setActiveFilterTab(binding.filterFailed); applyFilters() }
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

    override fun onResume() {
        super.onResume()
        fetchHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}