package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.utils.AppEx.toLanguageModel
import com.google.gson.Gson

class SpManager(private val preferences: SharedPreferences) {
    companion object {
        private var instance: SpManager? = null

        fun getInstance(context: Context): SpManager {
            if (instance == null) {
                instance =
                    SpManager(context.getSharedPreferences("transparent", Context.MODE_PRIVATE))
            }
            return instance!!
        }
    }

    fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)

    fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)


    fun getLong(key: String, defaultValue: Long): Long =
        preferences.getLong(key, defaultValue)

    fun putLong(key: String, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String? =
        preferences.getString(key, defaultValue)

    fun putString(key: String, value: String) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun saveLanguage(languageModel: LanguageModel) {
        preferences.edit().putString(Constant.KEY_SP_CURRENT_LANGUAGE, Gson().toJson(languageModel))
            .apply()
    }

    fun getLanguage(): LanguageModel {
        return runCatching {
            preferences.getString(Constant.KEY_SP_CURRENT_LANGUAGE, "")?.toLanguageModel()
        }.getOrNull() ?: LanguageModel("en", R.drawable.ic_lang_english, R.string.txt_english)
    }

    fun setUMPShowed(showed: Boolean) {
        preferences.edit().putBoolean(Constant.KEY_SP_UMP_SHOWED, showed).apply()
    }

    fun isUMPShowed(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_UMP_SHOWED, false)
    }

    fun setSettingUMPShowing(b: Boolean) {
        preferences.edit().putBoolean(Constant.KEY_SP_UMP_SETTING_SHOWED, b).apply()
    }

    fun isSettingUMPShowing(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_UMP_SETTING_SHOWED, false)
    }


    fun saveFirstOpenApp() {
        preferences.edit().putBoolean(Constant.KEY_SP_SHOW_ONBOARDING, false).apply()
    }

    fun getFirstOpenApp(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_SHOW_ONBOARDING, true)
    }

}