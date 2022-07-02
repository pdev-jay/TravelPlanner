package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.toObjects
import com.jccgs.travelplanner_v2.R
import com.jccgs.travelplanner_v2.databinding.ActivityLogInCyunBinding
import com.jccgs.travelplanner_v2.jkim.AuthController
import com.jccgs.travelplanner_v2.jkim.FirebaseController
import com.jccgs.travelplanner_v2.jkim.User

class LogInActivity_CYun : AppCompatActivity() {
    lateinit var binding: ActivityLogInCyunBinding
    lateinit var requestLaucher: ActivityResultLauncher<Intent>
    var googleSignInClient: GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLogInCyunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //콜백함수(구글로그인을 할때 ->인증-> 진짜,가짜)
        requestLaucher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                googleSignInSuccess(result.data)
            }
        }
    }

    override fun onStart() {
        if (AuthController.auth.currentUser != null && AuthController.auth.currentUser!!.isEmailVerified){
            addUser()
        }
        super.onStart()
    }


    fun onClick(view: View){
        when(view?.id){
            //파이어베이스 이메일과 패스워드 인증을 위한 회원가입,  로그인, 로그아웃
            R.id.tvSignUp ->{
                val intent = Intent(this, SignUpActivity_CYun::class.java)
                startActivity(intent)
            }

            R.id.btnLogin ->{
                val email = binding.edtAuthEmail.text.toString()
                val password = binding.edtAuthPassword.text.toString()
                signInWithEmail(email, password)
            }

            //구글 로그인 요청 이벤트
            R.id.btnGoogleLogin->{
                googleSignIn()
            }
        }
    }

    fun googleSignIn(){

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient?.signInIntent

        requestLaucher.launch(signInIntent)
    }

    fun googleSignInSuccess(data: Intent?){

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
        }
    }


    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        AuthController.auth?.signInWithCredential(credential)
            ?.addOnSuccessListener { result ->
                AuthController.currentUser = User(result.user?.uid, result.user?.email, result.user?.displayName)
                addUser()
            }
//            ?.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    AuthController.currentUser = User(AuthController.auth.currentUser?.uid, AuthController.auth.currentUser?.email, AuthController.auth.currentUser?.displayName)
//                    FirebaseController.addUser()
//                    fetchUser()
//                } else {
//                    // If sign in fails, display a message to the user.
//                }
//            }
    }

    fun signInWithEmail(userEmail: String, userPassword: String) {
        if (userEmail.isNullOrBlank() || userPassword.isNullOrBlank()){
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        AuthController.auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful){
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    addUser()
            }else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchUser(){
        if (AuthController.auth.currentUser?.isEmailVerified == true) {
            FirebaseController.USER_REF.whereEqualTo("id", AuthController.auth.currentUser?.uid)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        AuthController.currentUser = task.result.toObjects<User>().first()
                        Log.d("Log_debug", "${AuthController.currentUser?.displayName}")
                        val intent = Intent(this@LogInActivity_CYun, MainActivity_CYun::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
        } else {
            Toast.makeText(this, "이메일 인증을 진행해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    fun addUser(){
        FirebaseController.USER_REF.whereEqualTo("id", AuthController.currentUser?.id).get().addOnSuccessListener { result ->
            if (result.isEmpty){
                val newUser = User(AuthController.currentUser?.id, AuthController.currentUser?.userEmail, AuthController.currentUser?.displayName)
                FirebaseController.USER_REF.document(newUser.id.toString()).set(newUser).addOnSuccessListener {
                    fetchUser()
                }
            } else if (!result.isEmpty){
                fetchUser()
            }
        }
    }
}