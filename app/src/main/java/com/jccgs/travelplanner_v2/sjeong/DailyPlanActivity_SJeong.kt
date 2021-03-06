package com.jccgs.travelplanner_v2.sjeong

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.ckim.ChecklistActivity_CKim
import com.jccgs.travelplanner_v2.cyun.DetailActivity_CYun
import com.jccgs.travelplanner_v2.cyun.MainActivity_CYun
import com.jccgs.travelplanner_v2.databinding.ActivityDailyPlanSjeongBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import com.jccgs.travelplanner_v2.jkim.DailyPlan
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.MapController
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong.Companion.documentId
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong.Companion.stringDayList

class DailyPlanActivity_SJeong : AppCompatActivity(), OnMapReadyCallback{

    val binding by lazy { ActivityDailyPlanSjeongBinding.inflate(layoutInflater) }
    val tabLayout by lazy { binding.dayTab }

    // ?????? ???
    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    // ??????
    var dailyPlan: MutableList<DailyPlan> = mutableListOf<DailyPlan>() // ?????? ??????
    var selectDayPlan = mutableListOf<DailyPlan>() // ????????? ?????? ??????
    var selectDay = ""

//    var order = 0

    // ?????????????????? ?????????
    lateinit var placeAdapter: DailyPlanAdapter_SJeong

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // ?????? ??? ??????
        for(num in 1..stringDayList.size) {
            createTab(num, stringDayList[num-1])
        }

        Log.d("log", "??????: ${MapController.selectedPlaceCountryName}, ??????: ${MapController.selectedPlaceCity}")

        // ?????? ????????? ???????????? ????????? ??????
        if(MapController.selectedPlaceCountryName.isNullOrEmpty()) {
            binding.tvMainPlace.text = "?????? ??????"
        }else if(MapController.selectedPlaceCity.isNullOrEmpty() || MapController.selectedPlaceCountryName == MapController.selectedPlaceCity) {
            binding.tvMainPlace.text = "${MapController.selectedPlaceCountryName}"
        }else {
            binding.tvMainPlace.text = """${MapController.selectedPlaceCountryName}
                |${MapController.selectedPlaceCity}
            """.trimMargin()
        }
        selectDay = stringDayList.first()

