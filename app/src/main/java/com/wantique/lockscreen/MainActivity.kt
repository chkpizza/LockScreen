package com.wantique.lockscreen

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.wantique.lockscreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val startActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        /**
         * always returned RESULT_CANCEL
         * */
        if(!Settings.canDrawOverlays(this@MainActivity)) {
            Toast.makeText(this@MainActivity, "Draw Overlay 권한을 거부하였습니다.", Toast.LENGTH_SHORT).show()
        } else {
            startLockScreenService()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            checkDrawOverlayPermission()
        } else {
            Toast.makeText(this@MainActivity, "알림 권한을 거부하였습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * [SYSTEM_ALERT_WINDOW permission]
     * Allows an app to create windows using the type WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, shown on top of all other apps.
     * Very few apps should use this permission; these windows are intended for system-level interaction with the user.
     *
     * Note: If the app targets API level 23 or higher, the app user must explicitly grant this permission to the app through a permission management screen.
     * The app requests the user's approval by sending an intent with action Settings.ACTION_MANAGE_OVERLAY_PERMISSION.
     * The app can check whether it has this authorization by calling Settings.canDrawOverlays().
     * */
    private fun checkDrawOverlayPermission() {
        if(!Settings.canDrawOverlays(this@MainActivity)) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Draw Overlay 권한 요청")
                .setMessage("앱을 사용하기 위해서는 Draw Overlay 권한이 필요합니다.")
                .setNegativeButton("거부", null)
                .setPositiveButton("허용") { _, _ ->
                    val uri = Uri.parse("package:$packageName")
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                    startActivityResultLauncher.launch(intent)
                }
                .create().show()
        } else {
            startLockScreenService()
        }
    }

    private fun startLockScreenService() {
        Intent(applicationContext, LockScreenService::class.java).apply {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(this)
            } else {
                startService(this)
            }
        }
    }
}