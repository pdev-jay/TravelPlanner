package com.jccgs.travelplanner_v2.jkim

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseController {
    //User 와 Plan Collection 접근
    val USER_REF = Firebase.firestore.collection("User")
    val PLAN_REF = Firebase.firestore.collection("Plan")
}