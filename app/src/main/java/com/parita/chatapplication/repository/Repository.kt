package com.parita.chatapplication.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class Repository {
    companion object {
        private lateinit var loginStatus: MutableLiveData<Boolean>
        private lateinit var userAlreadyPresent: MutableLiveData<Boolean>

        fun fetchLoginStatus(email: String?): MutableLiveData<Boolean> {
            return loginStatus
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
                        map["loginStatus"] = false
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