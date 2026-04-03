package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.utils.AppEx.toLanguageModel
import com.google.gson.Gson
import javax.inject.Inject

class SpManager @Inject constructor(private val preferences: SharedPreferences) {
    companion object {
        private var instance: SpManager? = null

        fun getInstance(context: Context): SpManager {
            return instance ?: synchronized(this) {
                instance ?: SpManager(
                    context.applicationContext.getSharedPreferences("transparent", Context.MODE_PRIVATE)
                ).also { instance = it }
            }
        }
    }

    fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)

    fun putInt(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    fun getLong(key: String, defaultValue: Long): Long =
        preferences.getLong(key, defaultValue)

    fun putLong(key: String, value: Long) {
        preferences.edit {
            putLong(key, value)
        }
    }

    fun getString(key: String, defaultValue: String): String? =
        preferences.getString(key, defaultValue)

    fun putString(key: String, value: String) {
        preferences.edit {
            putString(key, value)
        }
    }

    fun saveLanguage(languageModel: LanguageModel) {
        preferences.edit {
            putString(Constant.KEY_SP_CURRENT_LANGUAGE, Gson().toJson(languageModel))
        }
    }

    fun getLanguage(): LanguageModel {
        val defaultLanguage = LanguageModel("en", R.drawable.ic_lang_english, R.string.txt_english)

        val json = preferences.getString(Constant.KEY_SP_CURRENT_LANGUAGE, null)

        if (json.isNullOrEmpty()) {
            return defaultLanguage
        }

        return try {
            json.toLanguageModel() ?: defaultLanguage
        } catch (e: Exception) {
            defaultLanguage
        }
    }

    fun setUMPShowed(showed: Boolean) {
        preferences.edit { putBoolean(Constant.KEY_SP_UMP_SHOWED, showed) }
    }

    fun isUMPShowed(): Boolean {
        return this.preferences.getBoolean(Constant.KEY_SP_UMP_SHOWED, false)
    }

    fun setSettingUMPShowing(b: Boolean) {
        preferences.edit { putBoolean(Constant.KEY_SP_UMP_SETTING_SHOWED, b) }
    }

    fun isSettingUMPShowing(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_UMP_SETTING_SHOWED, false)
    }

    fun saveFirstOpenApp() {
        preferences.edit { putBoolean(Constant.KEY_SP_SHOW_ONBOARDING, false) }
    }

    fun getFirstOpenApp(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_SHOW_ONBOARDING, true)
    }

    fun <T> saveObject(key: String, value: T) {
        preferences.edit {
            putString(key, Gson().toJson(value))
        }
    }

    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val json = preferences.getString(key, null)
        return if (json.isNullOrEmpty()) {
            null
        } else {
            try {
                Gson().fromJson(json, clazz)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun isPro(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_IS_PRO, false)
    }

    fun setPro(isPro: Boolean) {
        preferences.edit { putBoolean(Constant.KEY_SP_IS_PRO, isPro) }
    }
}