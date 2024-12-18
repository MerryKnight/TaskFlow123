package com.example.taskflow123

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.room.PrimaryKey
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
   // var taskDate by remember { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    val dateFormatter = rememberSaveable { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val showDatePicker = remember { mutableStateOf(false) }
    if (showDatePicker.value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                showDatePicker.value = false
                selectedDate = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    val showTimePicker = remember { mutableStateOf(false) }
    if (showTimePicker.value) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate!!
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                showTimePicker.value = false
                selectedDate = calendar.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Column {
        TextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Название") },
            modifier = Modifier
                .fillMaxWidth()

        )
        TextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            label = { Text("Описание") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Кнопка для выбора даты
        Button(onClick = { showDatePicker.value = true }) {
            Text("Выбрать день")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Кнопка для выбора времени
        Button(onClick = { showTimePicker.value = true }) {
            Text("Выбрать время")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Показать выбранную дату и время
        selectedDate?.let {
            Text("Due Date: ${dateFormatter.format(Date(it))}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*   TextField(
               value = taskDate,
               onValueChange = { taskDate = it },
               label = { Text("Date") }
           )*/
        Button(onClick = {
            val task = Task(title = taskTitle, description = taskDescription, dueDate = selectedDate)
            viewModel.addTask(task)
            scheduleNotification(context, task)
            navController.navigate("home")
        }) {
            Text("Добавить задачу")
        }
    }
}