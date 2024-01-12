package com.example.eatelite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ShoppingListActivity : AppCompatActivity() {

    companion object {
        const val RECIPES_KEY = "recipes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        val recipes = intent.getSerializableExtra(RECIPES_KEY) as? List<Recipe> ?: emptyList()
        Log.d("ShoppingListActivity", "Number of recipes: ${recipes.size}")

        val shoppingItems = extractShoppingItems(recipes)
        Log.d("ShoppingListActivity", "Shopping items: $shoppingItems")

        val listViewShoppingList: ListView = findViewById(R.id.listViewShoppingList)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shoppingItems)

        listViewShoppingList.adapter = adapter

        val buttonBackToMealPlan: Button = findViewById(R.id.buttonBackToMealPlan)


        buttonBackToMealPlan.setOnClickListener {
            val intent = Intent(this, MealPlanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun extractShoppingItems(recipes: List<Recipe>): List<String> {
        val shoppingItems = mutableListOf<String>()

        for (recipe in recipes) {
            for (ingredient in recipe.ingredients) {
                shoppingItems.add(ingredient)
            }
        }

        return shoppingItems.distinct()
    }

}
