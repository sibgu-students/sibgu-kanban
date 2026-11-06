package com.students.sibgu_kanban.domain.repository

import com.students.sibgu_kanban.domain.model.Task
import com.students.sibgu_kanban.domain.model.TaskStatus

data class TasksSnapshot(
    val tasks: List<Task>,
    val fromCache: Boolean,
    val hasPendingWrites: Boolean
)

interface TasksRepository {
    /** Returns a function that detaches the listener. */
    fun observe(onChange: (TasksSnapshot) -> Unit, onError: (Exception) -> Unit): () -> Unit
    fun add(title: String, description: String, status: TaskStatus, onError: (Exception) -> Unit)
    fun update(task: Task, onError: (Exception) -> Unit)
    fun delete(id: String, onError: (Exception) -> Unit)
}
