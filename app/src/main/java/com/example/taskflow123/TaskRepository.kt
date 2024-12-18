package com.example.taskflow123



import android.util.Log
import com.example.taskflow123.TaskDao
import com.example.taskflow123.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun getTasksFromFirestore(): List<Task> {
        val snapshot = firestore.collection("tasks").get().await()
        return snapshot.toObjects(Task::class.java)
    }

    suspend fun addTaskToFirestore(task: Task) {
        firestore.collection("tasks").add(task).await()
    }
    suspend fun deleteTaskFromFirestore(task: Task) {
        var dtId = ""
        firestore.collection("/tasks").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("RRR", document.id)
                 //   Log.d("RRR",firestore.collection("/tasks").document("/" + document.id).get().toString())
                    firestore.collection("/tasks").document("/" + document.id).get().addOnSuccessListener {
                        var fieldValue = document.getString("id")
                        Log.d("RRR", fieldValue.toString())
                        if (fieldValue == task.id) {
                            dtId = document.id
                            firestore.collection("/tasks").document("/" + dtId).delete()
                                .addOnSuccessListener {
                                    Log.d("TaskFlow", "Task successfully deleted from Firestore: ${task.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TaskFlow", "Error deleting task from Firestore: ${e.message}")
                                }
                        }
                    }
                }
            }
    }

    fun getLocalTasks() = taskDao.getAllTasks()
    suspend fun saveLocalTask(task: Task) = taskDao.insertTask(task)
    suspend fun saveTasksLocally(tasks: List<Task>) {
        taskDao.insertAll(tasks)
    }
    suspend fun deleteLocalTask(task: Task) = taskDao.deleteTask(task)
    fun observeTasks(): Flow<List<Task>> = callbackFlow {
        val listener = firestore.collection("tasks")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.toObjects(Task::class.java) ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }
    suspend fun clearLocalTasks() {
        taskDao.deleteAllTasks()
    }

}
