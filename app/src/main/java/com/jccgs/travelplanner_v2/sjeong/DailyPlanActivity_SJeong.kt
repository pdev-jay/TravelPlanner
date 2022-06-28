package com.jccgs.travelplanner_v2.sjeong

import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.Place
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.jccgs.travelplanner_v2.databinding.ActivityDailyPlanSjeongBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DailyPlanActivity_SJeong : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        lateinit var startDate: Calendar
        lateinit var endDate: Calendar
    }

    val binding by lazy { ActivityDailyPlanSjeongBinding.inflate(layoutInflater) }
    val tabLayout by lazy { binding.dayTab }

    // 구글 맵
    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    // 여행 기간
    var dayList: ArrayList<Parcelable>? = null
    var stringDayList: MutableList<String> = mutableListOf()
    // 계획
    var dailyPlan: MutableList<DailyPlan> = mutableListOf<DailyPlan>() // 전체; 데이터 클래스 이름을 DailyPlan으로 바꿔야 할지도
    var selectDayPlan = mutableListOf<DailyPlan>() // 선택된 날짜 일정
    var selectDay = ""

    // 리사이클러뷰 어댑터
    lateinit var placeAdapter: DailyPlanAdapter_SJeong

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 여행일자 리스트
        dayList = intent.getParcelableArrayListExtra("dayList")
        if(!dayList.isNullOrEmpty()) {
            for (i in dayList!!) {
                val date = SimpleDateFormat("yyyy-MM-dd").format((i as CalendarDay).date)
                stringDayList.add(date)
            }
        }
        // 시작&끝 날짜
        startDate = intent.getSerializableExtra("startDate") as Calendar
        endDate = intent.getSerializableExtra("endDate") as Calendar

        // 날짜 탭 생성
        for(num in 1..stringDayList.size) {
            createTab(num)
        }

        // 여행 지역을 선택하지 않았을 경우
        if(MapController.selectedPlaceCountryName.isNullOrEmpty()) {
            binding.tvMainPlace.text = "여행 지역"
        }else {
            binding.tvMainPlace.text = "${MapController.selectedPlaceCountryName}, ${MapController.selectedPlaceCity}"
        }
        binding.tvDate.text = stringDayList.first()

        // 지도
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()
            mapFragment.getMapAsync(this@DailyPlanActivity_SJeong)
        }

        // 처음 화면 - day1
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = DailyPlanAdapter_SJeong(selectDayPlan)
        binding.recyclerView.adapter = placeAdapter

        // 날짜 탭으로 화면 전환
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            // 선택될 때
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.tvDate.text = stringDayList[tab!!.id - 1]
                selectDay = binding.tvDate.text.toString()
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

        binding.btnPlaceAdd.setOnClickListener {
            tabLayout.visibility = View.INVISIBLE
            // 검색 동작
            search()
            placeAdapter.notifyDataSetChanged()
        }

        binding.btnPlaceDel.setOnClickListener {

        }

        binding.btnExpenses.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }
    }

    // 탭 생성
    fun createTab(num: Int) {
        val tab: TabLayout.Tab = tabLayout.newTab()
        tab.text = "day${num}"
        tabLayout.addTab(tab)
        tab.id = num
    }

    // 날짜 필터
    fun filterDay(selectDay: String) {
        selectDayPlan.clear()
        selectDayPlan.addAll(dailyPlan.filter { it.date.equals(selectDay) })
    }

    // 장소 추가 버튼 이벤트
    fun search() {
        binding.frameLayout.visibility = View.VISIBLE
        // 검색
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.search_fragment) as AutocompleteSupportFragment
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

                val newDailyPlan = DailyPlan(null, selectDay, MapController.selectedPlaceName!!, MapController.selectedPlaceAddress!!, MapController.selectedPlaceLatLng!!.latitude, MapController.selectedPlaceLatLng!!.longitude)
                dailyPlan.add(newDailyPlan)
                selectDayPlan.add(newDailyPlan)

                binding.frameLayout.visibility = View.GONE
                tabLayout.visibility = View.VISIBLE
            }

            override fun onError(status: Status) {
                Log.i("log", "An error occurred: $status")
            }
        })
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
    }

    fun moveCamera(latLng: LatLng){
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mapController.addMark(listOf(latLng))
    }

}