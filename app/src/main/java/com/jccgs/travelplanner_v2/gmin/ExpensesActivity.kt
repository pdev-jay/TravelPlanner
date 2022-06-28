package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jccgs.travelplanner_v2.databinding.ActivityExpensesBinding

class ExpensesActivity : AppCompatActivity() {
    lateinit var binding : ActivityExpensesBinding
    lateinit var customAdapter : CustomAdapter
    val itemViewDataList : MutableList<ItemViewData> = mutableListOf<ItemViewData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //레이아웃매니저 결정할것
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        //어댑터를 객체화시키고 recyclerView adapter에 연결
        customAdapter = CustomAdapter(itemViewDataList)
        binding.recyclerView.adapter = customAdapter

        //이벤트처리 ( FloatingActionButton ) : 사용자 정의창을 불러서 설계
        binding.floatingActionButton.setOnClickListener {
            val CustomDialog = CustomDialog(this)

            CustomDialog.ShowDialog()
        }
    }

    fun getSum(){
        var sum = 0

        for (i in itemViewDataList){
            sum += i.tvPay
        }

        binding.tvTotalPayMin.text = sum.toString()
    }



    fun addItemViewDataList(itemViewData: ItemViewData) {
        this.itemViewDataList.add(itemViewData)
        customAdapter.notifyDataSetChanged()
        Toast.makeText(this, "금액 사용 기록이 추가 되었습니다.", Toast.LENGTH_SHORT).show()
    }

    fun updateItemViewDataList(position: Int, itemViewData: ItemViewData){
        this.itemViewDataList.set(position, itemViewData)
        customAdapter.notifyDataSetChanged()

    }

    fun removeItemViewDataList(itemViewData: ItemViewData) {
        var dialog = Dialog(applicationContext)

        // 다이얼로그 이벤트 핸들러 등록
        val eventHandler = object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> {
                        itemViewDataList.remove(itemViewData)
                        customAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext,
                            "${itemViewData.tvDate}에 ${itemViewData.tvContent} 기록이 삭제 되었습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss()
                }
            }
        }

        dialog = AlertDialog.Builder(this).run {
            setMessage("삭제하시겠습니까?")
            setNegativeButton("취소", eventHandler)
            setPositiveButton("삭제", eventHandler)
            show()
        }
    }
}


