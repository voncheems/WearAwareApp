package com.example.wearawarer

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wearawarer.databinding.FragmentTeamBinding

class TeamFragment : Fragment() {

    private var _binding: FragmentTeamBinding? = null
    private val binding get() = _binding!!

    private var activeFilterTab: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFilterTabs()
        setupAddMember()
    }

    private fun setupFilterTabs() {
        val tabs = listOf(binding.filterAll, binding.filterActive, binding.filterInspectors)
        setActiveFilterTab(binding.filterAll)
        tabs.forEach { tab ->
            tab.setOnClickListener {
                setActiveFilterTab(tab)
            }
        }
    }

    private fun setActiveFilterTab(tab: TextView) {
        activeFilterTab?.apply {
            setTextColor(Color.parseColor("#64748B"))
            background = null
        }
        tab.setTextColor(Color.WHITE)
        // Updated to WearAware Purple
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

    private fun setupAddMember() {
        binding.btnAddMember.setOnClickListener {
            Toast.makeText(requireContext(), "Add Team Member coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
