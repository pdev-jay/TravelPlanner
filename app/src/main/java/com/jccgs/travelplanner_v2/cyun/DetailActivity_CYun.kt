package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityDetailCyunBinding
import com.jccgs.travelplanner_v2.databinding.BottomSheetMapBinding
import com.jccgs.travelplanner_v2.jkim.*
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong
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
    var invitedUsers: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editMode = false

        //BottomSheet 등록
        sheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(binding.bottomSheetDetail.root)
        sheetBehavior.isDraggable = false
        sheetBehavior.peekHeight = 0
        sheetBehavior.isHideable = true

        //메인 액티비티에서 클릭한 Plan의 정보
        if (intent.hasExtra("selectedPlan")){
            selectedPlan = intent.getSerializableExtra("selectedPlan") as Plan
        }

        binding.tvLocation.text = selectedPlan.country
        binding.tvDurationTitle.text = "${selectedPlan.period.first()} ~ ${selectedPlan.period.last()}"
        binding.tvPlanTitleDetail.text = selectedPlan.title
        binding.tvWithCount.text = "${selectedPlan.users.size} 명"

        binding.recyclerViewDetail.layoutManager = LinearLayoutManager(this)
        customAdapter = DetailAdapter_CYun(this, dailyPlans)
        binding.recyclerViewDetail.adapter = customAdapter

        //BottomSheet의 맵 프래그먼트 준비
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_bottom) as SupportMapFragment
        mapFragment.getMapAsync(this@DetailActivity_CYun)

        //
        bottomSheetBinding.btnDismissMap.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        //BottomSheet Open
        binding.btnShowMap.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        //Plan 삭제
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

        //일정 보기 버튼을 눌렀을 때
        binding.btnEditDetail.setOnClickListener {
            editMode = true
            prepareToEdit()
        }

        //여행에 참가한 인원 편집
        binding.btnEditInvitedUsers.setOnClickListener {
            getInvitedPeople()
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

        this.googleMap = googleMap
        mapController = MapController(this, this.googleMap)

        this.googleMap.setOnCameraIdleListener(mapController.clusterManager)
        this.googleMap.setOnMarkerClickListener(mapController.clusterManager)

        //마커 옵션 변경
        mapController.clusterManager.renderer = object:
            DefaultClusterRenderer<MapController.Cluster>(this, googleMap, mapController.clusterManager) {
            override fun onBeforeClusterItemRendered(
                item: MapController.Cluster,
                markerOptions: MarkerOptions
            ) {
                //마커 색깔 변경
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                super.onBeforeClusterItemRendered(item, markerOptions)
            }
        }
    }

    //상세 일정을 날짜, 순서를 기준으로 오름차순으로 받아옴
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
                    }
                }
                customAdapter.notifyDataSetChanged()

                //상세 일정의 모든 장소에 마커 추가
                mapController.addDailyPlanMarkers(dailyPlans)
                if (dailyPlans.isNotEmpty()){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dailyPlans.first().placeLat, dailyPlans.first().placeLng), 13f))
                }
            }
            .addOnFailureListener {
                Log.d("Log_debug", "dailyPlan = failed : $it")
            }
    }

    //예상 경비를 날짜, 순서를 기준으로 오름차순으로 받아옴
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

    //체크리스트를 순서를 기준으로 오름차순으로 받아옴
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

    //여행에 참가하는 인원의 id를 통하여 유저 데이터를 받아옴
    fun getInvitedPeople(){
        invitedUsers.clear()
        FirebaseController.USER_REF.whereIn("id", selectedPlan.users).get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty){
                for (i in snapshot){
                    val newData = i.toObject<User>()
                    invitedUsers.add(newData)
                }
                Log.d("Log_debug", "getInvitedPeople : $invitedUsers")
                val dialog = EditUsersDialog(selectedPlan.id!!, invitedUsers)
                dialog.show(supportFragmentManager, "editUsers")
            }
        }
    }

    //documentID(Plan의 id)를 이용하여 Firestore의 문서 삭제
    fun deletePlan(){
        selectedPlan.id?.let { FirebaseController.PLAN_REF.document(it).delete().addOnSuccessListener {
            val intent = Intent(this, MainActivity_CYun::class.java)
            startActivity(intent)
            finish()
        }}
    }

    //DailyPlanActivity에 현재 Plan을 넘겨줄 때 Calendar Type의 date를 얻기위한 변환과정
    fun calDate(index: Int): Calendar{
        val date = selectedPlan.period[index].split("-")
        val planStartDate = Calendar.getInstance()
        planStartDate.set(date[0].toInt(), date[1].toInt() - 1, date[2].toInt())
        return planStartDate
    }

    //MapController의 장소 정보를 현재 Plan의 상세 일정 중 첫번째 장소로 지정
    fun setPlaceInfo(){
        MapController.selectedPlaceLatLng = LatLng(dailyPlans.first().placeLat, dailyPlans.first().placeLng)
        MapController.selectedPlaceName = dailyPlans.first().placeName
        MapController.selectedPlaceAddress = dailyPlans.first().placeAddress
    }

    //Plan의 날짜 정보와 지역 정보를 현재 Plan으로 설정
    fun prepareToEdit(){
        va정 startDate = calDate(0)
        val endDate = calDate(selectedPlan.period.size - 1)
        CalendarActivity_SJeong.startDate = startDate
        CalendarActivity_SJeong.endDate = endDate
        CalendarActivity_SJeong.documentId = selectedPlan.id
        CalendarActivity_SJeong.stringDayList = selectedPlan.period

        MapController.selectedPlaceCountryName = selectedPlan.country
        MapController.selectedPlaceCity = selectedPlan.city
        MapController.selectedPlaceShortName = selectedPlan.countryCode

        //상세 일정이 비어있지 않았을 때
        if (dailyPlans.isNotEmpty()) {
            setPlaceInfo()
        }

        val intent = Intent(this, DailyPlanActivity_SJeong::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //BottomSheet이 올라와있을 때 Back Button을 눌렀을 때 BottomSheet 닫기
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }
}