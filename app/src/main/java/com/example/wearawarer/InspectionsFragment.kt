package com.example.wearawarer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wearawarer.databinding.FragmentInspectionsBinding

data class Inspection(
    val siteName: String,
    val location: String,
    val scheduledTime: String,
    val tags: List<String>,
    val isCompleted: Boolean
)

class InspectionsAdapter(private var items: List<Inspection>) :
    RecyclerView.Adapter<InspectionsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSiteName: TextView = itemView.findViewById(R.id.tvSiteName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvScheduled: TextView = itemView.findViewById(R.id.tvScheduled)
        val tvTag1: TextView = itemView.findViewById(R.id.tvTag1)
        val tvTag2: TextView = itemView.findViewById(R.id.tvTag2)
        val tvTagMore: TextView = itemView.findViewById(R.id.tvTagMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inspection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvSiteName.text = item.siteName
        holder.tvLocation.text = item.location
        holder.tvScheduled.text = item.scheduledTime

        holder.tvTag1.text = item.tags.getOrElse(0) { "" }
        holder.tvTag2.text = item.tags.getOrElse(1) { "" }
        holder.tvTag2.visibility = if (item.tags.size >= 2) View.VISIBLE else View.GONE

        val extraCount = item.tags.size - 2
        holder.tvTagMore.visibility = if (extraCount > 0) View.VISIBLE else View.GONE
        holder.tvTagMore.text = "+$extraCount"
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Inspection>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class InspectionsFragment : Fragment() {

    private var _binding: FragmentInspectionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: InspectionsAdapter
    private var showingPending = true

    private val allInspections = listOf(
        Inspection("Site A - Zone 4", "Construction Area", "Scheduled 2 hours ago", listOf("Hard Hat", "Safety Vest", "Gloves"), false),
        Inspection("Site B - Zone 4", "Construction Area", "Scheduled 2 hours ago", listOf("Hard Hat", "Safety Vest", "Goggles"), false),
        Inspection("Site C - Zone 4", "Construction Area", "Scheduled 2 hours ago", listOf("Hard Hat", "Safety Vest", "Boots"), false),
        Inspection("Site D - Zone 2", "Warehouse", "Scheduled 4 hours ago", listOf("Hard Hat", "Gloves"), false),
        Inspection("Site A - Zone 1", "Main Entrance", "Completed 1 hour ago", listOf("Hard Hat", "Safety Vest"), true),
        Inspection("Site B - Zone 2", "Warehouse", "Completed 3 hours ago", listOf("Hard Hat", "Gloves", "Goggles"), true),
        Inspection("Site C - Zone 1", "Construction Area", "Completed yesterday", listOf("Safety Vest", "Boots"), true),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInspectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        showPending()
    }

    private fun setupRecyclerView() {
        adapter = InspectionsAdapter(emptyList())
        binding.rvInspections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInspections.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabPending.setOnClickListener { if (!showingPending) showPending() }
        binding.tabCompleted.setOnClickListener { if (showingPending) showCompleted() }
    }

    private fun showPending() {
        showingPending = true
        binding.tvSectionLabel.text = "Pending Inspections"
        binding.tabPending.setBackgroundResource(R.drawable.bg_tab_active)
        binding.tabPending.setTextColor(Color.WHITE)
        binding.tabCompleted.setBackgroundColor(Color.TRANSPARENT)
        binding.tabCompleted.setTextColor(Color.parseColor("#64748B"))
        adapter.updateItems(allInspections.filter { !it.isCompleted })
    }

    private fun showCompleted() {
        showingPending = false
        binding.tvSectionLabel.text = "Completed Inspections"
        binding.tabCompleted.setBackgroundResource(R.drawable.bg_tab_active)
        binding.tabCompleted.setTextColor(Color.WHITE)
        binding.tabPending.setBackgroundColor(Color.TRANSPARENT)
        binding.tabPending.setTextColor(Color.parseColor("#64748B"))
        adapter.updateItems(allInspections.filter { it.isCompleted })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}