package com.rnnativelab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun getMainComponentName(): String = "RNNativeLab"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")

        handleNativeDebugIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(TAG, "onNewIntent called")

        setIntent(intent)
        handleNativeDebugIntent(intent)
    }

    private fun handleNativeDebugIntent(intent: Intent?) {
        if (intent == null) return

        val shouldOpenNativeDebug = intent.getBooleanExtra("openNativeDebug", false)

        if (shouldOpenNativeDebug) {
            val orderId = intent.getStringExtra("orderId") ?: "ORD-DEFAULT"
            val amount = intent.getStringExtra("amount") ?: "0.00"

            NativeActivityLauncher.openDebugScreen(
                context = this,
                orderId = orderId,
                source = "MainActivity",
                amount = amount
            )
        }
    }

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}