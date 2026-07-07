package com.rnnativelab

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap

class NativeSecureStorageModule(
    private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val STORAGE_NAME = "rn_native_lab_secure_storage"
        private const val TOKEN_KEY = "access_token"
        private const val SOURCE = "Android EncryptedSharedPreferences"
        private const val MAX_KEY_LENGTH = 100
        private const val MAX_VALUE_LENGTH = 10000
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

    private fun validateKey(key: String): String? {
        val normalizedKey = key.trim()

        if (normalizedKey.isBlank()) {
            return "Storage key cannot be blank"
        }

        if (normalizedKey.length > MAX_KEY_LENGTH) {
            return "Storage key cannot be longer than $MAX_KEY_LENGTH characters"
        }

        if (!normalizedKey.matches(Regex("^[A-Za-z0-9._-]+$"))) {
            return "Storage key can only contain letters, numbers, dot, underscore, and hyphen"
        }

        return null
    }

    private fun validateValue(value: String): String? {
        if (value.isBlank()) {
            return "Storage value cannot be blank"
        }

        if (value.length > MAX_VALUE_LENGTH) {
            return "Storage value cannot be longer than $MAX_VALUE_LENGTH characters"
        }

        return null
    }

    private fun createResultMap(
        success: Boolean,
        key: String?,
        message: String
    ): WritableMap {
        return Arguments.createMap().apply {
            putBoolean("success", success)
            if (key != null) {
                putString("key", key)
            }
            putString("message", message)
            putString("source", SOURCE)
        }
    }

    @ReactMethod
    fun saveValue(key: String, value: String, promise: Promise) {
        try {
            val keyError = validateKey(key)
            if (keyError != null) {
                promise.reject("INVALID_KEY", keyError)
                return
            }

            val valueError = validateValue(value)
            if (valueError != null) {
                promise.reject("INVALID_VALUE", valueError)
                return
            }

            getSecurePreferences()
                .edit()
                .putString(key.trim(), value)
                .apply()

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "SAVE_SECURE_VALUE_ERROR",
                error.message ?: "Failed to save secure value",
                error
            )
        }
    }

    @ReactMethod
    fun getValue(key: String, promise: Promise) {
        try {
            val keyError = validateKey(key)
            if (keyError != null) {
                promise.reject("INVALID_KEY", keyError)
                return
            }

            val value = getSecurePreferences().getString(key.trim(), null)

            promise.resolve(value)
        } catch (error: Exception) {
            promise.reject(
                "GET_SECURE_VALUE_ERROR",
                error.message ?: "Failed to read secure value",
                error
            )
        }
    }

    @ReactMethod
    fun deleteValue(key: String, promise: Promise) {
        try {
            val keyError = validateKey(key)
            if (keyError != null) {
                promise.reject("INVALID_KEY", keyError)
                return
            }

            getSecurePreferences()
                .edit()
                .remove(key.trim())
                .apply()

            promise.resolve(true)
        } catch (error: Exception) {
            promise.reject(
                "DELETE_SECURE_VALUE_ERROR",
                error.message ?: "Failed to delete secure value",
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
                error.message ?: "Failed to clear secure storage",
                error
            )
        }
    }

    @ReactMethod
    fun saveToken(token: String, promise: Promise) {
        try {
            val valueError = validateValue(token)
            if (valueError != null) {
                promise.reject("INVALID_TOKEN", valueError)
                return
            }

            getSecurePreferences()
                .edit()
                .putString(TOKEN_KEY, token)
                .apply()

            promise.resolve(
                createResultMap(
                    success = true,
                    key = TOKEN_KEY,
                    message = "Token saved securely"
                )
            )
        } catch (error: Exception) {
            promise.reject(
                "SAVE_TOKEN_ERROR",
                error.message ?: "Failed to save token",
                error
            )
        }
    }

    @ReactMethod
    fun getToken(promise: Promise) {
        try {
            val token = getSecurePreferences().getString(TOKEN_KEY, null)
            promise.resolve(token)
        } catch (error: Exception) {
            promise.reject(
                "GET_TOKEN_ERROR",
                error.message ?: "Failed to read token",
                error
            )
        }
    }

    @ReactMethod
    fun deleteToken(promise: Promise) {
        try {
            getSecurePreferences()
                .edit()
                .remove(TOKEN_KEY)
                .apply()

            promise.resolve(
                createResultMap(
                    success = true,
                    key = TOKEN_KEY,
                    message = "Token deleted securely"
                )
            )
        } catch (error: Exception) {
            promise.reject(
                "DELETE_TOKEN_ERROR",
                error.message ?: "Failed to delete token",
                error
            )
        }
    }

    @ReactMethod
    fun hasValue(key: String, promise: Promise) {
        try {
            val keyError = validateKey(key)
            if (keyError != null) {
                promise.reject("INVALID_KEY", keyError)
                return
            }

            val exists = getSecurePreferences().contains(key.trim())
            promise.resolve(exists)
        } catch (error: Exception) {
            promise.reject(
                "HAS_VALUE_ERROR",
                error.message ?: "Failed to check secure value",
                error
            )
        }
    }
}