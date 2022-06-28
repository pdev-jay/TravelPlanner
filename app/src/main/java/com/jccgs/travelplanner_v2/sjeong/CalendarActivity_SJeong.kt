package com.jccgs.travelplanner_v2.sjeong

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.StyleSpan
import android.util.Log
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class CalendarActivity_SJeong : AppCompatActivity() {
    val binding by lazy { ActivityCalendarBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val calendarView = binding.calendarView

        var dayList: ArrayList<CalendarDay> = arrayListOf()
        var term = 0
        // 여행 시작/끝 날짜
        var startDate: Calendar? = null
        var endDate: Calendar? = null

        // 달력을 현재 날짜로 설정
        var calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendarView.setCurrentDate(CalendarDay.today())

        // 달력 시작 요일 및 최소&최대 날짜 지정
        val min = Calendar.getInstance()
        min.set(currentYear, currentMonth, currentDay)
        val max = Calendar.getInstance()
        max.set(currentYear+1, currentMonth, currentDay)
        calendarView.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(min)
            .setMaximumDate(max)
            .commit()

        // 데코레이션
        calendarView.addDecorator(object: DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay?): Boolean {
                return day?.equals(CalendarDay.today()) == true
            }
            override fun decorate(view: DayViewFacade?) {
                val drawable = applicationContext.getDrawable(R.drawable.today_selector)
                view?.setBackgroundDrawable(drawable!!)
                view?.addSpan(StyleSpan(Typeface.BOLD))
            }
        })
        // 주말
        calendarView.addDecorator(HolidayDeco())

        // 날짜 선택
        calendarView.setOnDateChangedListener { widget, date, selected ->
            if(!selected) {
                dayList.clear()
                startDate = null
                endDate = null
            }else {
                startDate = date.calendar
                endDate = date.calendar
                dayList.add(date)
            }
            term = dayList.size
        }

        // 날짜 범위 선택
        calendarView.setOnRangeSelectedListener { widget, dates ->
            startDate = dates[0].calendar
            endDate = dates[dates.size - 1].calendar
            Log.d("log", "startdate: ${startDate}, endDate: ${endDate}")
            term = dates.size
            dayList = dates as ArrayList<CalendarDay>
        }

        // 확인 버튼
        binding.btnOk.setOnClickListener {
            val intent = Intent(this, DailyPlanActivity_SJeong::class.java)
            // 날짜를 선택하지 않았을 경우
            if(dayList.isNullOrEmpty()) {
                dayList.add(CalendarDay.today())
                startDate = CalendarDay.today().calendar
                endDate = CalendarDay.today().calendar
                term = dayList.size
            }
            intent.putExtra("dayList", dayList)
            intent.putExtra("startDate", startDate)
            intent.putExtra("endDate", endDate)
            intent.putExtra("term", term)
            startActivity(intent)
        }
        // 돌아가기 버튼
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}