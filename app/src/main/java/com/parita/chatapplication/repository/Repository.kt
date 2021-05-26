package com.parita.chatapplication.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.utils.SharedPreferenceHelper.isLogin
import com.parita.chatapplication.utils.SharedPreferenceHelper.password
import java.util.*

class Repository {
    companion object {
        private lateinit var loginStatus: MutableLiveData<Boolean>
        private lateinit var userAlreadyPresent: MutableLiveData<Boolean>
        private lateinit var defaultPrefs: SharedPreferences

        fun fetchLoginStatus(email: String, password: String, context: Context): MutableLiveData<Boolean> {
            loginStatus = MutableLiveData()
            checkLoginDetails(email, password, context)
            return loginStatus
        }

        private fun checkLoginDetails(email: String, password: String, context: Context) {
            defaultPrefs = SharedPreferenceHelper.defaultPreference(context)
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users/" + email.replace("@", "_").replace(".", "_"))
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("loginStatus") && dataSnapshot.hasChild("accountStatus")) {
                        if(password.equals(dataSnapshot.child("password").getValue(String::class.java))) {
                            loginStatus.value =
                                dataSnapshot.child("loginStatus").getValue(Boolean::class.java)
                            val loginStatus =
                                dataSnapshot.child("loginStatus").getValue(Boolean::class.java)!!
                            val accountStatus =
                                dataSnapshot.child("accountStatus").getValue(Boolean::class.java)!!
                            defaultPrefs.email = email
                            defaultPrefs.password = password
                            defaultPrefs.isLogin = true
                            updateUserList(email, accountStatus, loginStatus)
                        } else {
                            loginStatus.value=false
                        }
                    } else {
                        loginStatus.value=false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    loginStatus.value=false
                }
            })

        }

        fun initateSignUp(email: String, password: String): MutableLiveData<Boolean> {
            userAlreadyPresent = MutableLiveData()
            signUpUser(email, password)
            return userAlreadyPresent
        }
        fun signUpUser(email: String, password: String) {
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users/" + email.replace("@", "_").replace(".", "_"))
            val userId = "LetsChat" + databaseReference.push().key
            Log.d("TAG", "User ID:$userId")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userAlreadyPresent.value = true
                    } else {
                        val map: MutableMap<String, Any> = HashMap()
                        map["email"] = email
                        map["password"] = password
                        map["loginStatus"] = true
                        map["accountStatus"] = true
                        map["userId"] = userId
                        updateUserList(email, true, false)
                        databaseReference.updateChildren(map).addOnCompleteListener {
                            userAlreadyPresent.value = false
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    userAlreadyPresent.value = false
                }
            })
        }

        private fun updateUserList(email: String, accountStatus: Boolean, loginStatus: Boolean) {
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference("UserList/" + email.replace("@", "_").replace(".", "_"))
            val map: MutableMap<String, Any> = HashMap()
            map["email"] = email
            map["loginStatus"] = loginStatus
            map["accountStatus"] = accountStatus
            databaseReference.updateChildren(map)
        }
    }
}