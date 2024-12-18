@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskflow123

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: TaskViewModel, navController: NavController) {
    val tasks = viewModel.tasks.collectAsState(initial = emptyList()).value
    val context = LocalContext.current

    val update by viewModel.update_required.collectAsState()

    LaunchedEffect(tasks) {
        Log.e("rep", "update requqired")
        viewModel.syncTasksFromFirestore()
    }

    // Состояние для отображения кнопок "Удалить" и "Отмена"
    val taskToDelete = remember { mutableStateOf<Task?>(null) }

    Scaffold(
       // topBar = { TopAppBar(title = { Text("TaskFlow") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("addTask") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        TaskList(
            tasks = tasks,
            onTaskClick = { task ->
                // Открываем экран задачи
                navController.navigate("taskDetails/${task.id}")
                Log.d("RRR", task.id)
            },
            onTaskLongPress = { task ->
                taskToDelete.value = task // Сохраняем задачу для удаления

            },
            onCancelDelete = {
                taskToDelete.value = null // Отменяем удаление
            },
            onDelete = { task ->
                Log.d("TaskFlow", "Deleting task: ${task.id}")
                viewModel.deleteTask(task)
                // Удаляем задачу
                taskToDelete.value = null // Сбрасываем задачу для удаления
            },
            taskToDelete = taskToDelete.value
        )
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskDetailsScreen(task: Task, onTaskComplete: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(task.title) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Deadline: ${task.dueDate?.toFormattedString()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onTaskComplete,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Задача выполнена")
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskLongPress: (Task) -> Unit,
    onCancelDelete: () -> Unit,
    onDelete: (Task) -> Unit,
    taskToDelete: Task?
) {

    val items =


    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tasks available. Add a new task!")
        }
    } else {
        LazyColumn {
            items(tasks, key = { task -> task.id }) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = onTaskClick,
                    onTaskLongPress = onTaskLongPress,
                    onCancelDelete = onCancelDelete,
                    onDelete = onDelete,
                    isTaskToDelete = task == taskToDelete

                )

            }

        }
    }
}
fun Long.toFormattedString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,          // Короткое нажатие
    onTaskLongPress: (Task) -> Unit,      // Долгое нажатие
    onCancelDelete: () -> Unit,           // Отмена действия
    onDelete: (Task) -> Unit,             // Удалить задачу
    isTaskToDelete: Boolean// Показывать кнопки удаления
) {
    var showDeleteButtons by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { onTaskClick(task) },         // Короткое нажатие
                onLongClick = {                         // Долгое нажатие
                    onTaskLongPress(task)
                }
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = task.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Deadline: ${task.dueDate?.toFormattedString()}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (isTaskToDelete) {
                    Log.d("RRR","Here")
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Button(onClick = onCancelDelete) {
                            Text("Отмена")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {Log.d("TaskFlow", "Delete button clicked for task: ${task.id}")
                                onDelete(task) }, // Удалить задачу
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}

