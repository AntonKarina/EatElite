package com.example.eatelite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: MutableList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

             taskList = mutableListOf(
            Task("Mic dejun"),
            Task("Prânz"),
            Task("Cină"),
            Task("Snack"),
            Task("Consum de apă"),
            Task("Antrenament")
        )


        restoreTaskStates(taskList)

        recyclerView = findViewById(R.id.recyclerViewTasks)
        taskAdapter = TaskAdapter(taskList)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)



        val buttonBackToUserPage: Button = findViewById(R.id.buttonBackToUserPage2)
        buttonBackToUserPage.setOnClickListener {
            navigateBackToUserPage2()
        }

    }

    override fun onPause() {
        super.onPause()
        saveTaskStates(taskList)
    }

    private fun completeAllTasks(taskList: MutableList<Task>) {
        taskList.forEach { it.isCompleted = true }
        taskAdapter.notifyDataSetChanged()
    }

    private fun navigateBackToUserPage2() {
        val intent = Intent(this, UserPageActivity::class.java)
        startActivity(intent)
    }
    private fun restoreTaskStates(taskList: MutableList<Task>) {
        val sharedPreferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE)

        taskList.forEach { task ->
            val isChecked = sharedPreferences.getBoolean(task.name, false)
            task.isCompleted = isChecked
        }
    }


    private fun saveTaskStates(taskList: MutableList<Task>) {
        val sharedPreferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        taskList.forEach { task ->
            editor.putBoolean(task.name, task.isCompleted)
        }

        editor.apply()
    }

}
