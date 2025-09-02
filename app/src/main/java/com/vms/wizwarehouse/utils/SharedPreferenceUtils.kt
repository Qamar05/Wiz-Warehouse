package com.vms.wizwarehouse.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Prepared By Qamar Abbas
 * Dated: 26-09-2022
 */
object SharedPreferenceUtils {

    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val CHECK_IN_ID = "checkInId"
    const val ACCESS_TOKEN = "access_token"
    const val KEY_ACTIVITY_ID = "activity_id"
    const val CITY_ID = "city_id"
    const val SURVEY_ID = "survey_id"
    const val IS_LOGGED_IN_FWP = "is_logged_in_fwp"
    const val IS_CHECKED_IN_FWP = "is_checked_in_fwp"
    const val IS_LOGGED_IN_WAREHOUSE = "is_logged_in_warehouse"
    const val IS_CHECKED_IN_WAREHOUSE = "is_checked_in_warehouse"
    const val USER_NAME = "user_name"
    const val USER_EMAIL = "user_email"
    const val USER_CODE = "user_code"
    const val USER_ROLE = "user_role"
    const val IS_SUPERVISOR = "is_supervisor"
    const val PHONE_NUMBER = "phone"
    const val SELECTED_MODE = "selected_mode"
    const val BRAND_ID = "brand_id"
    const val VARIANT_ID = "variant_id"

    private const val PREFERENCE_NAME = "com.vms.wizactivity"

    private fun getSecureSharedPreferences(context: Context): SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFERENCE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            Log.e("SHARED_PREFERENCES", "Security error: $e")
            null
        } catch (e: IOException) {
            Log.e("SHARED_PREFERENCES", "IO error: $e")
            null
        }
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getSecureSharedPreferences(context)?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(context: Context, key: String): Boolean {
        return getSecureSharedPreferences(context)?.getBoolean(key, false) ?: false
    }

    fun saveInt(context: Context, key: String, value: Int) {
        getSecureSharedPreferences(context)?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(context: Context, key: String): Int {
        return getSecureSharedPreferences(context)?.getInt(key, 0) ?: 0
    }

    fun saveFloat(context: Context, key: String, value: Float) {
        getSecureSharedPreferences(context)?.edit()?.putFloat(key, value)?.apply()
    }

    fun getFloat(context: Context, key: String): Float {
        return getSecureSharedPreferences(context)?.getFloat(key, 0.0f) ?: 0.0f
    }

    fun saveLong(context: Context, key: String, value: Long) {
        getSecureSharedPreferences(context)?.edit()?.putLong(key, value)?.apply()
    }

    fun getLong(context: Context, key: String): Long {
        return getSecureSharedPreferences(context)?.getLong(key, 0L) ?: 0L
    }

    fun saveString(context: Context, key: String, value: String?) {
        getSecureSharedPreferences(context)?.edit()?.putString(key, value)?.apply()
    }

    fun getString(context: Context, key: String): String? {
        return getSecureSharedPreferences(context)?.getString(key, null)
    }
}
