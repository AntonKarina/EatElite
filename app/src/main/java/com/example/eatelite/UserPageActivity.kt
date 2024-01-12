package com.example.eatelite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.math.pow
//import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.example.eatelite.UserData
import com.google.firebase.FirebaseApp

class UserPageActivity : AppCompatActivity() {

    private lateinit var textViewResults: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextAge : EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var radioGroupGoal: RadioGroup
    private lateinit var buttonCalculate: Button
    private lateinit var buttonMealPlan: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        textViewResults = findViewById(R.id.textViewResults)

        editTextAge = findViewById(R.id.editTextAge)
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        radioGroupGoal = findViewById(R.id.radioGroupGoal)
        buttonCalculate = findViewById(R.id.buttonCalculate)
        buttonMealPlan = findViewById(R.id.buttonMealPlan)
        var caloriesForGoal: Double = 0.0

        loadUserData()

        buttonCalculate.setOnClickListener {
            // Obține valorile din câmpuri
            val age = editTextAge.text.toString().toIntOrNull() ?: 0
            val weight = editTextWeight.text.toString().toDoubleOrNull() ?: 0.0
            val height = editTextHeight.text.toString().toDoubleOrNull() ?: 0.0
            val activityLevel =
                spinnerActivityLevel.selectedItemPosition // Poate avea valori 0, 1 sau 2
            val goal = when (radioGroupGoal.checkedRadioButtonId) {
                R.id.radioButtonMaintain -> "Menținere"
                R.id.radioButtonLose -> "Pierdere"
                R.id.radioButtonGain -> "Câștig"
                else -> ""
            }

            val bmi = weight / (height / 100).pow(2)
            val bmr = calculateBMR(weight, height, age)


            val maintenanceCalories = calculateMaintenanceCalories(bmr, activityLevel)
            caloriesForGoal = calculateCaloriesForGoal(maintenanceCalories, goal)


            displayResults(bmi, bmr, maintenanceCalories, caloriesForGoal)

            saveUserData(age, weight, height, activityLevel, goal, bmi, bmr, maintenanceCalories, caloriesForGoal)

        }
        buttonMealPlan.setOnClickListener {

            val intent = Intent(this, MealPlanActivity::class.java)
            intent.putExtra("userCaloricNeeds", caloriesForGoal)  // Sau altceva, depinde de cum ai nevoie
            startActivity(intent)

            navigateToMealPlanActivity(caloriesForGoal)
        }

        val logoutButton: Button = findViewById(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            performLogout()
        }

        val buttonViewTasks: Button = findViewById(R.id.buttonViewTasks)

        // Setează un click listener pentru buton
        buttonViewTasks.setOnClickListener {
            // Deschide TaskListActivity
            val intent = Intent(this, TaskListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun calculateBMR(weight: Double, height: Double, age: Int): Double {
        return 10 * weight + 6.25 * height - 5 * age + 5
    }

    private fun calculateMaintenanceCalories(bmr: Double, activityLevel: Int): Double {
        val activityMultiplier = when (activityLevel) {
            0 -> 1.2 // Nivel scazut
            1 -> 1.5 // Nivel moderat
            2 -> 1.7 // Nivel crescut
            else -> 1.0
        }
        return bmr * activityMultiplier
    }

    private fun calculateCaloriesForGoal(maintenanceCalories: Double, goal: String): Double {
        // Implementează calculul necesarului caloric în funcție de obiectiv (menținere, pierdere, câștig)
        // Exemplu simplu: Calories for goal = Maintenance Calories + adjustment based on goal
        return when (goal) {
            "Menținere" -> maintenanceCalories
            "Pierdere" -> maintenanceCalories - 500.0
            "Câștig" -> maintenanceCalories + 300.0
            else -> maintenanceCalories
        }
    }

    private fun displayResults(
        bmi: Double,
        bmr: Double,
        maintenanceCalories: Double,
        caloriesForGoal: Double
    ) {
        // Textul  afisat în TextView
        val resultsText = """
            BMI: $bmi
            BMR: $bmr
            Calorii Propuse: $caloriesForGoal
        """.trimIndent()

        textViewResults.text = resultsText

    }

    private fun saveUserData(
        age: Int,
        weight: Double,
        height: Double,
        activityLevel: Int,
        goal: String,
        bmi: Double,
        bmr: Double,
        maintenanceCalories: Double,
        caloriesForGoal: Double
    ) {
        currentUser?.let { user ->
            // Creează o referință către colecția "users" în Firestore
            val usersCollection = db.collection("users")

            val userDoc = usersCollection.document(user.uid)

            val userData = UserData(
                age,
                weight,
                height,
                activityLevel,
                goal,
                bmi,
                bmr,
                maintenanceCalories,
                caloriesForGoal
            )

            userDoc.set(userData)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Datele salvate cu succes în Firestore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    // Salvarea a eșuat
                    Toast.makeText(
                        this,
                        "Eroare la salvarea datelor în Firestore",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loadUserData() {
        currentUser?.let { user ->
            val usersCollection = db.collection("users")

            val userDoc = usersCollection.document(user.uid)

            userDoc.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {

                        val userData = document.toObject(UserData::class.java)

                        userData?.let {
                            editTextAge?.setText(it.age.toString())
                            editTextWeight?.setText(it.weight.toString())
                            editTextHeight?.setText(it.height.toString())
                            spinnerActivityLevel?.setSelection(it.activityLevel)

                            when (it.goal) {
                                "Menținere" -> radioGroupGoal.check(R.id.radioButtonMaintain)
                                "Pierdere" -> radioGroupGoal.check(R.id.radioButtonLose)
                                "Câștig" -> radioGroupGoal.check(R.id.radioButtonGain)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Nu s-au găsit date pentru utilizatorul curent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Eroare la citirea datelor din Firestore: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun navigateToMealPlanActivity(userCaloricNeeds: Double) {
        val intent = Intent(this, MealPlanActivity::class.java)
        intent.putExtra("userCaloricNeeds", userCaloricNeeds)
        startActivity(intent)
    }

    fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}



