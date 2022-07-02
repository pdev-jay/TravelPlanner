package com.jccgs.travelplanner_v2.cyun

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.marginLeft
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.jccgs.travelplanner_v2.BuildConfig
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.ckim.InfoActivity_CKim
import com.jccgs.travelplanner_v2.databinding.ActivityMainCyunBinding
import com.jccgs.travelplanner_v2.jkim.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity_CYun : AppCompatActivity() {

    lateinit var binding: ActivityMainCyunBinding

    lateinit var customAdapter: CustomAdapter_CYun

    var googleSignInClient: GoogleSignInClient? = null

    val plans: MutableList<Plan> = mutableListOf()

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

        val layoutManager = GridLayoutManager(this,1, GridLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        customAdapter = CustomAdapter_CYun(plans)
        binding.recyclerView.adapter = customAdapter


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
        plans.clear()
        getPlans()
        customAdapter.notifyDataSetChanged()
        super.onStart()
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
                val intent = Intent(this, SearchPlaceActivity_JKim::class.java)
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
                plans.clear()
                for (document in documents){
                    val newData = document.toObject<Plan>()
                    plans.add(newData)
                }
                customAdapter.notifyDataSetChanged()
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