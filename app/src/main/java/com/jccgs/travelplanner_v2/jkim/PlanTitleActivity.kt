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
        binding.btnSetTitle.background.setTint(Color.GRAY)

        binding.btnNextTitle.setOnClickListener {
            val intent = Intent(this, SearchPlaceActivity_JKim::class.java)
            intent.putExtra("invitedUsers", invitedUsers)
            intent.putExtra("planTitle", planTitle)
            startActivity(intent)
        }

        binding.btnInviteFriend.setOnClickListener{
            if (!binding.etInviteFriend.text.toString().isNullOrBlank()) {
                searchUser(binding.etInviteFriend.text.toString())
                Log.d("Log_debug", "${binding.etInviteFriend.text.toString()}")
            }
        }

        binding.btnSetTitle.setOnClickListener {
            if (binding.etPlanTitle.text.toString().isNullOrBlank()){
                Toast.makeText(this, "여행의 이름을 지어주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            planTitle = binding.etPlanTitle.text.toString()
        }


        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        binding.friendsRecyclerView.layoutManager = layoutManager

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

    override fun onEditorAction(view: TextView?, action: Int, keyEvent: KeyEvent?): Boolean {
        return when (view?.id) {
            R.id.etPlanTitle -> {
                if (action == EditorInfo.IME_ACTION_DONE){
                    planTitle = binding.etPlanTitle.text.toString()
                    Log.d("Log_debug", "${planTitle}")

                }
                true
            }

            R.id.etInviteFriend -> {
                if (action == EditorInfo.IME_ACTION_DONE){
                    if (!binding.etInviteFriend.text.toString().isNullOrBlank()) {
                        searchUser(binding.etInviteFriend.text.toString())
                        Log.d("Log_debug", "${binding.etInviteFriend.text.toString()}")
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
        FirebaseController.USER_REF.whereEqualTo("userEmail", userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty){
                    searchedUser = snapshot.first().toObject<User>()
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
                        setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                            inviteUser()
                        })
                        setNegativeButton("취소", null)
                    }
                    invitedUserDialog.show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "검색한 이메일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    fun inviteUser(){
        searchedUser?.let { invitedUsers.add(it) }
        adapter = InvitedFriendsRVAdapter(this, invitedUsers)

        binding.friendsRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

    }
}