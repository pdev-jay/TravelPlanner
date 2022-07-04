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
        binding.etInviteFriendEdit
        return binding.root
    }

    override fun onDestroy() {
        (requireContext() as DetailActivity_CYun).binding.tvWithCount.text = "${invitedUsers.size.toString()} 명"
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
                if (invitedUsers.contains(searchedUser)){
                    AlertDialog.Builder(requireContext()).apply {
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

    fun inviteUser(){
        Log.d("Log_debug", "invitedUsers size = ${invitedUsers.size}")
        val newUsers = mutableListOf<String>()
        for (user in invitedUsers){
            user.id?.let { newUsers.add(it) }
        }
        Log.d("Log_debug", "newUsers : ${newUsers}")
        FirebaseController.PLAN_REF.document(documentId).update("users", newUsers).addOnSuccessListener {
            adapter.notifyDataSetChanged()
        }
    }
}