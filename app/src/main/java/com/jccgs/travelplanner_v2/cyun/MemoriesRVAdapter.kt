package com.jccgs.travelplanner_v2.cyun

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ItemviewCyunBinding
import com.jccgs.travelplanner_v2.jkim.Plan
import java.util.*

class MemoriesRVAdapter(val plans: MutableList<Plan>) : RecyclerView.Adapter<MemoriesRVAdapter.CustomViewHolder>(), ItemTouchHelperCallback.OnItemMoveListener {

    private lateinit var dragListener: OnStartDragListener
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoriesRVAdapter.CustomViewHolder {
        val binding = ItemviewCyunBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        context = parent.context
        val customViewHolder = MemoriesRVAdapter.CustomViewHolder(binding)

        return customViewHolder
    }

    //holder는 CustomAdapter 에서 onCreateViewHolder에서 return customViewHolder 해주면
    // 화면에서 제공해야 될 위치(position)와 CustomViewHolder 객체 매개변수로 준다.
    override fun onBindViewHolder(holder: MemoriesRVAdapter.CustomViewHolder, position: Int) {
        val binding = (holder as MemoriesRVAdapter.CustomViewHolder).binding
        binding.ivTravel.setImageResource(R.drawable.airplane_ticket_24)
        binding.tvPlanTitleMain.text = "${plans[position].title}"
        binding.tvPeriod.text = "${plans[position].period.first()} ~ ${plans[position].period.last()}"

        binding.root.setOnClickListener {
            val intent = Intent(context, DetailActivity_CYun::class.java)
            intent.putExtra("selectedPlan", plans[position])
            context.startActivity(intent)
        }
    }

    //customAdapter에서 recyclerView에 제공해줘야할 데이터 갯수 파악
    override fun getItemCount(): Int {
        return plans.size
    }

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    fun startDrag(listener: OnStartDragListener) {
        this.dragListener = listener
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(plans, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemSwiped(position: Int) {
        plans.removeAt(position)
        notifyItemRemoved(position)
    }



    //뷰 홀더 정의
    class CustomViewHolder(val binding: ItemviewCyunBinding): RecyclerView.ViewHolder(binding.root)
}