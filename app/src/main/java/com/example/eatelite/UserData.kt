package com.example.eatelite


    data class UserData(
        val age: Int =0,
        val weight: Double=0.0,
        val height: Double=0.0,
        val activityLevel: Int=0,
        val goal: String="",
        val bmi: Double=0.0,
        val bmr: Double=0.0,
        val maintenanceCalories: Double=0.0,
        val caloriesForGoal: Double=0.0
    )
