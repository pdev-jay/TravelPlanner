package com.jccgs.travelplanner_v2.ckim

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Paint
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ChecklistItemCkimBinding
import com.jccgs.travelplanner_v2.jkim.CheckList
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.sjeong.DailyPlanActivity_SJeong

class ViewHolder(val binding: ChecklistItemCkimBinding): RecyclerView.ViewHolder(binding.root)

class ChecklistAdapter_CKim(val checklistDatalist: MutableList<CheckList>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var mainActivity: ChecklistActivity_CKim
    var isEditing = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //mainActivity 객체 가져오기
        mainActivity = parent.context as ChecklistActivity_CKim

        val binding = ChecklistItemCkimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ViewHolder(binding)

        //삭제
        binding.ivDelete.setOnClickListener {

            var dialog = Dialog(mainActivity)

            //삭제 다이얼로그
            val eventHandler = object: DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    when(p1){
                        DialogInterface.BUTTON_POSITIVE -> {
                            val position = viewHolder.adapterPosition
                            mainActivity.removeCheckList(position)

                            //삭제대상항목에 체크박스가 체크되있거나 취소선이 그어져 있으면 모두 해제함
//                            if (binding.checkBox.isChecked) {
//                                //취소선 제거
//                                binding.edtInput.paintFlags = binding.edtInput.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
//                                //체크 해제
//                                binding.checkBox.isChecked = false
//                            }
                            dialog.dismiss()
                        }

                        DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss()
                    }
                }
            }

            AlertDialog.Builder(mainActivity).run {
                setMessage("삭제하시겠습니까?")
                setPositiveButton("삭제", eventHandler)
                setNegativeButton("취소", eventHandler)
                //창을 띄운 상태에서 뒤로가기를 눌렀을 때 못 나가게 함
                setCancelable(false)
                show()
            }.setCanceledOnTouchOutside(false)

            return@setOnClickListener
        }


        return viewHolder

    }//end of onCreateViewHolder

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val binding = (holder as ViewHolder).binding
        binding.edtInput.setText(checklistDatalist[position].content)

        if(binding.edtInput.text.toString() == ""){
            //플로팅버튼 비활성화
            mainActivity.binding.btnFAB.isEnabled = false

            //edittext 속성 활성화
            binding.edtInput.isFocusableInTouchMode = true
            binding.edtInput.isFocusable = true
            binding.edtInput.isClickable = true

            //edittext 입력시 밑줄 생성
            binding.edtInput.setBackgroundResource(R.drawable.underline_ckim)

        }else if(binding.edtInput.text.toString() != ""){
            //플로팅버튼 활성화
            mainActivity.binding.btnFAB.isEnabled = true
        }

        //자동 포커스
        binding.edtInput.requestFocus()

        //체크박스 클릭 이벤트

        binding.checkBox.setOnCheckedChangeListener { button, isChecked ->
            updateIsChecked(position, isChecked)
        }

        if (checklistDatalist[position].isChecked){
            binding.checkBox.isChecked = true
            binding.edtInput.paintFlags = binding.edtInput.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.checkBox.isChecked = false
            binding.edtInput.paintFlags = binding.edtInput.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        //edittext 입력 후 키보드의 완료버튼을 눌렀을 때 발생하는 이벤트
        binding.edtInput.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    //입력값이 없을 때
                    if(binding.edtInput.text.toString() == ""){
                        //플로팅버튼 비활성화
                        mainActivity.binding.btnFAB.isEnabled = false

                        //입력값이 있을 때
                    }else if(binding.edtInput.text.toString() != ""){
                        //플로팅버튼 활성화
                        mainActivity.binding.btnFAB.isEnabled = true

                        if (isEditing){
                            checklistDatalist[position].content = binding.edtInput.text.toString()
                            updateContent(position)
                        } else {
                            val order = checklistDatalist.last().order
                            val data =
                                CheckList(order = order, content = binding.edtInput.text.toString())
                            mainActivity.addCheckListToDB(data, position)
                        }
                        binding.edtInput.setText(binding.edtInput.text.toString())

                        //edittext 속성 비활성화
                        binding.edtInput.isFocusableInTouchMode = false
                        binding.edtInput.isFocusable = false
                        binding.edtInput.isClickable = false

                        //edittext 밑줄 제거
                        binding.edtInput.background = null

                        //키보드 내리기
                        mainActivity.hideKeyboard()
                    }
                    return true
                }else{
                    return true
                }

            }
        })


        //수정
        binding.ivModify.setOnClickListener {
            isEditing = true
            //키보드 올리기
            mainActivity.showKeyboard()
            binding.edtInput.requestFocus()

            //edittext에 밑줄 생성
            binding.edtInput.setBackgroundResource(R.drawable.underline_ckim)

            //edittext 활성화
            binding.edtInput.isFocusableInTouchMode = true
            binding.edtInput.isFocusable = true
            binding.edtInput.isClickable = true

            //커서를 글자의 맨 끝으로
            binding.edtInput.post(Runnable {binding.edtInput.setSelection(binding.edtInput.length())})
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return checklistDatalist.size
    }

    fun updateIsChecked(position: Int, isChecked: Boolean){
        FirebaseController
            .PLAN_REF
            .document(DailyPlanActivity_SJeong.documentId.toString())
            .collection("CheckList")
            .document(checklistDatalist[position].id.toString())
            .update("isChecked", isChecked)
            .addOnSuccessListener {
                checklistDatalist[position].isChecked = isChecked
                notifyDataSetChanged()
            }
    }

    fun updateContent(position: Int){
        FirebaseController
            .PLAN_REF
            .document(DailyPlanActivity_SJeong.documentId.toString())
            .collection("CheckList")
            .document(checklistDatalist[position].id.toString())
            .update("content", checklistDatalist[position].content)
            .addOnSuccessListener {
                notifyDataSetChanged()
                isEditing = false
            }
    }


}