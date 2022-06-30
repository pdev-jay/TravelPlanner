package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.jccgs.travelplanner_v2.databinding.CustomDialogGminBinding
import com.jccgs.travelplanner_v2.jkim.Expenses
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DateDialog


class CustomDialog(val context: Context){

    //멤버변수
    private val dialog = Dialog(context)
    //현재 다이얼로그 창을 객체화함
    private val binding = CustomDialogGminBinding.inflate(LayoutInflater.from(context))

    //멤버함수
    fun ShowDialog(){

        dialog.setContentView(binding.root)
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        binding.edtDateMin.setOnClickListener {
            val expensesDialog = DateDialog(context)
            expensesDialog.showExpensesDialog(CalendarActivity_SJeong.startDate!!, CalendarActivity_SJeong.endDate!!)
            expensesDialog.setonDialogClickListener(object: DateDialog.OnDialogClickListener{
                override fun onDialogClicked(date: String) {
                    binding.edtDateMin.setText(date)
                }
            })
        }

        //저장하기 이벤트 처리
        binding.btnDialEnter.setOnClickListener {
            val edtContent = binding.edtContentMin.text.toString()
            val edtDate = binding.edtDateMin.text.toString()
            var pay = binding.edtPayMin.text.toString()

            if(edtContent.isNullOrEmpty() || pay.isNullOrEmpty()) {
                Toast.makeText(context, "입력하지 않은 항목이 있습니다.", Toast.LENGTH_SHORT).show()
            }else {
                val edtPay = pay.toInt()
                val ABitemViewData = Expenses(date = edtDate, content = edtContent, cost = edtPay)
                (context as ExpensesActivity).addItemViewDataList(ABitemViewData)
                dialog.dismiss()
            }
        }

        //취소하기 이벤트 처리
        binding.btnDialExit.setOnClickListener{
            dialog.dismiss()
        }
    }
}





