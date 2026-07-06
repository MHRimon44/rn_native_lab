package com.rnnativelab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log

object NativeActivityLauncher {

    private const val TAG = "NativeActivityLauncher"

    fun openDebugScreen(
        context: Context,
        orderId: String,
        source: String,
        amount: String
    ) {
        Log.d(TAG, "Opening NativeDebugActivity")
        Log.d(TAG, "orderId=$orderId, source=$source, amount=$amount")

        val intent = Intent(context, NativeDebugActivity::class.java).apply {
            putExtra("orderId", orderId)
            putExtra("source", source)
            putExtra("amount", amount)
        }

        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}