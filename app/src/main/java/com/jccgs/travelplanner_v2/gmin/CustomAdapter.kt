package com.jccgs.travelplanner_v2.gmin


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jccgs.travelplanner_v2.databinding.ItemviewGminBinding


class CustomAdapter(val itemViewData : MutableList<ItemViewData>): RecyclerView.Adapter<CustomAdapter.CustomViewHolder>(){
    lateinit var parentContext: ExpensesActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        parentContext = parent?.context as ExpensesActivity

        val binding = ItemviewGminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //해당되는 아이템뷰에 이벤트를 설정한다.
        val customViewHolder = CustomViewHolder(binding)

        //아이템뷰 수정기능
        binding.ivEditMin.setOnClickListener{
            val abUpdateDialog = UpdateDialog(parent?.context)
            val position = customViewHolder.adapterPosition
            val tempItemViewData = itemViewData.get(position)
            abUpdateDialog.ShowUdDialog(tempItemViewData, position)
        }

        //아이템뷰 삭제기능
        binding.ivDeleteMin.setOnClickListener {
            val position = customViewHolder.adapterPosition
            val tempItemViewData = itemViewData.get(position)
            (parent.context as ExpensesActivity)
                .removeItemViewDataList(tempItemViewData)
        }

        return customViewHolder
    }

    //holder는 CustomAdapter와 OnCreateViewHolder에서 return customViewHolder해주면 화면에 제공해야될 위치와
    //CustomViewHolder를 객체변수로 준다
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding

        binding.tvDateMin.text = itemViewData.get(position).tvDate
        binding.tvContentMin.text = itemViewData.get(position).tvContent
        binding.tvPayMin.text = itemViewData.get(position).tvPay.toString()
    }

    override fun getItemCount(): Int {

        return itemViewData.size
    }

    class CustomViewHolder(val binding :ItemviewGminBinding) : RecyclerView.ViewHolder(binding.root)
}
















