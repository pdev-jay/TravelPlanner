package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.jccgs.travelplanner_v2.databinding.UpdateDialogGminBinding
import com.jccgs.travelplanner_v2.jkim.Expenses
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DateDialog

class UpdateDialog(val context: Context){

    //멤버변수
    private val dialog = Dialog(context)
    //현재 다이얼로그 창을 객체화함
    private val binding = UpdateDialogGminBinding.inflate(LayoutInflater.from(context))
    private lateinit var data: Expenses
    //멤버함수
    fun ShowUdDialog(data: Expenses, position: Int){

        dialog.setContentView(binding.root)
        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        this.data = data
        binding.edtUdDateMin.setText(data.date)
        binding.edtUdContentMin.setText(data.content)
        binding.edtUdPayMin.setText(data.cost.toString())

        binding.edtUdDateMin.setOnClickListener {
            val expensesDialog = DateDialog(context)
            expensesDialog.showExpensesDialog(DailyPlanActivity_SJeong.startDate, DailyPlanActivity_SJeong.endDate)
            expensesDialog.setonDialogClickListener(object: DateDialog.OnDialogClickListener{
                override fun onDialogClicked(date: String) {
                    binding.edtUdDateMin.setText(date)
                }
            })
        }

        //수정 이벤트 처리
        binding.btnUdEnterMin.setOnClickListener{
            data.content = binding.edtUdContentMin.text.toString()
            data.cost = binding.edtUdPayMin.text.toString().toInt()
            data.date = binding.edtUdDateMin.text.toString()
//            val itemViewData = Expenses(date = edtUdDate, cost = edtUdPay, content = edtUdContent)
            (context as ExpensesActivity).updateItemViewDataList(position, data)
            (context as ExpensesActivity).getSum()

            dialog.dismiss()
        }

        //취소하기 이벤트 처리
        binding.btnUdExitMin.setOnClickListener{
            dialog.dismiss()
        }
    }
}