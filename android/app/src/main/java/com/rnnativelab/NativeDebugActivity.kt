package com.rnnativelab

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NativeDebugActivity : Activity() {

    companion object {
        private const val TAG = "NativeDebugActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        
        val amount = intent.getStringExtra("amount") ?: "0.00"
        Log.d(TAG, "Received amount: $amount")

        val orderId = intent.getStringExtra("orderId") ?: "No Order ID"
        val source = intent.getStringExtra("source") ?: "Unknown Source"

        val titleText = TextView(this).apply {
            text = "Native Android Screen"
            textSize = 24f
            setPadding(30, 40, 30, 20)
        }

        val orderText = TextView(this).apply {
            text = "Order ID: $orderId"
            textSize = 18f
            setPadding(30, 20, 30, 20)
        }

        val sourceText = TextView(this).apply {
            text = "Source: $source"
            textSize = 18f
            setPadding(30, 20, 30, 20)
        }

        val closeButton = Button(this).apply {
            text = "Close Native Screen"
            setOnClickListener {
                Log.d(TAG, "Close button clicked")
                finish()
            }
        }
        val amountText = TextView(this).apply {
            text = "Amount: $amount"
            textSize = 18f
            setPadding(30, 20, 30, 20)
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleText)
            addView(orderText)
            addView(sourceText)
            addView(amountText)
            addView(closeButton)
        }

        setContentView(layout)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }
}