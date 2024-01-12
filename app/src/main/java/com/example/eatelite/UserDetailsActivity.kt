package com.example.eatelite

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var textViewUserDetails: TextView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        textViewUserDetails = findViewById(R.id.textViewUserDetails)

        getUserDataFromFirestore()
    }

    private fun getUserDataFromFirestore() {
        currentUser?.let { user ->
            // Referinta catre documentul utilizatorului în Firestore
            val userDoc = db.collection("users").document(user.uid)

            // Obtine date din Firestore
            userDoc.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {

                        val userData = document.toObject(UserData::class.java)
                        displayUserData(userData)
                    } else {

                        Toast.makeText(this, "Datele utilizatorului nu au fost găsite", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->

                    Toast.makeText(this, "Eroare la citirea datelor din Firestore", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayUserData(userData: UserData?) {
        // Afișează datele în TextView sau în altă parte a interfeței utilizatorului
        if (userData != null) {
            val userDetailsText = """
                Age: ${userData.age}
                Weight: ${userData.weight}
                Height: ${userData.height}
                /* ... Adaugă și celelalte câmpuri necesare*/
            """.trimIndent()

            textViewUserDetails.text = userDetailsText
        }
    }
}
