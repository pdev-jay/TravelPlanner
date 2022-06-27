package com.jccgs.travelplanner_v2.jkim

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthController {
    val auth = Firebase.auth

    var currentUser: User? = null

    var googleSignInClient: GoogleSignInClient? = null

}