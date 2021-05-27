package com.parita.chatapplication.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.parita.chatapplication.User
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.utils.SharedPreferenceHelper.isLogin
import com.parita.chatapplication.utils.SharedPreferenceHelper.password
import java.text.SimpleDateFormat
import java.util.*

class Repository {

    companion object {
        private lateinit var loginStatus: MutableLiveData<Boolean>
        private lateinit var userAlreadyPresent: MutableLiveData<Boolean>
        private lateinit var defaultPrefs: SharedPreferences
        private lateinit var userMutableLiveData: MutableLiveData<User>
        private lateinit var isImageDownloaded: MutableLiveData<ByteArray>
        private var userData: User = User()
        private lateinit var removeImage: MutableLiveData<Boolean>
        private lateinit var userImagePath: MutableLiveData<String>
        private lateinit var uploadImage: MutableLiveData<Boolean>
        private lateinit var noPreviousImgImageUploaded: MutableLiveData<Boolean>
        private lateinit var updateStatus: MutableLiveData<Boolean>
        private lateinit var updateDeactivationStatus: MutableLiveData<Boolean>
        private lateinit var splashLoginStatus: MutableLiveData<Boolean>


        fun fetchLoginStatus(
            email: String,
            password: String,
            context: Context
        ): MutableLiveData<Boolean> {
            loginStatus = MutableLiveData()
            checkLoginDetails(email, password, context)
            return loginStatus
        }

        private fun checkLoginDetails(email: String, password: String, context: Context) {
            defaultPrefs = SharedPreferenceHelper.defaultPreference(context)
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users/" + email.replace("@", "_").replace(".", "_"))
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("loginStatus") && dataSnapshot.hasChild(
                            "accountStatus"
                        )
                    ) {
                        if (password.equals(dataSnapshot.child("password").getValue(String::class.java))) {
                            val loginStatusDB = dataSnapshot.child("loginStatus").getValue(Boolean::class.java)!!
                            if (loginStatusDB == false) {
                                loginStatus.value = true
                                val accountStatus = dataSnapshot.child("accountStatus")
                                    .getValue(Boolean::class.java)!!
                                defaultPrefs.email = email
                                defaultPrefs.password = password
                                defaultPrefs.isLogin = true
                                updateUserList(email, accountStatus, true)
                            } else {
                                loginStatus.value = false
                                val accountStatus = dataSnapshot.child("accountStatus")
                                    .getValue(Boolean::class.java)!!
                                defaultPrefs.email = email
                                defaultPrefs.password = password
                                defaultPrefs.isLogin = true
                                updateUserList(email, accountStatus, false)
                            }
                        } else {
                            loginStatus.value = false
                        }
                    } else {
                        loginStatus.value = false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    loginStatus.value = false
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

    fun getLoginStatus(email: String): MutableLiveData<Boolean> {
        splashLoginStatus = MutableLiveData()
        fetchSplashLoginStatus(email)
        return splashLoginStatus
    }

    private fun fetchSplashLoginStatus(email: String) {
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("Users/" + email.replace("@", "_").replace(".", "_"))
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("loginStatus") && dataSnapshot.hasChild(
                        "accountStatus"
                    )
                ) {
                    splashLoginStatus.value =
                        dataSnapshot.child("loginStatus").getValue(Boolean::class.java)
                    val loginStatus = dataSnapshot.child("loginStatus").getValue(
                        Boolean::class.java
                    )!!
                    val accountStatus = dataSnapshot.child("accountStatus").getValue(
                        Boolean::class.java
                    )!!
                    updateUserList(email, accountStatus, loginStatus)
                } else {
                    splashLoginStatus.setValue(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun getUserProfileData(context: Context): MutableLiveData<User> {
        userMutableLiveData = MutableLiveData()
        fetchUserDataFromDb(context)
        return userMutableLiveData
    }

    private fun fetchUserDataFromDb(context: Context) {
        defaultPrefs = SharedPreferenceHelper.defaultPreference(context)
        val path = "Users/" + defaultPrefs.email?.replace("@", "_")?.replace(".", "_")
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userData = dataSnapshot.getValue(User::class.java)!!
                userMutableLiveData.value = userData
            }

            override fun onCancelled(databaseError: DatabaseError) {
                userMutableLiveData.value = userData
            }
        })
    }

    fun getDownloadImage(userEmail: String, imagePath: String): MutableLiveData<ByteArray> {
        isImageDownloaded = MutableLiveData<ByteArray>()
        downloadImageFromFirebase(userEmail, imagePath)
        return isImageDownloaded
    }

    private fun downloadImageFromFirebase(email: String, imagePath: String) {
        Log.d(
            "TAG",
            "Image Path details: " + imagePath.replace(extactPathEmail(email), "")
        )
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference
        val islandRef = storageReference.child(
            "images/" + imagePath.replace(extactPathEmail(email), "")
        )
        val ONE_MEGABYTE = (1024 * 1024).toLong()
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            isImageDownloaded.value = bytes
        }.addOnFailureListener { exception -> Log.d("TAG", "Exception: $exception") }

    }