        // ??????
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@DailyPlanActivity_SJeong)

        // ?????? ?????? - day1
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = DailyPlanAdapter_SJeong(selectDayPlan, dailyPlan)
        binding.recyclerView.adapter = placeAdapter

        // ??????

        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.search_fragment) as AutocompleteSupportFragment
        autocompleteFragment.apply {
            setCountry(MapController.selectedPlaceShortName)
            setActivityMode(AutocompleteActivityMode.FULLSCREEN)
            setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS))
            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {

                    MapController.selectedPlaceLatLng = place.latLng
                    MapController.selectedPlaceName = place.name
                    MapController.selectedPlaceAddress = place.address

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15f))
                    mapController.addMark(listOf(place.latLng))
                }

                override fun onError(status: Status) {
                    Log.i("log", "An error occurred: $status")
                }
            })
        }

        // ?????? ????????? ?????? ??????
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            // ????????? ???
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectDay = stringDayList[tab!!.id - 1]
                filterDay(selectDay)
                placeAdapter.notifyDataSetChanged()
            }

            // ?????? ????????? ???
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            // ?????? ????????? ???
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        // ??????
        binding.btnDoneDailyPlan.setOnClickListener {
            val intent = Intent(this, MainActivity_CYun::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        // ?????? ????????? ?????? ?????? ??????????????? ??????
        binding.buttomBtnLayout.btnPlan.setBackgroundColor(Color.parseColor("#C3B8D9"))
        binding.buttomBtnLayout.btnCheckList.setBackgroundColor(Color.WHITE)
        binding.buttomBtnLayout.btnExpenses.setBackgroundColor(Color.WHITE)
        binding.buttomBtnLayout.btnCheckList.setOnClickListener {
            startActivity(Intent(this, ChecklistActivity_CKim::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }

        binding.buttomBtnLayout.btnExpenses.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }

        binding.btnAddDailyPlan.setOnClickListener{
            if (!MapController.selectedPlaceName.isNullOrBlank()) {
                addDailyPlan()
            }
        }
    }

    override fun onStart() {
        FirebaseController.PLAN_REF
            .document(documentId.toString())
            .collection("DailyPlan")
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                dailyPlan.clear()
                for (i in snapshot){
                    dailyPlan.add(i.toObject<DailyPlan>())
                }
                filterDay(selectDay)
                placeAdapter.notifyDataSetChanged()
            }
        super.onStart()
    }

    // ??? ??????
    fun createTab(num: Int, date: String) {
        val tab: TabLayout.Tab = tabLayout.newTab()
        val dayNView = TextView(this)
        val dateView = TextView(this)
        dayNView.textSize = 18f
        dayNView.typeface = Typeface.DEFAULT_BOLD
        dateView.textSize = 13f
        dayNView.gravity = Gravity.CENTER
        dateView.gravity = Gravity.CENTER
        dayNView.text = "DAY${num}"
        dateView.text = "${date}"
        tabLayout.addTab(tab.setCustomView(dayNView).setCustomView(dateView))
        tab.id = num
    }

    // ?????? ??????
    fun filterDay(selectDay: String) {
        selectDayPlan.clear()
        selectDayPlan.addAll(dailyPlan.filter { it.date.equals(selectDay) })
    }

    fun addPlace(newDailyPlan: DailyPlan) {
        dailyPlan.add(newDailyPlan)
        selectDayPlan.add(newDailyPlan)
        placeAdapter.notifyDataSetChanged()
    }

    fun addDailyPlan(){
        var newDailyPlan = DailyPlan(null, 0, selectDay, MapController.selectedPlaceName.toString(), MapController.selectedPlaceAddress.toString(), MapController.selectedPlaceLatLng!!.latitude, MapController.selectedPlaceLatLng!!.longitude)

        if (!dailyPlan.isNullOrEmpty()){
            newDailyPlan.order = dailyPlan.last().order + 1
        }

        documentId?.let { documentId ->
            FirebaseController
                .PLAN_REF
                .document(documentId)
                .collection("DailyPlan")
                .add(newDailyPlan)
                .addOnSuccessListener { docRef ->
                    docRef.update("id", docRef.id)
                        .addOnSuccessListener {
                            newDailyPlan.id = docRef.id
                            addPlace(newDailyPlan)
                        }
                }
        }
    }

    override fun onBackPressed() {
        if (!DetailActivity_CYun.editMode) {
            AlertDialog.Builder(this).apply {
                setTitle("??????")
                setIcon(R.drawable.ic_baseline_info_24)
                setMessage(
                    """?????? ???????????? ?????????????????????????
                 |*?????? ????????? ????????? ???????????????.""".trimMargin()
                )
                setPositiveButton("??????") { _, _ ->
                    deleteCurrentPlan()
                }
                setNegativeButton("??????", null)
                show()
            }
            return
        }
        super.onBackPressed()
    }

    fun deleteCurrentPlan(){
        documentId?.let { FirebaseController.PLAN_REF.document(it).delete().addOnSuccessListener {
            val intent = Intent(this@DailyPlanActivity_SJeong, MainActivity_CYun::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } }
    }

    // ?????? ???
    override fun onMapReady(googleMap: GoogleMap) {
        mapController = MapController(this, googleMap)
        this.googleMap = googleMap
        this.googleMap.setOnCameraIdleListener(mapController.clusterManager)
        this.googleMap.setOnPoiClickListener(mapController)
        this.googleMap.setOnMarkerClickListener(mapController.clusterManager)

        MapController.selectedPlaceLatLng?.let {
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            val initialMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(MapController.selectedPlaceName)
                    .snippet(MapController.selectedPlaceAddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            ))
            initialMarker?.showInfoWindow()
        }

        mapController.clusterManager.renderer = mapController.renderer
    }

    fun moveCamera(dailyPlan: DailyPlan){
        val latLng = LatLng(dailyPlan.placeLat, dailyPlan.placeLng)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mapController.addDailyPlanMarkers(mutableListOf(dailyPlan))
    }
}