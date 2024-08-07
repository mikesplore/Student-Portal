package com.mike.unikonnect

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mike.unikonnect.model.Global
import com.mike.unikonnect.notification.createNotificationChannel
import com.mike.unikonnect.settings.BiometricPromptManager

class MainActivity : AppCompatActivity() {
    val promptManager by lazy {
        BiometricPromptManager(this)
    }

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_UniKonnect)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
            NavigationGraph(this)
        }
        createNotificationChannel(this)
    }


    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Global.showAlert.value = false
            } else {
                // Permission already granted
                Global.showAlert.value = true
                sharedPreferences.edit().putBoolean("NotificationPermissionGranted", true).apply()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Global.showAlert.value = true
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putBoolean("NotificationPermissionGranted", true).apply()
            } else {
                Global.showAlert.value = false
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

}



