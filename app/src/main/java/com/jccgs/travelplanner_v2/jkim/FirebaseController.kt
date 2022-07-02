package com.jccgs.travelplanner_v2.jkim

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseController {
    private val db = Firebase.firestore

    val USER_REF = db.collection("User")
    val PLAN_REF = db.collection("Plan")
}