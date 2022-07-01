package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.ktx.awaitMap
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityDetailCyunBinding
import com.jccgs.travelplanner_v2.databinding.BottomSheetMapBinding
import com.jccgs.travelplanner_v2.jkim.*
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*

class DetailActivity_CYun : AppCompatActivity(), OnMapReadyCallback {
    companion object{
        var editMode = false
    }

    lateinit var binding: ActivityDetailCyunBinding
    lateinit var customAdapter: DetailAdapter_CYun

    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap


    val bottomSheetBinding: BottomSheetMapBinding by lazy {
        binding.bottomSheetDetail
    }
    private lateinit var sheetBehavior: BottomSheetBehavior<ConstraintLayout>

    lateinit var selectedPlan: Plan
    var dailyPlans: MutableList<DailyPlan> = mutableListOf()
    var expensesList: MutableList<Expenses> = mutableListOf()
    var checkList: MutableList<CheckList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)
        editMode = false

        sheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(binding.bottomSheetDetail.root)
//        CalendarActivity_SJeong.startDate = Calendar.getInstance().set(year, month, date)

        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        sheetBehavior.isDraggable = false
        sheetBehavior.peekHeight = 0
        sheetBehavior.isHideable = true
        bottomSheetBinding.btnDismissMap.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        if (intent.hasExtra("selectedPlan")){
            selectedPlan = intent.getSerializableExtra("selectedPlan") as Plan
        }

        binding.tvLocation.text = selectedPlan.country
        binding.tvDurationTitle.text = "${selectedPlan.period.first()} ~ ${selectedPlan.period.last()}"



        binding.recyclerViewDetail.layoutManager = LinearLayoutManager(this)
        customAdapter = DetailAdapter_CYun(this, dailyPlans)
        binding.recyclerViewDetail.adapter = customAdapter

        binding.btnShowMap.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        lifecycle.coroutineScope.launchWhenCreated {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_bottom) as SupportMapFragment
            mapFragment.getMapAsync(this@DetailActivity_CYun)
        }

        binding.btnDeleteDetail.setOnClickListener{
            AlertDialog.Builder(this).apply {
                setTitle("알림")
                setIcon(R.drawable.ic_baseline_info_24)
                setMessage("""정말로 이 일정을 삭제하시겠습니까 ?""".trimMargin())
                setPositiveButton("확인") { _, _ ->
                    deletePlan()
                }
                setNegativeButton("취소", null)
                show()
            }
        }

        binding.btnEditDetail.setOnClickListener {
            editMode = true
            prepareToEdit()
        }
    }

    override fun onStart() {
        dailyPlans.clear()
        expensesList.clear()
        checkList.clear()
        getDailyPlan(selectedPlan.id.toString())
        getExpenses(selectedPlan.id.toString())
        getCheckList(selectedPlan.id.toString())

        super.onStart()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("Log_debug", "onMapReady")

        this.googleMap = googleMap
        mapController = MapController(this, this.googleMap)

        this.googleMap.setOnCameraIdleListener(mapController.clusterManager)
        this.googleMap.setOnMarkerClickListener(mapController.clusterManager)
    }


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
                mapController.addDailyPlanMarkers(dailyPlans)
                if (dailyPlans.isNotEmpty()){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dailyPlans.first().placeLat, dailyPlans.first().placeLng), 13f))
                }
            }
            .addOnFailureListener {
                Log.d("Log_debug", "dailyPlan = failed : $it")
            }
    }

    fun getExpenses(selectedPlanId: String){
        FirebaseController.PLAN_REF
            .document(selectedPlanId)
            .collection("Expenses")
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                var totalCost = 0
                if (!snapshot.isEmpty){
                    for(i in snapshot){
                        val expenses = i.toObject<Expenses>()
                        expensesList.add(expenses)
                        totalCost += expenses.cost
                    }
                }
                binding.tvCost.text = "₩ ${totalCost}"
            }
            .addOnFailureListener {
                Log.d("Log_debug", "dailyPlan = failed : $it")
            }
    }

    fun getCheckList(selectedPlanId: String){
        FirebaseController.PLAN_REF
            .document(selectedPlanId)
            .collection("CheckList")
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty){
                    for(i in snapshot){
                        val checkListItem = i.toObject<CheckList>()
                        checkList.add(checkListItem)
                    }
                }
                binding.tvCheckListCount.text = "${checkList.size} 개"
            }
            .addOnFailureListener {
                Log.d("Log_debug", "dailyPlan = failed : $it")
            }
    }

    fun deletePlan(){
        selectedPlan.id?.let { FirebaseController.PLAN_REF.document(it).delete().addOnSuccessListener {
            val intent = Intent(this, MainActivity_CYun::class.java)
            startActivity(intent)
            finish()
        }}
    }

    fun calDate(index: Int): Calendar{
        val date = selectedPlan.period[index].split("-")
        val planStartDate = Calendar.getInstance()
        planStartDate.set(date[0].toInt(), date[1].toInt(), date[2].toInt())
        return planStartDate
    }

    fun setPlaceInfo(){
        MapController.selectedPlaceCountryName = selectedPlan.country
        MapController.selectedPlaceCity = selectedPlan.city
        MapController.selectedPlaceLatLng = LatLng(dailyPlans.first().placeLat, dailyPlans.first().placeLng)
        MapController.selectedPlaceName = dailyPlans.first().placeName
        MapController.selectedPlaceAddress = dailyPlans.first().placeAddress
        MapController.selectedPlaceShortName = selectedPlan.countryCode
    }

    fun prepareToEdit(){
        val startDate = calDate(0)
        val endDate = calDate(selectedPlan.period.size - 1)
        CalendarActivity_SJeong.startDate = startDate
        CalendarActivity_SJeong.endDate = endDate
        CalendarActivity_SJeong.documentId = selectedPlan.id
        CalendarActivity_SJeong.stringDayList = selectedPlan.period
        if (dailyPlans.isNotEmpty()) {
            setPlaceInfo()
        }

        val intent = Intent(this, DailyPlanActivity_SJeong::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }
}