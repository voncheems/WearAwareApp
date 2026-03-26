package com.example.wearawarer

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wearawarer.databinding.FragmentTeamBinding
import kotlinx.coroutines.launch

// ── Worker Adapter ────────────────────────────────────────────
class WorkerAdapter(
    private var workers: List<Worker>
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView        = view.findViewById(R.id.tvAvatar)
        val tvName: TextView          = view.findViewById(R.id.tvWorkerName)
        val tvPosition: TextView      = view.findViewById(R.id.tvWorkerPosition)
        val tvStation: TextView       = view.findViewById(R.id.tvWorkerStation)
        val tvStatus: TextView        = view.findViewById(R.id.tvWorkerStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]

        // Avatar initials
        val initials = worker.full_name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("")
        holder.tvAvatar.text = initials

        holder.tvName.text     = worker.full_name
        holder.tvPosition.text = worker.position ?: "No position"
        holder.tvStation.text  = worker.station_label ?: ""

        // Status badge
        when (worker.status) {
            "active" -> {
                holder.tvStatus.text = "● Active"
                holder.tvStatus.setTextColor(Color.parseColor("#166534"))
                setBackground(holder.tvStatus, "#DCFCE7", 20f)
            }
            "on_leave" -> {
                holder.tvStatus.text = "● On Leave"
                holder.tvStatus.setTextColor(Color.parseColor("#92400E"))
                setBackground(holder.tvStatus, "#FEF3C7", 20f)
            }
            else -> {
                holder.tvStatus.text = "● Terminated"
                holder.tvStatus.setTextColor(Color.parseColor("#991B1B"))
                setBackground(holder.tvStatus, "#FEE2E2", 20f)
            }
        }
    }

    private fun setBackground(view: View, colorHex: String, radiusDp: Float) {
        val radius = radiusDp * view.resources.displayMetrics.density
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(Color.parseColor(colorHex))
        }
    }

    override fun getItemCount() = workers.size

    fun updateData(newWorkers: List<Worker>) {
        workers = newWorkers
        notifyDataSetChanged()
    }
}

// ── TeamFragment ──────────────────────────────────────────────
class TeamFragment : Fragment() {

    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WorkerAdapter
    private var allWorkers: List<Worker> = emptyList()
    private var activeFilter = "all"
    private var activeFilterTab: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterTabs()
        setupAddMember()
        fetchWorkers()
    }

    private fun setupRecyclerView() {
        adapter = WorkerAdapter(emptyList())
        binding.rvWorkers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkers.adapter = adapter
    }

    private fun setupFilterTabs() {
        val tabs = listOf(binding.filterAll, binding.filterActive, binding.filterOnLeave)
        setActiveFilterTab(binding.filterAll)
        binding.filterAll.setOnClickListener    { activeFilter = "all";      setActiveFilterTab(binding.filterAll);      applyFilter() }
        binding.filterActive.setOnClickListener { activeFilter = "active";   setActiveFilterTab(binding.filterActive);   applyFilter() }
        binding.filterOnLeave.setOnClickListener{ activeFilter = "on_leave"; setActiveFilterTab(binding.filterOnLeave);  applyFilter() }
    }

    private fun setActiveFilterTab(tab: TextView) {
        activeFilterTab?.apply {
            setTextColor(Color.parseColor("#64748B"))
            background = null
        }
        tab.setTextColor(Color.WHITE)
        tab.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f * resources.displayMetrics.density
            setColor(Color.parseColor("#6366F1"))
        }
        activeFilterTab = tab
    }

    private fun applyFilter() {
        val filtered = when (activeFilter) {
            "active"   -> allWorkers.filter { it.status == "active" }
            "on_leave" -> allWorkers.filter { it.status == "on_leave" }
            else       -> allWorkers
        }
        adapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun fetchWorkers() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvWorkers.visibility   = View.GONE
        binding.layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyWorkers("Bearer $token")
                if (response.isSuccessful) {
                    allWorkers = response.body() ?: emptyList()
                    updateStats()
                    applyFilter()
                }
            } catch (e: Exception) {
                Log.e("TeamFragment", "Error fetching workers", e)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.rvWorkers.visibility   = View.VISIBLE
            }
        }
    }

    private fun updateStats() {
        binding.tvTotalMembers.text = allWorkers.size.toString()
        binding.tvActiveNow.text    = allWorkers.count { it.status == "active" }.toString()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvWorkers.visibility        = if (isEmpty) View.GONE    else View.VISIBLE
    }

    private fun setupAddMember() {
        binding.btnAddMember.setOnClickListener {
            showAddMemberDialog()
        }
    }

    private fun showAddMemberDialog() {
        val prefs = requireContext().getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        // Fetch unassigned workers and stations in parallel, then show dialog
        lifecycleScope.launch {
            try {
                val unassignedResponse = RetrofitClient.instance.getUnassignedWorkers("Bearer $token")
                val stationsResponse   = RetrofitClient.instance.getStations("Bearer $token")

                val unassigned = unassignedResponse.body() ?: emptyList()
                val stations   = stationsResponse.body()   ?: emptyList()

                if (unassigned.isEmpty()) {
                    Toast.makeText(requireContext(), "No unassigned workers available", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                if (stations.isEmpty()) {
                    Toast.makeText(requireContext(), "You have no stations assigned", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                showAssignDialog(unassigned, stations, token)

            } catch (e: Exception) {
                Log.e("TeamFragment", "Error loading dialog data", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAssignDialog(unassigned: List<Worker>, stations: List<Station>, token: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_assign_worker, null)
        val spinnerWorker  = dialogView.findViewById<Spinner>(R.id.spinnerWorker)
        val spinnerStation = dialogView.findViewById<Spinner>(R.id.spinnerStation)

        // Populate worker spinner
        val workerNames = unassigned.map { "${it.full_name} (${it.employee_id})" }
        spinnerWorker.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, workerNames)

        // Populate station spinner
        val stationNames = stations.map { it.label }
        spinnerStation.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stationNames)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Team Member")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val selectedWorker  = unassigned[spinnerWorker.selectedItemPosition]
                val selectedStation = stations[spinnerStation.selectedItemPosition]
                assignWorker(selectedWorker, selectedStation, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun assignWorker(worker: Worker, station: Station, token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.assignWorker(
                    "Bearer $token",
                    worker.id,
                    AssignWorkerRequest(station_id = station.id)
                )
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "${worker.full_name} added to ${station.label}!", Toast.LENGTH_SHORT).show()
                    fetchWorkers() // Refresh the list
                } else {
                    val msg = response.errorBody()?.string() ?: "Failed to assign worker"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TeamFragment", "Error assigning worker", e)
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchWorkers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}