    fun extactPathEmail(email: String): String {
        return email.replace("@", "_").replace(".", "_")
    }

    fun removeImageProcess(email: String, imagePath: String): MutableLiveData<Boolean> {
        removeImage = MutableLiveData<Boolean>()
        removeImageFromDb(email, imagePath)
        return removeImage
    }

    private fun removeImageFromDb(userEmail: String, imagePath: String) {
        Log.d(
            "TAG",
            "User details: " + extactPathEmail(userEmail)
                .toString() + "\t" + imagePath
        )
        val path = "Users/" + extactPathEmail(userEmail)
        if (imagePath != null) { //TODO if previous image is present then deleting the data from storage
            val previousImageId: String =
                imagePath.replace(extactPathEmail(userEmail), "")
            Log.d("TAG", "Previous image path: $previousImageId")
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val desertRef = storageRef.child("images/$previousImageId")
            val removeImagePathReference = FirebaseDatabase.getInstance().getReference(path)
            removeImagePathReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("profileImagePath").exists()) {
                        removeImagePathReference.child("profileImagePath").removeValue()
                    } else {
                        Log.d("TAG", "Error occured")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
            desertRef.delete().addOnSuccessListener {
                Log.d("TAG", "Previous image is deleted")
                removeImage.setValue(true)
            }.addOnFailureListener { e ->
                Log.d("TAG", "Exception in deletion of previous image: $e")
                removeImage.setValue(false)
            }
        } else {
            Log.d("TAG", "No previous history of image is present")
        }
    }

    fun getUserPreviousImageDetails(userEmail: String): MutableLiveData<String> {
        userImagePath = MutableLiveData<String>()
        fetchProfileImagePathFromDb(userEmail)
        return userImagePath
    }

