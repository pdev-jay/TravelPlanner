package com.jccgs.travelplanner_v2.sjeong

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.jccgs.travelplanner_v2.databinding.DateDialogBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.util.*

class DateDialog(val context: Context): OnDateSelectedListener {
    val exDialog = Dialog(context)
    val dialogView = DateDialogBinding.inflate(LayoutInflater.from(context))
    lateinit var selectDate: Date

    // 인터페이스 타입의 객체를 멤버 변수로 선언
    lateinit var onDialogClickListener: OnDialogClickListener
    // setters
    fun setonDialogClickListener(onDialogClickListener: OnDialogClickListener){
        this.onDialogClickListener = onDialogClickListener
    }

    // 가계부
    fun showExpensesDialog(min: Calendar, max: Calendar) {

        val widget = dialogView.dialogCal
        // 오늘 날짜를 달력에 선택
        widget.selectedDate = CalendarDay.today()
        widget.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
        // 달력의 최대&최소 날짜 지정
        widget.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(min)
            .setMaximumDate(max)
            .commit()
        widget.setOnDateChangedListener(this)
        exDialog.setContentView(dialogView.root)
        exDialog.show()

        // 확인 버튼
        dialogView.btnOk.setOnClickListener {
            val stringDate = SimpleDateFormat("yyyy-MM-dd").format(selectDate)
            onDialogClickListener.onDialogClicked(stringDate)
            exDialog.dismiss()
        }

        // 취소 버튼
        dialogView.btnCancel.setOnClickListener {
            exDialog.dismiss()
        }
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        selectDate = date.date
    }

    // 내부 인터페이스
    interface OnDialogClickListener{
        fun onDialogClicked(date: String)
    }

}