package com.jccgs.travelplanner_v2.cyun

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.databinding.BottomSheetDialogBinding
import com.jccgs.travelplanner_v2.jkim.AuthController
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.User

class EditUsersDialog(val documentId: String, val invitedUsers: MutableList<User>): BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetDialogBinding
    lateinit var adapter: EditUsersRVAdapter

    var searchedUser: User? = null
    lateinit var invitedUserDialog: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetDialogBinding.inflate(inflater, container, false)
        invitedUserDialog = AlertDialog.Builder(requireContext())

        //Tag like RecyclerView LayoutManager
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        binding.invitedRecyclerView.layoutManager = layoutManager

        adapter = EditUsersRVAdapter(requireContext(), invitedUsers)
        binding.invitedRecyclerView.adapter = adapter

        binding.btnInviteFriendEdit.setOnClickListener {
            searchUser(binding.etInviteFriendEdit.text.toString())
        }
        return binding.root
    }

    override fun onDestroy() {
        //Dialog가 사라졌을 때 DetailActivity_CYun에 표시된 여행 인원 숫자 변경
        (requireContext() as DetailActivity_CYun).binding.tvWithCount.text = "${invitedUsers.size} 명"

        //변경된 여행 인원에 따라 현재 Plan의 users 필드 업데이트
        (requireContext() as DetailActivity_CYun).selectedPlan.users.clear()
        for (i in invitedUsers) {
            (requireContext() as DetailActivity_CYun).selectedPlan.users.add(i.id.toString())
        }
        super.onDestroy()
    }

    fun searchUser(userEmail: String){
        if (userEmail == AuthController.currentUser?.userEmail){
            binding.etInviteFriendEdit.text.clear()
            return
        }

        //입력한 userEmail과 같은 이메일을 갖는 유저의 정보 가져옴
        FirebaseController.USER_REF.whereEqualTo("userEmail", userEmail).get()
            .addOnSuccessListener { snapshot ->
                Log.d("Log_debug", "snapshot : ${snapshot.isEmpty}")
                if (!snapshot.isEmpty){
                    searchedUser = snapshot.first().toObject<User>()
                } else {
                    Toast.makeText(requireContext(), "검색하신 이메일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    binding.etInviteFriendEdit.text.clear()
                    return@addOnSuccessListener
                }

                //검색한 유저가 이미 초대되어있을 때
                if (invitedUsers.contains(searchedUser)){
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("알림")
                        setMessage("이미 함께하고 계십니다")
                        setPositiveButton("확인", null)
                        show()
                    }
                } else {
                    //검색한 유저가 초대되어있지 않을 때
                    invitedUserDialog.apply {
                        setTitle("알림")
                        setMessage("${searchedUser?.displayName}님을 여행에 초대하시겠습니까 ?")
                        setPositiveButton("확인") { _, _ ->
                            searchedUser?.let { it -> invitedUsers.add(it) }
                            inviteUser()
                        }
                        setNegativeButton("취소", null)
                    }
                    invitedUserDialog.show()
                }
                binding.etInviteFriendEdit.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "검색에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    //변경된 invitedUsers의 id들을 현재 Plan의 users 필드에 적용
    fun inviteUser(){
        val newUsers = mutableListOf<String>()
        for (user in invitedUsers){
            user.id?.let { newUsers.add(it) }
        }

        FirebaseController.PLAN_REF.document(documentId).update("users", newUsers).addOnSuccessListener {
            adapter.notifyDataSetChanged()
        }
    }
}