package com.jccgs.travelplanner_v2.jkim

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseController {
    private val db = Firebase.firestore
    val TAG = "Log_debug"

    val USER_REF = db.collection("User")
    val PLAN_REF = db.collection("Plan")

    val DAILYPLAN = "DailyPlan"
    val CHECKLIST = "CheckList"
    val EXPENSES = "Expenses"



     fun getUserPlans(): MutableList<Plan>{
        val plans = mutableListOf<Plan>()
        if (AuthController.currentUser != null) {
            val result =
                PLAN_REF
                    .whereArrayContains("users", AuthController.currentUser!!.id as String)
                    .get()


//            plans.addAll(result)
        }

        return plans
    }


     fun getDailyPlans(planId: String): MutableList<DailyPlan>{
        val dailyPlan = mutableListOf<DailyPlan>()
        if (AuthController.currentUser != null) {
            val result = PLAN_REF.document(planId).collection(DAILYPLAN).get().addOnCompleteListener{

            }
//            dailyPlan.addAll(result)
        }

        return dailyPlan
    }

     fun getCheckList(planId: String): MutableList<CheckList>{
        val checkList = mutableListOf<CheckList>()

        if (AuthController.currentUser != null) {
            val result = PLAN_REF.document(planId).collection(CHECKLIST).get()
//            checkList.addAll(result)
        }

        return checkList
    }

     fun getExpenses(planId: String): MutableList<Expenses>{
        val expenses = mutableListOf<Expenses>()

        if (AuthController.currentUser != null) {
            val result = PLAN_REF.document(planId).collection(EXPENSES).get()
//            expenses.addAll(result)
        }


        return expenses
    }

    fun addPlan(mainPlance: String, period: String, users: MutableList<String>): Boolean{
        var isSuccessful = false

//        val plan = Plan("Test Place", "How long", mutableListOf("sampleUserId"))
//        PLAN_REF.add(plan).addOnSuccessListener {
//            isSuccessful = true
//        }

        return isSuccessful
    }

//    fun addDailyPlan(date: String, placeName: String, placeAddress: String, placeLat: Double, placeLng: Double){
//        val newDailyPlan = DailyPlan(date, placeName, placeAddress, placeLat, placeLng)
//    }

    fun checkSameUser(userEmail: String): Boolean{
        var exist = false

        USER_REF.whereEqualTo("userEmail", userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty){
                   exist = true
                }
            }

        return exist
    }
}