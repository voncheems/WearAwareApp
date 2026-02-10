package com.example.wearawarer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OnboardingViewPagerAdapter(private val layouts: IntArray) :
    RecyclerView.Adapter<OnboardingViewPagerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layouts[viewType], parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Slides are already designed in XML, no binding needed
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int = position
}
