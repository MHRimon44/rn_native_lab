package com.rnnativelab

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class NativeSecureStorageModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val STORAGE_NAME = "rn_native_lab_secure_storage"
    }

    override fun getName(): String {
        return "NativeSecureStorageModule"
    }

    private fun getSecurePreferences() =
        EncryptedSharedPreferences.create(
            reactContext,
            STORAGE_NAME,
            MasterKey.Builder(reactContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    @ReactMethod
    fun saveValue(key: String, value: String, promise: Promise) {
        try {
            if (key.isBlank()) {
                promise.reject(
                    "INVALID_KEY",
                    "Storage key cannot be blank"
                )
                return
            }

            getSecurePreferences()
                .edit()
                .putString(key, value)
                .apply()

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "SAVE_SECURE_VALUE_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun getValue(key: String, promise: Promise) {
        try {
            if (key.isBlank()) {
                promise.reject(
                    "INVALID_KEY",
                    "Storage key cannot be blank"
                )
                return
            }

            val value = getSecurePreferences().getString(key, null)

            promise.resolve(value)
        } catch (error: Exception) {
            promise.reject(
                "GET_SECURE_VALUE_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun deleteValue(key: String, promise: Promise) {
        try {
            if (key.isBlank()) {
                promise.reject(
                    "INVALID_KEY",
                    "Storage key cannot be blank"
                )
                return
            }

            getSecurePreferences()
                .edit()
                .remove(key)
                .apply()

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "DELETE_SECURE_VALUE_ERROR",
                error.message,
                error
            )
        }
    }

    @ReactMethod
    fun clearAll(promise: Promise) {
        try {
            getSecurePreferences()
                .edit()
                .clear()
                .apply()

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "CLEAR_SECURE_STORAGE_ERROR",
                error.message,
                error
            )
        }
    }
}