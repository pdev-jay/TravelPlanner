package com.jccgs.travelplanner_v2.sjeong

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.StyleSpan
import android.util.Log
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityCalendarBinding
import com.jccgs.travelplanner_v2.jkim.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity_SJeong : AppCompatActivity() {

    companion object{
        var startDate: Calendar? = null
        var endDate: Calendar? = null
        var stringDayList: MutableList<String> = mutableListOf()
        var documentId: String? = null
    }

    val binding by lazy { ActivityCalendarBinding.inflate(layoutInflater) }

    var dayList: ArrayList<CalendarDay> = arrayListOf()
    var term = 0

    lateinit var planTitle: String
    var invitedUsers: ArrayList<User> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

         startDate = null
         endDate = null
         stringDayList = mutableListOf()
         documentId = null

        if (intent.hasExtra("planTitle")){
            planTitle = intent.getStringExtra("planTitle") as String
        }
        if (intent.hasExtra("invitedUsers")){
            invitedUsers = intent.getSerializableExtra("invitedUsers") as ArrayList<User>
        }


        val calendarView = binding.calendarView

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
        calendarView.addDecorators(SaturdayDeco(), SundayDeco())

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
           savePlan()
        }
    }

    fun savePlan(){
        if(!dayList.isNullOrEmpty()) {
            for (i in dayList!!) {
                val date = SimpleDateFormat("yyyy-MM-dd").format((i as CalendarDay).date)
                stringDayList.add(date)
            }
        } else {
            dayList.add(CalendarDay.today())
            val date = SimpleDateFormat("yyyy-MM-dd").format(CalendarDay.today().date)
            stringDayList.add(date)
            startDate = CalendarDay.today().calendar
            endDate = CalendarDay.today().calendar
            term = dayList.size
        }

        val users = mutableListOf<String>(AuthController.currentUser?.id.toString())
        if (!invitedUsers.isNullOrEmpty()){
            for (user in invitedUsers){
                users.add(user.id.toString())
            }
        }
        val newPlan = Plan(null, planTitle, MapController.selectedPlaceCountryName.toString(), MapController.selectedPlaceCity.toString(), MapController.selectedPlaceShortName.toString(), stringDayList, users)
        FirebaseController.PLAN_REF.add(newPlan).addOnSuccessListener { docRef ->
            documentId = docRef.id
            FirebaseController.PLAN_REF.document(docRef.id).update("id", docRef.id)
                .addOnSuccessListener {
                    val intent = Intent(this, DailyPlanActivity_SJeong::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
        }
    }
}