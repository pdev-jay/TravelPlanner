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

//        val layoutManager = GridLayoutManager(this,1, GridLayoutManager.HORIZONTAL, false)
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

        Log.d("Log_debug", "${ingPlans}")

        super.onStart()
    }

    fun calPlans(){
        val filterIng = allPlans.filter { it.period.contains(
            SimpleDateFormat("yyyy-MM-dd").format(
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

        AuthController.auth.signOut()

        googleSignInClient?.signOut()
        googleSignInClient = null
        AuthController.currentUser = null

        val intent = Intent(this, LogInActivity_CYun::class.java)
        startActivity(intent)
        finish()

    }

    fun deregisterAccount(){

        FirebaseController.USER_REF.document(AuthController.currentUser?.id.toString()).delete()

        AuthController.auth.currentUser?.delete()?.addOnSuccessListener {
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