package com.example.taskflow123


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class TaskViewModel @Inject constructor(private val repository: TaskRepository) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    var update_required = MutableStateFlow<Boolean>(false);

    init {
        fetchTasks()
    }

    private fun fetchTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getTasksFromFirestore()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.saveLocalTask(task)
            repository.addTaskToFirestore(task)
            fetchTasks()
        }

    }
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTaskFromFirestore(task)
            repository.deleteLocalTask(task)
            fetchTasks()

        }
    }

    fun syncTasksFromFirestore() {
        viewModelScope.launch {
            repository.observeTasks().collect { tasks ->
                repository.saveTasksLocally(tasks)
            }
        }
    }
}
