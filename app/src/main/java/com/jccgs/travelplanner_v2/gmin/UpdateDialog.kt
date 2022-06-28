package com.example.accountbook.gmin

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.jccgs.travelplanner_v2.databinding.UpdateDialogGminBinding
import com.jccgs.travelplanner_v2.gmin.ExpensesActivity
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity
import com.jccgs.travelplanner_v2.sjeong.DateDialog


class UpdateDialog(val context: Context){

    //멤버변수
    private val dialog = Dialog(context)
    //현재 다이얼로그 창을 객체화함
    private val binding = UpdateDialogGminBinding.inflate(LayoutInflater.from(context))

    //멤버함수
    fun ShowUdDialog(data: ItemViewData, position: Int){

        dialog.setContentView(binding.root)
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        binding.edtUdDateMin.setText(data.tvDate)
        binding.edtUdContentMin.setText(data.tvContent)
        binding.edtUdPayMin.setText(data.tvPay)


        binding.edtUdDateMin.setOnClickListener {
            val expensesDialog = DateDialog(context)
            expensesDialog.showExpensesDialog(DailyPlanActivity.startDate, DailyPlanActivity.endDate)
            expensesDialog.setonDialogClickListener(object: DateDialog.OnDialogClickListener{
                override fun onDialogClicked(date: String) {
                    binding.edtUdDateMin.setText(date)
                }
            })
        }
        //수정 이벤트 처리
        binding.btnUdEnterMin.setOnClickListener{
            val edtUdContent = binding.edtUdContentMin.text.toString()
            val edtUdPay = binding.edtUdPayMin.text.toString().toInt()
            val edtUdDate = binding.edtUdDateMin.text.toString()
            val ABitemViewData = ItemViewData(edtUdDate, edtUdContent, edtUdPay)
            (context as ExpensesActivity).updateItemViewDataList(position, ABitemViewData)

            dialog.dismiss()
        }

        //취소하기 이벤트 처리
        binding.btnUdExitMin.setOnClickListener{
            dialog.dismiss()
        }
    }
}





