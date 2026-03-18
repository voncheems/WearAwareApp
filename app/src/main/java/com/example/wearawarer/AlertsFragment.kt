package com.example.wearawarer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wearawarer.databinding.FragmentAlertsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AlertsAdapter(
    private var alerts: MutableList<NotificationAlert>,
    private val onItemClick: (NotificationAlert) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvAlertTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvAlertMessage)
        val tvTime: TextView = view.findViewById(R.id.tvAlertTime)
        val root: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]

        holder.tvTitle.text = if (alert.result == "violation") "PPE VIOLATION" else "COMPLIANT"

        val missing = alert.missing_ppe?.joinToString(", ") ?: "None"
        holder.tvMessage.text = "Missing: $missing at ${alert.location ?: alert.station ?: "Site Entrance"}"

        holder.tvTime.text = formatTimestamp(alert.created_at)

        holder.root.alpha = if (alert.is_read) 0.6f else 1.0f
        holder.tvTitle.setTypeface(null, if (alert.is_read) android.graphics.Typeface.NORMAL else android.graphics.Typeface.BOLD)

        holder.root.setOnClickListener { onItemClick(alert) }
    }

    private fun formatTimestamp(isoString: String): String {
        if (isoString == "Just Now") return "Just Now"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")            // input is UTC
            val date = parser.parse(isoString.replace("Z", ""))
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("Asia/Manila") // display in PH time
            formatter.format(date!!)
        } catch (e: Exception) {
            isoString.take(10)
        }
    }

    override fun getItemCount() = alerts.size

    fun updateData(newAlerts: List<NotificationAlert>) {
        this.alerts = newAlerts.toMutableList()
        notifyDataSetChanged()
    }

    fun addAlertAtTop(alert: NotificationAlert) {
        if (this.alerts.any { it.id == alert.id && it.id != -1 }) return
        this.alerts.add(0, alert)
        notifyItemInserted(0)
    }
}

class AlertsFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlertsAdapter

    private val alertReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val rawJson = intent?.getStringExtra("raw_data")
            if (rawJson != null) {
                try {
                    val data = JSONObject(rawJson)

                    val missingArray = data.optJSONArray("missing_ppe")
                    val missingList = mutableListOf<String>()
                    if (missingArray != null) {
                        for (i in 0 until missingArray.length()) {
                            missingList.add(missingArray.getString(i))
                        }
                    }

                    val tempAlert = NotificationAlert(
                        id           = data.optInt("notification_id", -1),
                        detection_id = data.optInt("detection_id", -1),
                        is_read      = false,
                        created_at   = "Just Now",
                        result       = data.optString("type", "violation"),
                        missing_ppe  = missingList,
                        photo_url    = data.optString("photo_url").takeIf { it.isNotEmpty() },
                        station      = data.optString("station", "Live Detection"),
                        location     = data.optString("location").takeIf { it.isNotEmpty() }
                    )

                    adapter.addAlertAtTop(tempAlert)
                    binding.rvAlerts.scrollToPosition(0)
                    updateEmptyState(false)

                } catch (e: Exception) {
                    Log.e("AlertsFragment", "Error parsing live alert", e)
                }
            }

            // Sync with DB after 2 seconds to replace the temp card with the real one
            lifecycleScope.launch {
                delay(2000)
                fetchAlerts()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToRefresh()
        fetchAlerts()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(alertReceiver, IntentFilter("com.wearaware.NEW_ALERT"))
    }

    private fun setupRecyclerView() {
        adapter = AlertsAdapter(mutableListOf()) { alert ->
            markAlertAsRead(alert)
        }
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { fetchAlerts() }
        binding.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.primary_purple))
    }

    private fun fetchAlerts() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            binding.swipeRefresh.isRefreshing = false
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getNotifications("Bearer $token")
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val alerts = response.body() ?: emptyList()
                    updateUI(alerts)
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false
                Log.e("AlertsFragment", "Network error", e)
            }
        }
    }

    private fun markAlertAsRead(alert: NotificationAlert) {
        if (alert.is_read || alert.id == -1) return
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.markAsRead("Bearer $token", alert.id)
                if (response.isSuccessful) fetchAlerts()
            } catch (e: Exception) {
                Log.e("AlertsFragment", "Error marking read", e)
            }
        }
    }

    private fun updateUI(alerts: List<NotificationAlert>) {
        if (!isAdded) return
        updateEmptyState(alerts.isEmpty())
        if (alerts.isNotEmpty()) {
            adapter.updateData(alerts)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvAlerts.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvAlerts.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAlerts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(alertReceiver)
        } catch (e: Exception) {}
        _binding = null
    }
}