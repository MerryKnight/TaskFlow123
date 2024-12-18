package com.example.taskflow123


import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import java.io.File

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

    }


}