package com.wantique.lockscreen

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.wantique.lockscreen.databinding.ActivityLockScreenBinding

class LockScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockScreenBinding
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lock_screen)

        setupForLockScreen()
        disableBackPress()

        val onRightDragListener = {
            Toast.makeText(this@LockScreenActivity, "drag to right", Toast.LENGTH_SHORT).show()
            finish()
        }

        val onLeftDragListener =  {
            Toast.makeText(this@LockScreenActivity, "drag to left", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.dndView.setOnDragListener(onRightDragListener, onLeftDragListener)
    }

    private fun setupForLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this@LockScreenActivity, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

         */
    }

    private fun disableBackPress() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}