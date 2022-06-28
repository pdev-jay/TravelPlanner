package com.example.accountbook.gmin

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.jccgs.travelplanner_v2.databinding.CustomDialogGminBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity.Companion.endDate
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity.Companion.startDate
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
            expensesDialog.showExpensesDialog(DailyPlanActivity.startDate, DailyPlanActivity.endDate)
            expensesDialog.setonDialogClickListener(object: DateDialog.OnDialogClickListener{
                override fun onDialogClicked(date: String) {
                    binding.edtDateMin.setText(date)
                }
            })
        }

        //저장하기 이벤트 처리
        binding.btnDialEnter.setOnClickListener{
            val edtContent = binding.edtContentMin.text.toString()
            val edtPay = binding.edtPayMin.text.toString()
            val edtDate = binding.edtDateMin.text.toString()
            val ABitemViewData = ItemViewData(edtDate, edtContent, edtPay)
            (context as ExpensesActivity).addItemViewDataList(ABitemViewData)

            dialog.dismiss()
        }

        //취소하기 이벤트 처리
        binding.btnDialExit.setOnClickListener{
            dialog.dismiss()
        }
    }
}





