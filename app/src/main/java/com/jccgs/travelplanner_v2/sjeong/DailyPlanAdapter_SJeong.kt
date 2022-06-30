package com.jccgs.travelplanner_v2.sjeong

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.PlaceItemSjeongBinding
import com.jccgs.travelplanner_v2.jkim.DailyPlan
import com.jccgs.travelplanner_v2.jkim.FirebaseController

// 뷰홀더 클래스
class PlaceViewHolder(val bindnig: PlaceItemSjeongBinding): RecyclerView.ViewHolder(bindnig.root)

class DailyPlanAdapter_SJeong(val dailyPlan: MutableList<DailyPlan>): RecyclerView.Adapter<PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val placeBinding = PlaceItemSjeongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = PlaceViewHolder(placeBinding)

        // 아이템 클릭 시 지도 카메라 이동
        viewHolder.itemView.setOnClickListener {
            val placeItem = dailyPlan[viewHolder.adapterPosition]
            val lat = placeItem.placeLat
            val lng = placeItem.placeLng
            val latLng = LatLng(lat, lng)
            (parent.context as DailyPlanActivity_SJeong).moveCamera(latLng)
            Log.d("log", "클릭: ${placeItem.placeName}")
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val binding = holder.bindnig
        val placeItem = dailyPlan[position]
        binding.ivIcon.setImageResource(R.drawable.icon_place_sjeong)
        binding.tvPlace.text = placeItem.placeName
        binding.tvAdr.text = placeItem.placeAddress

        binding.ivPlaceDel.setOnClickListener {
            FirebaseController.PLAN_REF
                .document(CalendarActivity_SJeong.documentId.toString())
                .collection("DailyPlan")
                .document(dailyPlan[position].id.toString())
                .delete()
                .addOnSuccessListener {

                }
        }
    }

    override fun getItemCount(): Int = dailyPlan.size
}