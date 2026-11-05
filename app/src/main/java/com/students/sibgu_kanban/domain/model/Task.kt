package com.students.sibgu_kanban.domain.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: TaskStatus
)