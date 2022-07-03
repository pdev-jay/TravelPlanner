package com.jccgs.travelplanner_v2.gmin

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Slide
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.transition.Fade
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.ckim.ChecklistActivity_CKim
import com.jccgs.travelplanner_v2.cyun.MainActivity_CYun
import com.jccgs.travelplanner_v2.databinding.ActivityExpensesBinding
import com.jccgs.travelplanner_v2.jkim.Expenses
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.sjeong.CalendarActivity_SJeong
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong

class ExpensesActivity : AppCompatActivity() {
    lateinit var binding : ActivityExpensesBinding
    lateinit var customAdapter : CustomAdapter
    val itemViewDataList : MutableList<Expenses> = mutableListOf<Expenses>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //레이아웃매니저 결정할것
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        //어댑터를 객체화시키고 recyclerView adapter에 연결
        customAdapter = CustomAdapter(itemViewDataList)
        binding.recyclerView.adapter = customAdapter

        //이벤트처리 ( FloatingActionButton ) : 사용자 정의창을 불러서 설계
        binding.floatingActionButton.setOnClickListener {
            val CustomDialog = CustomDialog(this)

            CustomDialog.ShowDialog()
        }

        // 하단 버튼을 통해 다른 액티비티로 이동
        binding.buttomBtnLayout.btnExpenses.setBackgroundColor(Color.parseColor("#C3B8D9"))
        binding.buttomBtnLayout.btnPlan.setBackgroundColor(Color.WHITE)
        binding.buttomBtnLayout.btnCheckList.setBackgroundColor(Color.WHITE)

        binding.buttomBtnLayout.btnPlan.setOnClickListener {
            startActivity(Intent(this, DailyPlanActivity_SJeong::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.buttomBtnLayout.btnCheckList.setOnClickListener {
            startActivity(Intent(this, ChecklistActivity_CKim::class.java))
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnDoneExpenses.setOnClickListener {
            val intent = Intent(this, MainActivity_CYun::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onStart() {
        FirebaseController.PLAN_REF
            .document(CalendarActivity_SJeong.documentId.toString())
            .collection("Expenses")
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                itemViewDataList.clear()
                for (i in snapshot){
                    itemViewDataList.add(i.toObject<Expenses>())
                }
                customAdapter.notifyDataSetChanged()
            }

        super.onStart()
    }


    fun getSum(){
        var sum = 0

        for (i in itemViewDataList){
            sum += i.cost
        }

        binding.tvTotalPayMin.text = sum.toString()
    }

    fun addItemViewDataList(itemViewData: Expenses) {
        if (itemViewDataList.isNotEmpty()){
            itemViewData.order = itemViewDataList.last().order + 1
        }
        FirebaseController
            .PLAN_REF
            .document(CalendarActivity_SJeong.documentId.toString())
            .collection("Expenses")
            .add(itemViewData)
            .addOnSuccessListener { docRef ->
                docRef.update("id", docRef.id)
                this.itemViewDataList.add(itemViewData)
                itemViewData.id = docRef.id
                customAdapter.notifyDataSetChanged()
                getSum()
            }
        Toast.makeText(this, "금액 사용 기록이 추가 되었습니다.", Toast.LENGTH_SHORT).show()
    }

    fun updateItemViewDataList(position: Int, itemViewData: Expenses){
        FirebaseController
            .PLAN_REF
            .document(CalendarActivity_SJeong.documentId.toString())
            .collection("Expenses")
            .document(itemViewData.id.toString())
            .set(itemViewData)
            .addOnSuccessListener {
                this.itemViewDataList.set(position, itemViewData)
                customAdapter.notifyDataSetChanged()
                getSum()
            }
    }

    fun removeItemViewDataList(itemViewData: Expenses) {
        var dialog = Dialog(applicationContext)

        // 다이얼로그 이벤트 핸들러 등록
        val eventHandler = object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> {
                        FirebaseController
                            .PLAN_REF
                            .document(CalendarActivity_SJeong.documentId.toString())
                            .collection("Expenses")
                            .document(itemViewData.id.toString())
                            .delete()
                            .addOnSuccessListener {
                                itemViewDataList.remove(itemViewData)
                                customAdapter.notifyDataSetChanged()
                                Toast.makeText(applicationContext,
                                    "${itemViewData.date}에 ${itemViewData.content} 기록이 삭제 되었습니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                                getSum()
                            }
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


