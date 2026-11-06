package com.students.sibgu_kanban.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.students.sibgu_kanban.domain.model.Task
import com.students.sibgu_kanban.domain.model.TaskStatus
import com.students.sibgu_kanban.domain.repository.TasksRepository
import com.students.sibgu_kanban.domain.repository.TasksSnapshot

/** All installations using this Firebase project share the tasks collection. */
class FirestoreTasksRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TasksRepository {
    private val tasks = firestore.collection("tasks")

    override fun observe(
        onChange: (TasksSnapshot) -> Unit,
        onError: (Exception) -> Unit
    ): () -> Unit {
        val registration = tasks.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            try {
                val items = snapshot.documents.map { doc ->
                    Task(
                        id = doc.id,
                        title = requireNotNull(doc.getString("title")) { "У задачи ${doc.id} нет названия" },
                        description = doc.getString("description").orEmpty(),
                        status = TaskStatus.valueOf(requireNotNull(doc.getString("status")))
                    )
                }.sortedBy { it.id }
                onChange(TasksSnapshot(items, snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites()))
            } catch (error: Exception) {
                onError(error)
            }
        }
        return { registration.remove() }
    }

    override fun add(title: String, description: String, status: TaskStatus, onError: (Exception) -> Unit) {
        // Firestore IDs avoid collisions when several devices create tasks offline.
        tasks.document().set(fields(title, description, status)).addOnFailureListener { onError(it) }
    }

    override fun update(task: Task, onError: (Exception) -> Unit) {
        // update fails if another device has deleted the task; it never recreates it.
        tasks.document(task.id).update(fields(task.title, task.description, task.status))
            .addOnFailureListener { onError(it) }
    }

    override fun delete(id: String, onError: (Exception) -> Unit) {
        tasks.document(id).delete().addOnFailureListener { onError(it) }
    }

    private fun fields(title: String, description: String, status: TaskStatus): Map<String, Any> =
        mapOf("title" to title, "description" to description, "status" to status.name)
}
