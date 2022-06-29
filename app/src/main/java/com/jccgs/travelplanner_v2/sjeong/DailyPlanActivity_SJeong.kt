package com.jccgs.travelplanner_v2.sjeong

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
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
import com.google.maps.android.clustering.ClusterManager
import com.jccgs.travelplanner_v2.ckim.ChecklistActivity_CKim
import com.jccgs.travelplanner_v2.cyun.MainActivity_CYun
import com.jccgs.travelplanner_v2.databinding.ActivityDailyPlanSjeongBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DailyPlanActivity_SJeong : AppCompatActivity(), OnMapReadyCallback{
    companion object {
        lateinit var startDate: Calendar
        lateinit var endDate: Calendar
        var documentId: String? = null
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
    var dailyPlan: MutableList<DailyPlan> = mutableListOf<DailyPlan>() // 전체 일정
    var selectDayPlan = mutableListOf<DailyPlan>() // 선택된 날짜 일정
    var selectDay = ""

    var order = 0

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

        if (intent.hasExtra("documentId")){
            documentId = intent.getStringExtra("documentId")
        }
        // 날짜 탭 생성
        for(num in 1..stringDayList.size) {
            createTab(num)
        }

        // 여행 지역을 선택하지 않았을 경우
        if(MapController.selectedPlaceCountryName.isNullOrEmpty()) {
            binding.tvMainPlace.text = "여행 지역"
        }else {
            binding.tvMainPlace.text = "${MapController.selectedPlaceCountryName} ${MapController.selectedPlaceCity}"
        }
        binding.tvDate.text = stringDayList.first()
        selectDay = stringDayList.first()

        // 지도
        lifecycle.coroutineScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            googleMap = mapFragment.awaitMap()
            mapFragment.getMapAsync(this@DailyPlanActivity_SJeong)
        }

        // 처음 화면 - day1
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = DailyPlanAdapter_SJeong(selectDayPlan)
        binding.recyclerView.adapter = placeAdapter

        // 검색
        search()

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

        // 장소 추가
        binding.btnPlaceAdd.setOnClickListener {
            placeAdapter.notifyDataSetChanged()
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

    // 검색
    fun search() {
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.search_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setCountry(MapController.selectedPlaceShortName)
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

    fun addPlace() {
        val newDailyPlan = DailyPlan(null, selectDay, MapController.selectedPlaceName!!, MapController.selectedPlaceAddress!!, MapController.selectedPlaceLatLng!!.latitude, MapController.selectedPlaceLatLng!!.longitude)
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
             setPositiveButton("확인", { _, _ ->
                 deleteCurrentPlan()
             })
             setNegativeButton("취소", null)
             show()
         }
//        super.onBackPressed()
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
            Toast.makeText(this, "cluste info window long click", Toast.LENGTH_SHORT).show()
            Log.d("Log_debug", "cluste info window long click")
            val newDailyPlan = DailyPlan(order++, selectDay, MapController.selectedPlaceName.toString(), MapController.selectedPlaceAddress.toString(), MapController.selectedPlaceLatLng!!.latitude, MapController.selectedPlaceLatLng!!.longitude)
            documentId?.let { documentId ->
                FirebaseController
                .PLAN_REF
                .document(documentId)
                .collection("DailyPlan")
                .add(newDailyPlan)
                    .addOnSuccessListener {
                        addPlace()
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