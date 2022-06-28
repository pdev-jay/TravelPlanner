package com.jccgs.travelplanner_v2.sjeong

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.ktx.awaitMap
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityDailyPlanBinding
import com.jccgs.travelplanner_v2.jkim.DailyPlan
import com.jccgs.travelplanner_v2.jkim.MapController
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import androidx.lifecycle.coroutineScope
import java.util.*

class DailyPlanActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        lateinit var startDate: Calendar
        lateinit var endDate: Calendar
    }

    val binding by lazy { ActivityDailyPlanBinding.inflate(layoutInflater) }
    val tabLayout by lazy { binding.dayTab }

    // 구글 맵
    lateinit var mapController: MapController
    lateinit var googleMap: GoogleMap

    // 여행 기간
    var dayList: ArrayList<Parcelable>? = null
    var stringDayList: MutableList<String> = mutableListOf()
    // 계획
    var dailyPlan: MutableList<DailyPlan> = mutableListOf<DailyPlan>() // 전체; 데이터 클래스 이름을 DailyPlan으로 바꿔야 할지도
    var selectPlan = mutableListOf<DailyPlan>() // 선택된 날짜 일정
    var selectDate = ""

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
        binding.tvMainPlace.text = "${MapController.selectedPlaceCountryName}, ${MapController.selectedPlaceCity}"
        binding.tvDate.text = stringDayList.first()

        // 지도
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()
            mapFragment.getMapAsync(this@DailyPlanActivity)
        }

        // 처음 화면 - day1
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = DailyPlanAdapter_SJeong(selectPlan)
        binding.recyclerView.adapter = placeAdapter

        // 날짜 탭으로 화면 전환
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            // 선택될 때
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.tvDate.text = stringDayList[tab!!.id - 1]
                selectDate = binding.tvDate.text.toString()
                selectPlan.addAll(dailyPlan.filter { it.date.equals(selectDate) })
            }

            // 선택 해제될 때
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            // 다시 선택할 때
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


    }

    // 탭 생성
    fun createTab(num: Int) {
        val tab: TabLayout.Tab = tabLayout.newTab()
        tab.text = "day${num}"
        tabLayout.addTab(tab)
        tab.id = num
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }
}