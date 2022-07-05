package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.jccgs.travelplanner_v2.databinding.UpdateDialogGminBinding
import com.jccgs.travelplanner_v2.jkim.Expenses
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
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

        //다이얼로그가 나올때 아이템뷰에 입력돼있던 데이터가 미리 입력 돼있도록 한다.
        binding.edtUdDateMin.setText(data.date)
        binding.edtUdContentMin.setText(data.content)
        binding.edtUdPayMin.setText(data.cost.toString())

        //날짜칸을 클릭하면 DateDialog가 나온다
        binding.edtUdDateMin.setOnClickListener {
            val expensesDialog = DateDialog(context)
            expensesDialog.showExpensesDialog(CalendarActivity_SJeong.startDate!!, CalendarActivity_SJeong.endDate!!)
            expensesDialog.setonDialogClickListener(object: DateDialog.OnDialogClickListener{
                override fun onDialogClicked(date: String) {
                    binding.edtUdDateMin.setText(date)
                }
            })
        }

        //수정 이벤트 처리
        binding.btnUdEnterMin.setOnClickListener{
            data.content = binding.edtUdContentMin.text.toString()
            var pay = binding.edtUdPayMin.text.toString()
            data.date = binding.edtUdDateMin.text.toString()

            //금액이나 내역을 입력하지않고 완료를 눌렀을때 경고 메세지를 출력한다.
            if(data.content.isNullOrEmpty() || pay.isNullOrEmpty()){
                Toast.makeText(context,"입력하지 않은 항목이 있습니다.", Toast.LENGTH_SHORT).show()
            }else{
                //모든 정보를 입력하고 완료버튼을 눌렀을땐 값이 입력되고 창이 종료된다.
                data.cost = pay.toInt()
                (context as ExpensesActivity).updateItemViewDataList(position, data)
                (context as ExpensesActivity).getSum()
                dialog.dismiss()
            }
        }

        //취소하기 이벤트 처리
        binding.btnUdExitMin.setOnClickListener{
            dialog.dismiss()
        }
    }
}