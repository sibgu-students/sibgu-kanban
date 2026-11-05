package com.students.sibgu_kanban.domain.model

enum class TaskStatus(val title: String) {
    TODO("В очереди"),
    IN_PROGRESS("В процессе"),
    DONE("Выполнено")
}
