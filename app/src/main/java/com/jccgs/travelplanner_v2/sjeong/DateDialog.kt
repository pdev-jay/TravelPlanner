package com.jccgs.travelplanner_v2.sjeong

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.LayoutInflater
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.DateDialogBinding
import com.prolificinteractive.materialcalendarview.*
import java.text.SimpleDateFormat
import java.util.*

class DateDialog(val context: Context): OnDateSelectedListener {
    val exDialog = Dialog(context)
    val dialogView = DateDialogBinding.inflate(LayoutInflater.from(context))
    private var selectDate: Date? = null

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
        widget.currentDate = CalendarDay.today()
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

        // 데코레이션
        widget.addDecorator(object: DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay?): Boolean {
                return day?.equals(CalendarDay.today()) == true
            }
            override fun decorate(view: DayViewFacade?) {
                val drawable = context.getDrawable(R.drawable.today_selector)
                view?.setBackgroundDrawable(drawable!!)
                view?.addSpan(StyleSpan(Typeface.BOLD))
            }
        })
        // 주말
        widget.addDecorators(SaturdayDeco(), SundayDeco())

        // 확인 버튼
        dialogView.btnOk.isEnabled = false
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
        dialogView.btnOk.isEnabled = true
    }

    // 내부 인터페이스
    interface OnDialogClickListener{
        fun onDialogClicked(date: String)
    }

}