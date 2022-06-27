package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.Query
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityMainCyunBinding

class MainActivity_CYun : AppCompatActivity() {
    lateinit var binding: ActivityMainCyunBinding

    lateinit var customAdapter: CustomAdapter_CYun

    val plans: MutableList<Plan> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //앱바를 툴바로 변경
        setSupportActionBar(binding.toolbar)
        //앱타이틀 숨기기
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //툴바에 메뉴추가
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //메뉴 아이콘 지정
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu)

//        makeItemViewDataList()
        getPlans()

        val layoutManager = GridLayoutManager(this,1, GridLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager

        customAdapter = CustomAdapter_CYun(plans)

        binding.recyclerView.adapter = customAdapter

        binding.includeNavi.tvLogout.setOnClickListener {
            signOut()
        }

        binding.includeNavi.ivPhoto.setOnClickListener {
            val intent = Intent(this,PrevTripActivity_CKim::class.java)
            startActivity(intent)
        }

        binding.includeNavi.tvUserDisplayName.text = "${AuthController.currentUser?.displayName} 님"
        Log.d("Log_debug", "${AuthController.currentUser?.displayName}")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.nav_menu,menu)

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

    fun onClick(view: View){
        when(view.id){

            R.id.btnAdd1 ->{
                val intent = Intent(this, MainPlaceActivity_SJeong::class.java)
                startActivity(intent)
            }
            R.id.btnAdd2 ->{
            }
            R.id.btnAdd3 ->{
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
        AuthController.auth.signOut()
        AuthController.currentUser = null

        if (AuthController.googleSignInClient != null){
            AuthController.googleSignInClient?.signOut()?.addOnCompleteListener {
                AuthController.googleSignInClient = null
            }
        }

        val intent = Intent(this, LogInActivity_CYun::class.java)
        startActivity(intent)
        finish()
    }
}