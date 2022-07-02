package com.jccgs.travelplanner_v2.cyun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivitySignUpCyunBinding
import com.jccgs.travelplanner_v2.jkim.AuthController
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.User

class SignUpActivity_CYun : AppCompatActivity() {
    lateinit var binding: ActivitySignUpCyunBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    fun onClick(view: View) {
        when (view?.id) {
            //파이어베이스 이메일과 패스워드 인증을 위한 회원가입,  로그인, 로그아웃
            R.id.btnSign -> {
                val nickname = binding.edtAuthNickname.text.toString()
                val email = binding.edtAuthEmail.text.toString()
                val password = binding.edtAuthPassword.text.toString()

                signUp(email, password, nickname)

            }
        }
    }

    fun signUp(userEmail: String, userPassword: String, displayName: String) {
        if (userEmail.isNullOrBlank() || userPassword.isNullOrBlank() || displayName.isNullOrBlank()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnSuccessListener { result ->
                    AuthController.currentUser = User(AuthController.auth.currentUser?.uid, AuthController.auth.currentUser?.email, displayName)
                    FirebaseController.USER_REF.document(AuthController.currentUser!!.id.toString()).set(
                        AuthController.currentUser!!
                    ).addOnSuccessListener {
                        AuthController.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener {
                                AlertDialog.Builder(this@SignUpActivity_CYun).run {
                                    setTitle("회원가입 인증메일 발송완료")
                                    setMessage("입력하신 이메일로 발송된 인증메일을 통해 회원가입하세요")
                                    setPositiveButton("확인") { p0, p1 -> finish() }
                                    //창 띄운 상태에서 뒤로가기 못 누름
                                    setCancelable(false)
                                    show()
                                }.setCanceledOnTouchOutside(false) //창 밖 화면 터치해도 못 나감
                            }
                        binding.edtAuthNickname.text.clear()
                        binding.edtAuthEmail.text.clear()
                        binding.edtAuthPassword.text.clear()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}