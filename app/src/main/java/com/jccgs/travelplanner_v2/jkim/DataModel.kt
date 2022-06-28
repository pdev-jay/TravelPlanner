package com.jccgs.travelplanner_v2.jkim

import java.io.Serializable

data class User(var id: String? = "",
                val userEmail: String? = "",
                val displayName: String? = "")


data class Plan(val id: String? = "",
                val mainPlace: String = "",
                val period: MutableList<String> = mutableListOf(),
                val users: MutableList<String> = mutableListOf()): Serializable


data class DailyPlan(
    var order: Int? = 0,
    val date: String = "",
    val placeName: String = "",
    val placeAddress: String = "",
    val placeLat: Double = 0.0,
    val placeLng: Double = 0.0)


data class CheckList(val content: String,
                     val isChecked: Boolean)


data class Expenses(val price: Int,
                    val content: String,
                    val data: String)