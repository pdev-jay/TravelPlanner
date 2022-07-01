package com.jccgs.travelplanner_v2.sjeong

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.ktx.awaitMap
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.jkim.DailyPlan
import com.jccgs.travelplanner_v2.jkim.MapController
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.clustering.ClusterManager
import com.jccgs.travelplanner_v2.ckim.ChecklistActivity_CKim
import com.jccgs.travelplanner_v2.cyun.MainActivity_CYun
import com.jccgs.travelplanner_v2.databinding.ActivityDailyPlanSjeongBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong.Companion.documentId
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong.Companion.stringDayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DailyPlanActivity_SJeong : AppCompatActivity(), OnMapReadyCallback{

    val binding by lazy { ActivityDailyPlanSjeongBinding.inflate(layoutInflater) }
    val tabLayout by lazy { binding.dayTab }

    // 구글 맵
    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    // 계획
    var dailyPlan: MutableList<DailyPlan> = mutableListOf<DailyPlan>() // 전체 일정
    var selectDayPlan = mutableListOf<DailyPlan>() // 선택된 날짜 일정
    var selectDay = ""

//    var order = 0

    // 리사이클러뷰 어댑터
    lateinit var placeAdapter: DailyPlanAdapter_SJeong

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 날짜 탭 생성
        for(num in 1..stringDayList.size) {
            createTab(num, stringDayList[num-1])
        }

        // 여행 지역을 선택하지 않았을 경우
        if(MapController.selectedPlaceCountryName.isNullOrEmpty()) {
            binding.tvMainPlace.text = "여행 지역"
        }else if(MapController.selectedPlaceCity.isNullOrEmpty()) {
            binding.tvMainPlace.text = "${MapController.selectedPlaceCountryName}"
        }else {
            binding.tvMainPlace.text = """${MapController.selectedPlaceCountryName}
                |${MapController.selectedPlaceCity}
            """.trimMargin()
        }
        selectDay = stringDayList.first()

        // 지도
        lifecycle.coroutineScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            googleMap = mapFragment.awaitMap()
            mapFragment.getMapAsync(this@DailyPlanActivity_SJeong)
        }

        // 처음 화면 - day1
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = DailyPlanAdapter_SJeong(selectDayPlan, dailyPlan)
        binding.recyclerView.adapter = placeAdapter

        // 검색
        search()

        // 날짜 탭으로 화면 전환
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            // 선택될 때
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectDay = stringDayList[tab!!.id - 1]
                filterDay(selectDay)
                placeAdapter.notifyDataSetChanged()
            }

            // 선택 해제될 때
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            // 다시 선택할 때
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        // 완료
        binding.btnDone.setOnClickListener {
            val intent = Intent(this, MainActivity_CYun::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        // 하단 버튼을 통해 다른 액티비티로 이동
        binding.buttomBtnLayout.btnPlan.setBackgroundColor(Color.parseColor("#C3B8D9"))
        binding.buttomBtnLayout.btnCheckList.setBackgroundColor(Color.WHITE)
        binding.buttomBtnLayout.btnExpenses.setBackgroundColor(Color.WHITE)
        binding.buttomBtnLayout.btnCheckList.setOnClickListener {
            startActivity(Intent(this, ChecklistActivity_CKim::class.java))
        }

        binding.buttomBtnLayout.btnExpenses.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
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

    // 탭 생성
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

    // 날짜 필터
    fun filterDay(selectDay: String) {
        selectDayPlan.clear()
        selectDayPlan.addAll(dailyPlan.filter { it.date.equals(selectDay) })
    }

    // 검색
    fun search() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.search_fragment) as AutocompleteSupportFragment
//        autocompleteFragment.setCountry(MapController.selectedPlaceShortName)
        autocompleteFragment.setActivityMode(AutocompleteActivityMode.FULLSCREEN)
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val geocoder = Geocoder(this@DailyPlanActivity_SJeong)

                val specificPlace = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)
                val countryName = specificPlace[0].countryName

                Log.i("log", "Place: ${place.name}, ${place.id}, ${place.latLng}, ${countryName}")
                CoroutineScope(Dispatchers.Main).launch {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15f))
                    mapController.addMark(listOf(place.latLng))
                }

                MapController.selectedPlaceLatLng = place.latLng
                MapController.selectedPlaceName = place.name
                MapController.selectedPlaceAddress = place.address
            }

            override fun onError(status: Status) {
                Log.i("log", "An error occurred: $status")
            }
        })
    }

    fun addPlace(newDailyPlan: DailyPlan) {
//        val newDailyPlan = DailyPlan(null, selectDay, MapController.selectedPlaceName!!, MapController.selectedPlaceAddress!!, MapController.selectedPlaceLatLng!!.latitude, MapController.selectedPlaceLatLng!!.longitude)
        dailyPlan.add(newDailyPlan)
        selectDayPlan.add(newDailyPlan)
        placeAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("알림")
            setIcon(R.drawable.ic_baseline_info_24)
            setMessage("""메인 화면으로 돌아가시겠습니까?
                 |*현재 저장된 정보가 삭제됩니다.""".trimMargin())
            setPositiveButton("확인") { _, _ ->
                deleteCurrentPlan()
            }
            setNegativeButton("취소", null)
            show()
        }
    }

    fun deleteCurrentPlan(){
        documentId?.let { FirebaseController.PLAN_REF.document(it).delete().addOnSuccessListener {
            val intent = Intent(this@DailyPlanActivity_SJeong, MainActivity_CYun::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } }
    }

    // 구글 맵
    override fun onMapReady(p0: GoogleMap) {
        mapController = MapController(this, googleMap)

        googleMap.setOnCameraIdleListener(mapController.clusterManager)
        googleMap.setOnPoiClickListener(mapController)
        googleMap.setOnMarkerClickListener(mapController.clusterManager)

        CoroutineScope(Dispatchers.Main).launch {
            MapController.selectedPlaceLatLng?.let { moveCamera(it) }
        }

        mapController.clusterManager.setOnClusterItemInfoWindowLongClickListener {
            //long click시 이벤트 발생
            Log.d("Log_debug", "cluste info window long click")
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
            true
        }

    }

    fun moveCamera(latLng: LatLng){
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mapController.addMark(listOf(latLng))
    }

}