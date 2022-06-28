package com.jccgs.travelplanner_v2.sjeong

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.PlaceItemSjeongBinding
import com.jccgs.travelplanner_v2.jkim.DailyPlan

// 뷰홀더 클래스
class PlaceViewHolder(val bindnig: PlaceItemSjeongBinding): RecyclerView.ViewHolder(bindnig.root)

class DailyPlanAdapter_SJeong(val dailyPlan: MutableList<DailyPlan>): RecyclerView.Adapter<PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val placeBinding = PlaceItemSjeongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = PlaceViewHolder(placeBinding)

        return viewHolder
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val binding = holder.bindnig
        val placeItem = dailyPlan[position]
        binding.ivIcon.setImageResource(R.drawable.icon_place_sjeong)
        binding.tvPlace.text = placeItem.placeName
        binding.tvAdr.text = placeItem.placeAddress
    }

    override fun getItemCount(): Int = dailyPlan.size
}