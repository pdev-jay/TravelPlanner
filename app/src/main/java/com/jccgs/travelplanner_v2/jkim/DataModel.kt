package com.jccgs.travelplanner_v2.jkim

import java.io.Serializable

data class User(var id: String? = null,
                val userEmail: String? = "",
                val displayName: String? = "")


data class Plan(val id: String? = null,
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

data class CheckList(
    var id: String? = null,
    val order: Int = 0,
    var content: String = "",
    @JvmField
    var isChecked: Boolean = false)


data class Expenses(
    var id: String? = null,
    var order: Int = 0,
    var cost: Int = 0,
    var content: String = "",
    var date: String = "")