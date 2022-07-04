package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.BuildConfig
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.ckim.InfoActivity_CKim
import com.jccgs.travelplanner_v2.databinding.ActivityMainCyunBinding
import com.jccgs.travelplanner_v2.jkim.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat

class MainActivity_CYun : AppCompatActivity() {

    lateinit var binding: ActivityMainCyunBinding

    lateinit var ingRVAdapter: IngRVAdapter_CYun
    lateinit var upcomingRVAdapter: UpcomingRVAdapter
    lateinit var memoriesRVAdapter: MemoriesRVAdapter

    var googleSignInClient: GoogleSignInClient? = null

    val allPlans: MutableList<Plan> = mutableListOf()
    val ingPlans: MutableList<Plan> = mutableListOf()
    val upcomingPlans: MutableList<Plan> = mutableListOf()
    val pastPlans: MutableList<Plan> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Places를 사용하기 위해 초기화
        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY)
        }

        binding = ActivityMainCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //앱바를 툴바로 변경
        setSupportActionBar(binding.toolbar)
        //앱타이틀 숨기기
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //메뉴 아이콘 지정
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_menu)


        //RecyclerView(Ing, Upcoming, Memories)
        val ingLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val upcomingLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val memoriesLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.ingRecyclerView.layoutManager = ingLayoutManager
        ingRVAdapter = IngRVAdapter_CYun(ingPlans)
        binding.ingRecyclerView.adapter = ingRVAdapter

        binding.upcomingRecyclerView.layoutManager = upcomingLayoutManager
        upcomingRVAdapter = UpcomingRVAdapter(upcomingPlans)
        binding.upcomingRecyclerView.adapter = upcomingRVAdapter

        binding.memoriesRecyclerView.layoutManager = memoriesLayoutManager
        memoriesRVAdapter = MemoriesRVAdapter(pastPlans)
        binding.memoriesRecyclerView.adapter = memoriesRVAdapter


        binding.includeNavi.tvUserDisplayName.text = "${AuthController.currentUser?.displayName} 님"

        //회원 탈퇴 AlertDialog
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("경고")
            setIcon(R.drawable.ic_baseline_info_24)
            setMessage(
                "계정을 삭제하시겠습니까?"
            )

            setPositiveButton("확인") { _, _ ->
                deregisterAccount()
            }

            setNegativeButton("취소", null)
        }

        binding.includeNavi.tvLogout.setOnClickListener {
            signOut()
        }

        binding.includeNavi.ivPhoto.setOnClickListener {
            val intent = Intent(this, InfoActivity_CKim::class.java)
            startActivity(intent)
        }

        binding.includeNavi.ivClose.setOnClickListener {
            binding.drawerLayout.closeDrawers()
        }

        binding.includeNavi.tvDeregister.setOnClickListener {
            dialog.show()
        }

    }

    override fun onStart() {
        MapController.clearMapInfo()
        allPlans.clear()
        getPlans()

        super.onStart()
    }

    //모든 Plan에서 현재 진행중인 Plan, 예정중인 Plan, 지나간 Plan 나누어 각각 다른 mutableList에 저장
    fun calPlans(){
        val filterIng = allPlans.filter { it.period.contains(SimpleDateFormat("yyyy-MM-dd").format(
                CalendarDay.today().date)) }.sortedBy { it.period.first() }
        ingPlans.clear()
        ingPlans.addAll(filterIng)

        val filterUpcoming = allPlans.filter { it.period.first() >  SimpleDateFormat("yyyy-MM-dd").format(
            CalendarDay.today().date)}.sortedBy { it.period.first() }
        upcomingPlans.clear()
        upcomingPlans.addAll(filterUpcoming)

        val filterPast = allPlans.filter { it.period.last() <  SimpleDateFormat("yyyy-MM-dd").format(
            CalendarDay.today().date)}.sortedByDescending { it.period.last() }
        pastPlans.clear()
        pastPlans.addAll(filterPast)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.nav_menu_ckim,menu)

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.btnMenu -> {
                binding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        //네비게이션 뷰가 열려있을 때 Back Button을 누르면 네비게이션 뷰 닫힘
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)){
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            return
        }
        super.onBackPressed()
    }

    fun onClick(view: View){
        when(view.id){

            R.id.btnAdd1 ->{
                val intent = Intent(this, PlanTitleActivity::class.java)
                startActivity(intent)
            }
        }
    }

    //전체 Plan을 Firestore에서 가져옴
    fun getPlans(){
        FirebaseController.PLAN_REF.
        whereArrayContains("users", AuthController.currentUser?.id.toString())
            .orderBy("period", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                allPlans.clear()
                for (document in documents){
                    val newData = document.toObject<Plan>()
                    allPlans.add(newData)
                }
                //전체 Plan를 Ing, Upcoming, Memories로 나눠서 저장
                calPlans()
                ingRVAdapter.notifyDataSetChanged()
                upcomingRVAdapter.notifyDataSetChanged()
                memoriesRVAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.d("Log_debug", "error in getPlans : $e")
            }
    }

    //signOut
    fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth에서 로그아웃
        AuthController.auth.signOut()

        //Google에서 로그아웃
        googleSignInClient?.signOut()
        googleSignInClient = null
        AuthController.currentUser = null

        val intent = Intent(this, LogInActivity_CYun::class.java)
        startActivity(intent)
        finish()

    }

    fun deregisterAccount(){
        //Firestore에 저장된 User 정보 삭제
        FirebaseController.USER_REF.document(AuthController.currentUser?.id.toString()).delete()

        //Firebase Auth에서 유저 삭제
        AuthController.auth.currentUser?.delete()?.addOnSuccessListener {
            //구글 로그아웃
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            googleSignInClient = GoogleSignIn.getClient(this@MainActivity_CYun, gso)
            googleSignInClient?.signOut()

            googleSignInClient = null
            AuthController.currentUser = null

            val intent = Intent(this@MainActivity_CYun, LogInActivity_CYun::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}