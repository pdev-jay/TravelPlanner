package com.jccgs.travelplanner_v2.jkim

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityPlanTitleBinding


class PlanTitleActivity : AppCompatActivity(), TextView.OnEditorActionListener{
    val binding by lazy { ActivityPlanTitleBinding.inflate(layoutInflater) }

    lateinit var adapter: InvitedFriendsRVAdapter

    lateinit var planTitle: String
    var invitedUsers: ArrayList<User> = arrayListOf()
    var searchedUser: User? = null

    lateinit var invitedUserDialog: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        invitedUserDialog = AlertDialog.Builder(this)

        binding.etPlanTitle.setOnEditorActionListener(this)
        binding.etInviteFriend.setOnEditorActionListener(this)
        binding.btnNextTitle.isEnabled = false
        adapter = InvitedFriendsRVAdapter(this, invitedUsers)
        binding.friendsRecyclerView.adapter = adapter


        binding.btnNextTitle.setOnClickListener {
            val intent = Intent(this, SearchPlaceActivity_JKim::class.java)
            intent.putExtra("invitedUsers", invitedUsers)
            intent.putExtra("planTitle", planTitle)
            startActivity(intent)
        }

        binding.btnInviteFriend.setOnClickListener{
            //검색창에 입력된 값이 있을 때만 검색 진행
            if (!binding.etInviteFriend.text.toString().isNullOrBlank()) {
                searchUser(binding.etInviteFriend.text.toString())
                Log.d("Log_debug", "${binding.etInviteFriend.text}")
            }
        }

        binding.btnSetTitle.setOnClickListener {
            if (binding.etPlanTitle.text.toString().isNullOrBlank()){
                Toast.makeText(this, "여행의 이름을 정해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            planTitle = binding.etPlanTitle.text.toString()
        }

        //태그와 비슷하게 layout의 너비가 충분하지 않을 때 span count를 자동으로 조절해주는 3rd party library
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        binding.friendsRecyclerView.layoutManager = layoutManager

        //editText에 입력값 변화가 있을 때의 동작
        binding.etPlanTitle.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!charSequence.isNullOrBlank()){
                    binding.tvPlanTitle.text = charSequence.toString()
                    planTitle = charSequence.toString()
                    binding.btnNextTitle.isEnabled = true
                    binding.btnSetTitle.background.setTint(Color.parseColor("#C3B8D9"))
                } else {
                    binding.tvPlanTitle.text = ""
                    planTitle = charSequence.toString()
                    binding.btnNextTitle.isEnabled = false
                    binding.btnSetTitle.background.setTint(Color.GRAY)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    //editText가 focus 된 상태에서 엔터를 눌렀을 때
    override fun onEditorAction(view: TextView?, action: Int, keyEvent: KeyEvent?): Boolean {
        return when (view?.id) {

            R.id.etInviteFriend -> {
                if (action == EditorInfo.IME_ACTION_DONE){
                    if (!binding.etInviteFriend.text.toString().isNullOrBlank()) {
                        searchUser(binding.etInviteFriend.text.toString())
                    }
                }
                true
            }

            else -> {
                false
            }
        }
    }

    fun searchUser(userEmail: String){
        //자기 자신을 검색했을 때 함수 종료
        if (userEmail == AuthController.currentUser?.userEmail){
            binding.etInviteFriend.text.clear()
            return
        }

        FirebaseController.USER_REF.whereEqualTo("userEmail", userEmail).get()
            .addOnSuccessListener { snapshot ->
                Log.d("Log_debug", "snapshot : ${snapshot.isEmpty}")
                if (!snapshot.isEmpty){
                    searchedUser = snapshot.first().toObject<User>()
                } else {
                    Toast.makeText(this, "검색하신 이메일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    binding.etInviteFriend.text.clear()
                    return@addOnSuccessListener
                }
                if (invitedUsers.contains(searchedUser)){
                    AlertDialog.Builder(this).apply {
                        setTitle("알림")
                        setMessage("이미 함께하고 계십니다")
                        setPositiveButton("확인", null)
                        show()
                    }
                } else {
                    invitedUserDialog.apply {
                        setTitle("알림")
                        setMessage("${searchedUser?.displayName}님을 여행에 초대하시겠습니까 ?")
                        setPositiveButton("확인") { _, _ ->
                            inviteUser()
                        }
                        setNegativeButton("취소", null)
                    }
                    invitedUserDialog.show()
                }
                binding.etInviteFriend.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "검색에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    fun inviteUser(){
        searchedUser?.let { invitedUsers.add(it) }
        adapter.notifyDataSetChanged()

    }
}