package com.jccgs.travelplanner_v2.cyun

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.jccgs.travelplanner_v2.databinding.DetailItemViewBinding
import com.jccgs.travelplanner_v2.jkim.DailyPlan


class DetailAdapter_CYun(val context: Context, val dailyPlans: MutableList<DailyPlan>): RecyclerView.Adapter<DetailAdapter_CYun.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailAdapter_CYun.ViewHolder {
        val binding = DetailItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailAdapter_CYun.ViewHolder, position: Int) {
        val binding = holder.binding
        binding.tvDateDetail.text = dailyPlans[position].date
        binding.tvPlaceNameDetail.text = dailyPlans[position].placeName
    }

    override fun getItemCount(): Int {
        return dailyPlans.size
    }

    inner class ViewHolder(val binding: DetailItemViewBinding): RecyclerView.ViewHolder(binding.root)
}