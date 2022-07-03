package com.jccgs.travelplanner_v2.cyun

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jccgs.travelplanner_v2.databinding.InvitedFriendItemViewBinding
import com.jccgs.travelplanner_v2.jkim.AuthController
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.User

class EditUsersRVAdapter(val context: Context, val users: MutableList<User>): RecyclerView.Adapter<EditUsersRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EditUsersRVAdapter.ViewHolder {
        val binding = InvitedFriendItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EditUsersRVAdapter.ViewHolder, position: Int) {
        holder.binding.tvFriendDisplayName.text = users[position].displayName

        holder.itemView.setOnLongClickListener {
            if (users[position].id != AuthController.currentUser?.id) {
                AlertDialog.Builder(context).apply {
                    setTitle("알림")
                    setMessage("초대를 취소하시겠습니까?")
                    setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                        users.removeAt(position)
                        removeUserFromPlan()
                        notifyDataSetChanged()
                    })
                    setNegativeButton("취소", null)
                    show()
                }
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class ViewHolder(val binding: InvitedFriendItemViewBinding): RecyclerView.ViewHolder(binding.root)

    fun removeUserFromPlan(){
        val userIds = mutableListOf<String>()
        for (i in users){
            userIds.add(i.id.toString())
        }
        var documentId = (context as DetailActivity_CYun).selectedPlan.id
        if (documentId != null) {
            FirebaseController.PLAN_REF.document(documentId).update("users", userIds)

        }
    }
}