    private fun fetchProfileImagePathFromDb(userEmail: String) {
        val path = "Users/" + extactPathEmail(userEmail)
        val fetchProfileImage = FirebaseDatabase.getInstance().getReference(path)
        fetchProfileImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val image = dataSnapshot.child("profileImagePath").getValue(
                        String::class.java
                    )
                    userImagePath.setValue(image)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun initiateDeactivationProcess(email: String): MutableLiveData<Boolean> {
        updateDeactivationStatus = MutableLiveData<Boolean>()
        deactivateAccount(email)
        return updateDeactivationStatus
    }

    private fun deactivateAccount(email: String) {
        val path = "Users/" + extactPathEmail(email)
        val deactivateReference = FirebaseDatabase.getInstance().getReference(path)
        deactivateReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    deactivateReference.child("accountStatus").setValue(false)
                    deactivateReference.child("loginStatus").setValue(false)
                    deactivateReference.child("activityStatus").setValue("Account Deactivated")
                    updateUserList(email!!, false, false)
                    updateDeactivationStatus.setValue(true)
                } else {
                    updateDeactivationStatus.setValue(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

    }

    fun uploadImageProcess(
        pictureUri: Uri,
        userEmail: String,
        previousImagePath: String
    ): MutableLiveData<Boolean> {
        uploadImage = MutableLiveData<Boolean>()
        uploadImageToFirebase(pictureUri, userEmail, previousImagePath)
        return uploadImage
    }

    private fun uploadImageToFirebase(
        pictureUri: Uri,
        userEmail: String,
        previousImagePath: String
    ) {
        val imageId: String =
            extactPathEmail(userEmail) + pictureUri.lastPathSegment
        val path = "Users/" + extactPathEmail(userEmail)
        Log.d("TAG", "ImageId and path: $imageId\t$path")
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference
        val uploadImageReference = storageReference.child("images/" + pictureUri.lastPathSegment)
        if (previousImagePath != null) { //TODO if previous image is present then deleting the data from storage
            val previousImageId: String =
                previousImagePath.replace(extactPathEmail(userEmail), "")
            Log.d("TAG", "Previous image path: $previousImageId")
            val storageRef = storage.reference
            val desertRef = storageRef.child("images/$previousImageId")
            desertRef.delete().addOnSuccessListener {
                Log.d(
                    "TAG",
                    "Previous image is deleted"
                )
            }.addOnFailureListener { e ->
                Log.d(
                    "TAG",
                    "Exception in deletion of previous image: $e"
                )
            }
        } else {
            Log.d("TAG", "No previous history of image is present")
        }
        //TODO uploading the image in storage
        val uploadTask = uploadImageReference.putFile(pictureUri)
        uploadTask.addOnFailureListener { e ->
            Log.d("TAG", "Exception: $e")
            uploadImage.setValue(false)
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("TAG", "Image details: " + taskSnapshot.metadata)
            uploadImage.setValue(true)
        }
        // TODO to keep track which user has uploaded which image, in User node under the particular email address, profileImagePath is added so that database and storage data can tally each other
        val uploadImagePathReference = FirebaseDatabase.getInstance().getReference(path)
        uploadImagePathReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    uploadImagePathReference.child("profileImagePath").setValue(imageId)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun initiateLogout(userEmail: String): MutableLiveData<Boolean> {
        updateStatus = MutableLiveData<Boolean>()
        userLogout(userEmail)
        return updateStatus
    }

    private fun userLogout(email: String) {
        val path = "Users/" + email.replace("@", "_").replace(".", "_")
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var user: User? = User()
                if (dataSnapshot.exists()) {
                    databaseReference.child("loginStatus").setValue(false)
                    user = dataSnapshot.getValue(User::class.java)
                    updateUserList(
                        email,
                        user!!.isAccountStatus,
                        false
                    )
                    updateStatus.setValue(true)
                } else {
                    updateStatus.setValue(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateActivityStatus(email: String, status: Boolean) {
        val text: String
        text = if (status) {
            "Online"
        } else {
            val simpleDateFormat = SimpleDateFormat("hh:mm a dd/MM/YYYY")
            val dt = simpleDateFormat.format(Calendar.getInstance().time)
            "Last seen at $dt"
        }
        val path = "Users/" + extactPathEmail(email)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.child("activityStatus").setValue(text)
    }

    fun uploadFirstImage(photoURI: Uri, userEmail: String): MutableLiveData<Boolean> {
        noPreviousImgImageUploaded = MutableLiveData()
        uploadFirstImageToFirebase(photoURI, userEmail)
        return noPreviousImgImageUploaded
    }

    private fun uploadFirstImageToFirebase(photoURI: Uri, userEmail: String) {
        val imageId: String =
            extactPathEmail(userEmail) + photoURI.lastPathSegment
        val path = "Users/" + extactPathEmail(userEmail)
        Log.d("TAG", "ImageId and path: $imageId\t$path")
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference
        val uploadImageReference = storageReference.child("images/" + photoURI.lastPathSegment)
        val uploadTask = uploadImageReference.putFile(photoURI)
        uploadTask.addOnFailureListener { e ->
            Log.d("TAG", "Exception: $e")
            noPreviousImgImageUploaded.value = false
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("TAG", "Image details: " + taskSnapshot.metadata)
            noPreviousImgImageUploaded.value = true
        }
        // TODO to keep track which user has uploaded which image, in User node under the particular email address, profileImagePath is added so that database and storage data can tally each other
        val uploadImagePathReference = FirebaseDatabase.getInstance().getReference(path)
        uploadImagePathReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    uploadImagePathReference.child("profileImagePath").setValue(imageId)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

}