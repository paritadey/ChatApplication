package com.parita.chatapplication.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.parita.chatapplication.R

object SharedPreferenceHelper {
    private lateinit var sharedPreferences: SharedPreferences
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_LOGIN = "isLogin"
    fun defaultPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String) {
        sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.email
        get() = getString(KEY_EMAIL, "no_data")
        set(value) {
            editMe {
                it.putString(KEY_EMAIL, value)
            }
        }

    var SharedPreferences.password
        get() = getString(KEY_PASSWORD, "no_data")
        set(value) {
            editMe {
                it.putString(KEY_PASSWORD, value)
            }
        }

    var SharedPreferences.isLogin
        get() = getBoolean(KEY_LOGIN, false)
        set(value) {
            editMe {
                it.putBoolean(KEY_LOGIN, value)
            }
        }
    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}