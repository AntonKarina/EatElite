package com.example.eatelite

import com.google.firebase.firestore.PropertyName
import java.io.Serializable


data class Recipe(
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    @PropertyName("instructions")val instructions: String = "",
    val calories: Double = 0.0,
    @PropertyName("mealType") val mealType: String = ""
    ): Serializable

