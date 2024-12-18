package com.example.taskflow123

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.firebase.FirebaseApp

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        setContent {
            val taskViewModel: TaskViewModel = hiltViewModel()
            taskViewModel.syncTasksFromFirestore()
            MainScreen(taskViewModel)
        }

    }


}


@Composable
fun MainScreen(taskViewModel: TaskViewModel) {

    val navController = rememberNavController()
    val tasks by taskViewModel.tasks.collectAsState()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = taskViewModel, navController = navController)
        }
        composable("addTask") {
            AddTaskScreen(navController = navController, viewModel = taskViewModel)
        }
        composable("taskDetails/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = tasks.find { it.id == taskId } //тут исправт
                if (task != null) {
                    TaskDetailsScreen(task = task) {
                        // Удаляем задачу, когда она выполнена
                        taskViewModel.deleteTask(task)
                        navController.popBackStack()
                    }
                } else {
                    Log.d("TaskDetails", "Task not found for id: $taskId")
                }

        }
    }
}
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "task_reminder_channel",
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for task reminders"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
fun scheduleNotification(context: Context, task: Task) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", task.title)
        putExtra("description", task.description)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.hashCode(), // Используем ID задачи для уникальности
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        task.dueDate ?: System.currentTimeMillis(), // Используем timestamp из задачи
        pendingIntent
    )
}
fun cancelNotification(context: Context, taskId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)
}