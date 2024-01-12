package com.example.eatelite

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MealPlanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plan)

        val userCaloricNeeds = intent.getDoubleExtra("userCaloricNeeds", 0.0)

        fetchRecipesAndGenerateMealPlan(userCaloricNeeds)
    }


    private fun fetchRecipesAndGenerateMealPlan(userCaloricNeeds: Double) {
        val recipesCollection = FirebaseFirestore.getInstance().collection("recipes")

        recipesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val recipeList = mutableListOf<Recipe>()

                for (document in querySnapshot.documents) {
                    val recipe = document.toObject(Recipe::class.java)
                    recipe?.let {
                        recipeList.add(it)
                        Log.d("MealPlanActivity", "Recipe: $it")
                    }
                }

                Log.d("MealPlanActivity", "Number of recipes: ${recipeList.size}")

                generateMealPlan(recipeList, userCaloricNeeds)

            }
            .addOnFailureListener { exception ->
                Log.e("MealPlanActivity", "Error fetching recipes", exception)
            }
    }


    private fun generateMealPlan(recipeList: List<Recipe>, userCaloricNeeds: Double) {
        val meals = listOf("mic dejun", "pranz", "cina", "snack")
        val mealPlan = mutableMapOf<String, MutableList<Recipe>>()

        val caloriesPerMeal = userCaloricNeeds / 4

        for (meal in meals) {
            Log.d("MealPlanActivity", "  $meal:")
            val selectedRecipes = selectRecipesForMeal(recipeList.toMutableList(), caloriesPerMeal, meal)
            mealPlan[meal] = selectedRecipes
        }

        displayMealPlan(mealPlan)
        val shoppingListButton: Button = findViewById(R.id.buttonOpenShoppingList)
        shoppingListButton.setOnClickListener {
            openShoppingList(mealPlan)
        }
    }



    private fun selectRecipesForMeal(
        recipeList: MutableList<Recipe>,
        caloriesPerMeal: Double,
        mealType: String
    ): MutableList<Recipe> {
        val selectedRecipes = mutableListOf<Recipe>()
        var remainingCalories = caloriesPerMeal
        val maxAttempts = 100

        val suitableRecipes = recipeList.filter { recipe ->
            recipe.mealType == mealType
        }

        Log.d("MealPlanActivity", "Remaining Calories for $mealType: $remainingCalories")

        var attempts = 0
        while (remainingCalories > 0 && suitableRecipes.isNotEmpty() && attempts < maxAttempts) {
            val selectedRecipe = suitableRecipes.shuffled().firstOrNull()

            selectedRecipe?.let {
                selectedRecipes.add(it)
                remainingCalories -= it.calories
                recipeList.remove(it)
                Log.d("MealPlanActivity", "    - ${it.name}")
                Log.d("MealPlanActivity", "    Remaining Calories after adding ${it.name}: $remainingCalories")
            }

            attempts++
        }

        if (selectedRecipes.isEmpty()) {
            Log.d("MealPlanActivity", "    No suitable recipe found for $mealType.")
        }

        return selectedRecipes
    }


    private fun displayMealPlan(mealPlan: Map<String, List<Recipe>>) {
        val linearLayoutMealPlanDynamic = findViewById<LinearLayout>(R.id.linearLayoutMealPlanDynamic)
        linearLayoutMealPlanDynamic.removeAllViews()

        for ((meal, recipes) in mealPlan) {
            val textViewMeal = createTextView(meal)
            linearLayoutMealPlanDynamic.addView(textViewMeal)

            for (recipe in recipes) {
                val textViewRecipeName = createTextView("    - ${recipe.name}")
                val textViewRecipeIngredients = createTextView("        Ingredients: ${recipe.ingredients.joinToString(", ")}")
                val textViewRecipeInstructions = createTextView("        Instructions: ${recipe.instructions}")
                val textViewRecipeCalories = createTextView("        Calories: ${recipe.calories} kcal")

                linearLayoutMealPlanDynamic.addView(textViewRecipeName)
                linearLayoutMealPlanDynamic.addView(textViewRecipeIngredients)
                linearLayoutMealPlanDynamic.addView(textViewRecipeInstructions)
                linearLayoutMealPlanDynamic.addView(textViewRecipeCalories)
            }
        }
    }



    private fun createTextView(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 16f
        textView.setTypeface(null, Typeface.BOLD)
        textView.setPadding(16, 0, 0, 0)
        return textView
    }

    fun navigateBackToUserPage(view: View) {
        val intent = Intent(this, UserPageActivity::class.java)
        startActivity(intent)
    }


    private fun openShoppingList(mealPlan: Map<String, List<Recipe>>) {
        val includedRecipes = mealPlan.flatMap { it.value }
        val intent = Intent(this, ShoppingListActivity::class.java)
        intent.putExtra(ShoppingListActivity.RECIPES_KEY, ArrayList(includedRecipes))
        startActivity(intent)
    }
}
