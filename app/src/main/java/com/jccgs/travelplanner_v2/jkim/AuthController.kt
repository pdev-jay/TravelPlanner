package com.jccgs.travelplanner_v2.jkim

import android.content.Context
import android.provider.Settings.Global.getString
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jccgs.travelplanner_v2.R

object AuthController {
    val auth = Firebase.auth
    var currentUser: User? = null
}