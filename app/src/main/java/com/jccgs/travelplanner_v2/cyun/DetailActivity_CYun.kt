package com.jccgs.travelplanner_v2.cyun

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityDetailCyunBinding
import com.jccgs.travelplanner_v2.jkim.DailyPlan
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.Plan

class DetailActivity_CYun : AppCompatActivity() {
    lateinit var binding: ActivityDetailCyunBinding
    lateinit var customAdapter: DetailAdapter_CYun
//    val itemViewData: MutableList<ItemViewData> = mutableListOf<ItemViewData>()

    lateinit var selectedPlan: Plan
    var dailyPlans: MutableList<DailyPlan> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra("selectedPlan")){
            selectedPlan = intent.getSerializableExtra("selectedPlan") as Plan
        }

        getDailyPlan(selectedPlan.id.toString())

        binding.tvLocation.text = selectedPlan.mainPlace
        binding.tvDurationTitle.text = "${selectedPlan.period.first()} ~ ${selectedPlan.period.last()}"



        binding.recyclerViewDetail.layoutManager = LinearLayoutManager(this)
        customAdapter = DetailAdapter_CYun(this, dailyPlans)
        binding.recyclerViewDetail.adapter = customAdapter

//
//        val callback = ItemTouchHelperCallback(customAdapter)
//        val touchHelper = ItemTouchHelper(callback)
//
//        touchHelper.attachToRecyclerView(binding.recyclerViewDetail)
//        binding.recyclerViewDetail.adapter = customAdapter
//
//        customAdapter.startDrag(object : CustomAdapter_CYun.OnStartDragListener {
//            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
//                touchHelper.startDrag(viewHolder)
//            }
//        })
    }

//    fun onClick(view: View) {
//        when (view.id) {
//            R.id.ivCorrect -> {
//                binding.ivCorrect.visibility = View.INVISIBLE
//                binding.ivCheck.visibility = View.VISIBLE
//                binding.tvLocation.visibility = View.INVISIBLE
//                binding.edtLocation.visibility = View.VISIBLE
//
//            }
//            R.id.ivCheck -> {
//                binding.ivCheck.visibility = View.INVISIBLE
//                binding.ivCorrect.visibility = View.VISIBLE
//                binding.tvLocation.text = binding.edtLocation.text
//                binding.edtLocation.visibility = View.INVISIBLE
//                binding.tvLocation.visibility = View.VISIBLE
//
//            }
//            R.id.ivCorrect2 -> {
//                binding.ivCorrect2.visibility = View.INVISIBLE
//                binding.ivCheck2.visibility = View.VISIBLE
//                binding.tvCost.visibility = View.INVISIBLE
//                binding.edtCost.visibility = View.VISIBLE
//
//            }
//            R.id.ivCheck2 -> {
//                binding.ivCheck2.visibility = View.INVISIBLE
//                binding.ivCorrect2.visibility = View.VISIBLE
//                binding.tvCost.text = binding.edtCost.text
//                binding.edtCost.visibility = View.INVISIBLE
//                binding.tvCost.visibility = View.VISIBLE
//
//            }
//        }
//    }

    fun getDailyPlan(selectedPlanId: String){
        FirebaseController.PLAN_REF
            .document(selectedPlanId)
            .collection("DailyPlan")
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty){
                    for(i in snapshot){
                        val dailyPlan = i.toObject<DailyPlan>()
                        dailyPlans.add(dailyPlan)
                        Log.d("Log_debug", "dailyPlan = ${dailyPlan}")
                    }
                }
                customAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("Log_debug", "dailyPlan = failed : $it")

            }
    }

}