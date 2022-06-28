package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accountbook.gmin.CustomAdapter
import com.example.accountbook.gmin.CustomDialog
import com.example.accountbook.gmin.ItemViewData
import com.jccgs.travelplanner_v2.databinding.ActivityExpensesBinding

class ExpensesActivity : AppCompatActivity() {
    lateinit var binding : ActivityExpensesBinding
    lateinit var ABcustomAdapter : CustomAdapter
    val ABitemViewDataList : MutableList<ItemViewData> = mutableListOf<ItemViewData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //레이아웃매니저 결정할것
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        //어댑터를 객체화시키고 recyclerView adapter에 연결
        ABcustomAdapter = CustomAdapter(ABitemViewDataList)
        binding.recyclerView.adapter = ABcustomAdapter

        //이벤트처리 ( FloatingActionButton ) : 사용자 정의창을 불러서 설계
        binding.floatingActionButton.setOnClickListener {
            val ABCustomDialog = CustomDialog(this)

            ABCustomDialog.ShowDialog()
        }


    }

    fun getSum(){
        var sum = 0

        for (i in ABitemViewDataList){
            sum += i.tvPay
        }
        binding.tvTotalPayMin.text = sum.toString()
    }

    fun addItemViewDataList(ABitemViewData: ItemViewData) {
        this.ABitemViewDataList.add(ABitemViewData)
        ABcustomAdapter.notifyDataSetChanged()
        Toast.makeText(this, "금액 사용 기록이 추가 되었습니다.", Toast.LENGTH_SHORT).show()

    }

    fun updateItemViewDataList(position: Int, ABitemViewData: ItemViewData){
        this.ABitemViewDataList.set(position, ABitemViewData)
        ABcustomAdapter.notifyDataSetChanged()

    }

    fun removeItemViewDataList(ABitemViewData: ItemViewData) {
        var dialog = Dialog(applicationContext)

        // 다이얼로그 이벤트 핸들러 등록
        val eventHandler = object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> {
                        ABitemViewDataList.remove(ABitemViewData)
                        ABcustomAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext,
                            "${ABitemViewData.tvDate}에 ${ABitemViewData.tvContent} 기록이 삭제 되었습니다",
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
