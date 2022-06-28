package com.jccgs.travelplanner_v2.ckim

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityChecklistCkimBinding

class ChecklistActivity_CKim : AppCompatActivity() {

    lateinit var binding: ActivityChecklistCkimBinding

    //ChecklistData를 저장하는 MutableList
    lateinit var checklistDataList : MutableList<ChecklistData_CKim>
    //리사이클러뷰 어댑터
    lateinit var adapter: ChecklistAdapter_CKim

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistCkimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checklistDataList = mutableListOf<ChecklistData_CKim>()

        adapter = ChecklistAdapter_CKim(checklistDataList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        //플로팅 액션 버튼 누를 때 이벤트
        binding.btnFAB.setOnClickListener {
            //목록 추가
            addItem()
            adapter.notifyDataSetChanged()
            binding.recyclerView.scrollToPosition(checklistDataList.size-1)
            //키보드 올리기
            showKeyboard()
        }

        //구분선
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager(this).orientation)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

    }

    //edittext외 다른 화면을 터치했을 때 키보드 내리기
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        val focusView: View? = currentFocus

        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev!!.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0)
                focusView.clearFocus()
            }

            binding.btnFAB.isEnabled = false

        }

        return super.dispatchTouchEvent(ev)
    }

    //입력창 생성 (리사이클러뷰에 checklistitem을 출력하는 용도)
    fun addItem(){
        checklistDataList.add(ChecklistData_CKim(""))
    }

    //사용자가 입력한 값 넣기
    @SuppressLint("NotifyDataSetChanged")
    fun addChecklistData(position: Int, data: ChecklistData_CKim){
        checklistDataList.set(position,data)
        adapter.notifyDataSetChanged()
    }

    //선택한 항목 삭제
    @SuppressLint("NotifyDataSetChanged")
    fun removeChecklistDataList(position: Int){
        checklistDataList.removeAt(position)
        adapter.notifyDataSetChanged()
    }


    //키보드 올리기
    fun showKeyboard(){
        val imm: InputMethodManager? = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.btnFAB.isEnabled = true
    }

    //키보드 내리기
    fun hideKeyboard(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        binding.btnFAB.isEnabled = true
    }

}