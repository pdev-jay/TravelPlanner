package com.jccgs.travelplanner_v2.jkim

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jccgs.travelplanner_v2.databinding.InvitedFriendItemViewBinding

class InvitedFriendsRVAdapter(val context: Context, val invitedFriendList: ArrayList<User>): RecyclerView.Adapter<InvitedFriendsRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InvitedFriendsRVAdapter.ViewHolder {
        val binding = InvitedFriendItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitedFriendsRVAdapter.ViewHolder, position: Int) {
        holder.binding.tvFriendDisplayName.text = invitedFriendList[position].displayName.toString()

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("알림")
                setMessage("초대를 취소하시겠습니까?")
                setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                    invitedFriendList.removeAt(position)
                    notifyDataSetChanged()
                })
                setNegativeButton("취소", null)
                show()
            }
            true
        }
    }

    override fun getItemCount(): Int {
        return invitedFriendList.size
    }

    inner class ViewHolder(val binding: InvitedFriendItemViewBinding): RecyclerView.ViewHolder(binding.root)
}