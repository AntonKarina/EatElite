package com.example.eatelite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private val taskList: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.textViewTaskName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = taskList[position]
        holder.taskName.text = currentTask.name
        holder.checkBox.isChecked = currentTask.isCompleted

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            currentTask.isCompleted = isChecked
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
