package com.parita.chatapplication.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.parita.chatapplication.model.*
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
        private lateinit var feedbackMutableLiveData: MutableLiveData<Boolean>
        private lateinit var userList: ArrayList<User>
        private lateinit var dataList: MutableLiveData<ArrayList<User>>
        private lateinit var alreadyFriends: MutableLiveData<Boolean>
        private lateinit var requestAlreadyReceived: MutableLiveData<Boolean>
        private lateinit var requestFlag: MutableLiveData<String>
        private lateinit var sendRequestFlag: MutableLiveData<String>
        private lateinit var blockFlag: MutableLiveData<Boolean>
        private lateinit var friendRequestData: MutableLiveData<Boolean>
        private lateinit var unfriendFlag: MutableLiveData<Boolean>
        private lateinit var updateRemoveStatus: MutableLiveData<Boolean>
        private lateinit var blockList: ArrayList<Friends>
        private lateinit var dataLists: MutableLiveData<ArrayList<Friends>>


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
                        if (password.equals(
                                dataSnapshot.child("password").getValue(String::class.java)
                            )
                        ) {
                            val loginStatusDB =
                                dataSnapshot.child("loginStatus").getValue(Boolean::class.java)!!
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

        private fun fetchSearchedUserFromDb(search: String) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("UserList")
            databaseReference.orderByKey().limitToFirst(100)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    //TODO here we are trying to filter the data with respect to the key value and we are extracting first 100 records.
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (i in dataSnapshot.children) {
                            Log.d("TAG", "add contact repository: " + i.key)
                            if (i.key!!.contains(search)) {
                                val user = i.getValue(User::class.java)
                                userList.add(user!!)
                            }
                        }
                        dataList.setValue(userList)
                        userList.clear()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
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

    fun initiateFeedback(feedback: Feedback): MutableLiveData<Boolean> {
        feedbackMutableLiveData = MutableLiveData()
        uploadFeedback(feedback)
        return feedbackMutableLiveData
    }

    private fun uploadFeedback(feedback: Feedback) {
        val feedbackReference = FirebaseDatabase.getInstance().getReference("FeedBack")
        val data: MutableMap<String, Any> = HashMap()
        data["email"] = feedback.email
        data["ratings"] = feedback.ratings
        data["message"] = feedback.message
        data["dateTime"] = feedback.dateTime
        feedbackReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                feedbackReference.child("Feedback_" + dataSnapshot.childrenCount.toString())
                    .updateChildren(data).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateStatus.value = true
                        } else {
                            updateStatus.value = false
                        }
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

    fun getSearchedUserList(searchWord: String): MutableLiveData<ArrayList<User>> {
        userList = ArrayList()
        dataList = MutableLiveData()
        fetchSearchedUserFromDb(searchWord)
        return dataList
    }

    private fun fetchFriendList(userEmail: String, friendEmail: String) {
        //TODO check for if a the user is already friends with the searched email, by looking into the
        // friend node in the user we check if the entry exist, if it exist then we return true else false.
        val path =
            "Users/" + extactPathEmail(
                userEmail
            ) + "/Friends/" + extactPathEmail(
                friendEmail
            )
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    alreadyFriends.setValue(true)
                } else {
                    alreadyFriends.setValue(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun isAlreadyFriend(userEmail: String, friendEmail: String): MutableLiveData<Boolean> {
        alreadyFriends = MutableLiveData()
        fetchFriendList(userEmail, friendEmail)
        return alreadyFriends
    }

    fun getRequestAlreadyReceived(
        userEmail: String,
        friendEmail: String
    ): MutableLiveData<Boolean> {
        requestAlreadyReceived = MutableLiveData()
        fetchAlreadyReceivedRequest(userEmail, friendEmail)
        return requestAlreadyReceived
    }

    private fun fetchAlreadyReceivedRequest(userEmail: String, friendEmail: String) {
        //TODO check for if a the user is already received a friend request from the searched email, by looking into the
        // FriendRequest/Received node in the user we check if the entry exist, if it exist then we return true else false.
        val path =
            "Users/" + extactPathEmail(
                userEmail
            ) + "/FriendRequest/Received/" + extactPathEmail(
                friendEmail
            )
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    requestAlreadyReceived.value = true
                } else {
                    requestAlreadyReceived.value = false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun getFriendshipStatus(userEmail: String, friendEmail: String): MutableLiveData<String> {
        requestFlag = MutableLiveData<String>()
        fetchFriendStatus(userEmail, friendEmail)
        return requestFlag
    }

    private fun fetchFriendStatus(userEmail: String, friendEmail: String) {
        //TODO As because the text of the button need to be changed in regards to the value of the flag
        // so here we find what is the status of the friend by reading the flag field in the inside friend node.
        val path = "Users/" + extactPathEmail(userEmail)
            .toString() + "/Friends/" + extactPathEmail(friendEmail)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("flag")) {
                    requestFlag.setValue(dataSnapshot.child("flag").getValue(String::class.java))
                } else {
                    requestFlag.setValue("no_data")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun loadRequestData(friendRequest: FriendRequest, email: String): MutableLiveData<String> {
        sendRequestFlag = MutableLiveData()
        loadData(friendRequest, email)
        return sendRequestFlag
    }

    private fun loadData(friendRequest: FriendRequest, userEmail: String) {
        //TODO this section will work when user search for a contact and go to contact details then this method loads whether Person A has sent friend request or not to Person B
        // if Person A sends friend request then fetch the flag value from the db. If the flag=neutral then it will show request sent, if the flag= accepted then it will show Accepted
        // and if the flag=rejected then it will show Send Request. If Person A has not sent any request to that particular email id of Person B then it will show no change
        Log.d("TAG", "User Email : $userEmail")
        val sentFriendEmail = userEmail.replace("@", "_").replace(".", "_") //TODO this is person A
        Log.d("TAG", "sent friend email :$sentFriendEmail")
        val sentRequestPath = "Users/$sentFriendEmail/FriendRequest"
        val receiveEmail: String = friendRequest.email
        val receiveRequestFriendEmail =
            receiveEmail.replace("@", "_").replace(".", "_") //TODO this is person B
        Log.d("TAG", "Receive friend email : $receiveRequestFriendEmail")
        val sentRequestReference = FirebaseDatabase.getInstance().getReference(sentRequestPath)
        sentRequestReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("TAG", "FriendRequest node exists")
                    if (dataSnapshot.child("Sent").exists()) {
                        Log.d("TAG", "FriendRequest/Sent node exists")
                        for (i in dataSnapshot.children) {
                            for (j in i.children) {
                                if (j.key == receiveRequestFriendEmail) {
                                    Log.d(
                                        "TAG",
                                        "friend request sent email is present: $receiveRequestFriendEmail"
                                    )
                                    for (k in j.children) {
                                        if (k.key == "flag") {
                                            Log.d("TAG", "Flag value: " + k.value)
                                            val flag = k.value as String?
                                            sendRequestFlag.value = flag
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.d("TAG", "No request has been sent : ")
                    sendRequestFlag.value = "no_data"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun initiateBlockUser(email: String, friendEmail: String): MutableLiveData<Boolean> {
        blockFlag = MutableLiveData<Boolean>()
        blockUser(email, friendEmail)
        return blockFlag
    }

    private fun blockUser(userEmail: String, friendEmail: String) {
        //TODO to block the user friend we go into the Friend node find the friend and set the flag value to block.

        //TODO to block the user friend we go into the Friend node find the friend and set the flag value to block.
        val path = "Users/" + extactPathEmail(userEmail)
            .toString() + "/Friends/" + extactPathEmail(friendEmail)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("flag") && dataSnapshot.child("flag")
                        .getValue(
                            String::class.java
                        ) == "friend"
                ) {
                    databaseReference.child("flag").setValue("block")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                blockFlag.setValue(true)
                            } else {
                                blockFlag.setValue(false)
                            }
                        }
                } else {
                    blockFlag.setValue(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun initiateUnBlockUser(userEmail: String, friendEmail: String): MutableLiveData<Boolean> {
        blockFlag = MutableLiveData()
        unBlockUser(userEmail, friendEmail)
        return blockFlag
    }

    private fun unBlockUser(userEmail: String, friendEmail: String) {
        //TODO to unblock the user friend we go into the Friend node find the friend and check if the user has "block" in the flag,
        // if block is found then we change the value of the flag to "friend".
        val path =
            "Users/" + extactPathEmail(userEmail) + "/Friends/" + extactPathEmail(friendEmail)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("flag") && dataSnapshot.child("flag")
                        .getValue(
                            String::class.java
                        ) == "block"
                ) {
                    databaseReference.child("flag").setValue("friend")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                blockFlag.setValue(false)
                            } else {
                                blockFlag.setValue(true)
                            }
                        }
                } else {
                    blockFlag.setValue(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun initiateFriendRequest(
        friendRequest: FriendRequest,
        userEmail: String
    ): MutableLiveData<Boolean> {
        friendRequestData = MutableLiveData()
        sendRequest(friendRequest, userEmail)
        return friendRequestData
    }

    private fun sendRequest(friendRequest: FriendRequest, userEmail: String) {
        //TODO this will create a node called FriendRequest under the sender's email node and this
        // FriendRequest has two sections, one is Sent and another is Received
        //TODO if a person A sends a friend request to another person B then B will get the request as FriendRequest/Received
        // and A will have the node structure like FriendRequest/Sent
        Log.d("TAG", "User Email : $userEmail")
        val sentFriendEmail = userEmail.replace("@", "_").replace(".", "_") //TODO this is person A
        Log.d("TAG", "sent friend email :$sentFriendEmail")
        val sentRequestPath = "Users/$sentFriendEmail/FriendRequest/Sent"
        val receiveEmail: String = friendRequest.email
        val receiveRequestFriendEmail =
            receiveEmail.replace("@", "_").replace(".", "_") //TODO this is person B
        Log.d("TAG", "Receive friend email : $receiveRequestFriendEmail")
        val receiveRequestPath =
            "Users/$receiveRequestFriendEmail/FriendRequest/Received"
        val sendNotificationPath =
            "Users/$receiveRequestFriendEmail/Notification" // TODO notification node will be created as soon as the Person A sends friend request to Person B
        val sendRequestDataReference = FirebaseDatabase.getInstance().getReference(sentRequestPath)
        val receiveRequestDataReference =
            FirebaseDatabase.getInstance().getReference(receiveRequestPath)
        val notificationDataReference =
            FirebaseDatabase.getInstance().getReference(sendNotificationPath)
        val notification = Notification()
        notification.emailFrom = userEmail
        notification.notificationMessage = "Pending Contact Request"
        notification.dateTime =
            friendRequest.date //TODO setting the date and time when Person A has sent friend Request to Person B
        notification.notificationType =
            3 //TODO if notificationType is 1 then it means message from another user, if notificationType is 2 then it means contact request acceptance, and if notificationType is 3 then it means contact request
        notification.isSeenFlag =
            false // TODO setting the seen flag : if the user has seen the notification then seenFlag = true and those will not be shown in app
        notificationDataReference.child(sentFriendEmail)
            .setValue(notification) //TODO this notification node will be deleted as soon as Person B views/gets notification from Person A
        val data: MutableMap<String, Any> = HashMap()
        data["email"] = userEmail
        data["date"] = friendRequest.date
        data["flag"] = friendRequest.flag
        sendRequestDataReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                sendRequestDataReference.child(receiveRequestFriendEmail).setValue(friendRequest)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        receiveRequestDataReference.child(sentFriendEmail)
            .updateChildren(
                data
            ) { databaseError, databaseReference ->
                if (databaseError == null) {
                    updateStatus.setValue(true)
                } else {
                    updateStatus.setValue(false)
                }
            }
    }

    fun initiateUnfriendProcess(userEmail: String, friendEmail: String): MutableLiveData<Boolean> {
        unfriendFlag = MutableLiveData()
        removeFriend(userEmail, friendEmail)
        return unfriendFlag
    }

    private fun removeFriend(userEmail: String, friendEmail: String) {
        //TODO to implement unfriend operation we remove the entries from both the user account and the
        // friend account from the friend node.
        removeFriendNotification(userEmail, friendEmail)
        val path = "Users/" + extactPathEmail(userEmail)
            .toString() + "/Friends/" + extactPathEmail(friendEmail)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                unfriendFlag.setValue(true)
            } else {
                unfriendFlag.setValue(false)
            }
        }
        val path1 =
            "Users/" + extactPathEmail(friendEmail) + "/Friends/" + extactPathEmail(userEmail)
        val databaseReference1 = FirebaseDatabase.getInstance().getReference(path1)
        databaseReference1.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                unfriendFlag.setValue(true)
            } else {
                unfriendFlag.setValue(false)
            }
        }
    }

    private fun removeFriendNotification(userEmail: String, friendEmail: String) {
        //TODO This is just a safety check if the user is already unfriend then we remove all and every
        // notification in both the accounts accordingly from the notification node.
        val path =
            "Users/" + extactPathEmail(userEmail) + "/Notification/" + extactPathEmail(friendEmail)
        val databaseReference = FirebaseDatabase.getInstance().getReference(path)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
            }
        }
        val path1 = "Users/" + extactPathEmail(friendEmail)
            .toString() + "/Notification/" + extactPathEmail(userEmail)
        val databaseReference1 = FirebaseDatabase.getInstance().getReference(path1)
        databaseReference1.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
            }
        }
    }

    fun removeFriendRequest(
        friendRequest: FriendRequest,
        userEmail: String
    ): MutableLiveData<Boolean> {
        updateRemoveStatus = MutableLiveData()
        removeRequest(friendRequest, userEmail)
        return updateRemoveStatus
    }

    private fun removeRequest(friendRequest: FriendRequest, userEmail: String) {
        //TODO remove the friend request i.e if Person A sends request to Person B and Person A wants to undo the friends request
        //TODO case 1 : this section will only cancel the friend request if Person B has not accepted the friend request, i.e flag = neutral
        //TODO case 2 : if Person B accepts the friend request of Person A and then Person A searches for the name of Person B, then on acceptance of friend request,
        // Person A will see three option :
        // option 1 : Send Request button will converted to  Accepted Request
        // option 2 : Cancel Request button will not be shown
        // option 3 : Go to chat button will be visible, so that Person A can redirect to the Chat of B and A
        // TODO case 3 : if Person B rejects the friend request of Person A, then Person A will see two options: option a -> Send Request and option b -> Cancel Request
        Log.d("TAG", "User Email : $userEmail")
        val sentFriendEmail = userEmail.replace("@", "_").replace(".", "_") //TODO this is person A
        Log.d("TAG", "sent friend email :$sentFriendEmail")
        val cancelSentRequestPath = "Users/$sentFriendEmail/FriendRequest/Sent"
        val receiveEmail: String = friendRequest.email
        val receiveRequestFriendEmail =
            receiveEmail.replace("@", "_").replace(".", "_") //TODO this is person B
        Log.d("TAG", "Receive friend email : $receiveRequestFriendEmail")
        val cancelReceiveRequestPath =
            "Users/$receiveRequestFriendEmail/FriendRequest/Received"
        val cancelSendNotificationPath =
            "Users/$receiveRequestFriendEmail/Notification" // TODO notification node will be created as soon as the Person A sends friend request to Person B
        val cancelSendRequestDataReference =
            FirebaseDatabase.getInstance().getReference(cancelSentRequestPath)
        val cancelReceiveRequestDataReference =
            FirebaseDatabase.getInstance().getReference(cancelReceiveRequestPath)
        val cancelNotificationDataReference =
            FirebaseDatabase.getInstance().getReference(cancelSendNotificationPath)
        //TODO removing all the nodes on cancelling the request only if Person A and Person B are not friends
        cancelSendRequestDataReference.child(receiveRequestFriendEmail).removeValue()
        cancelReceiveRequestDataReference.child(sentFriendEmail).removeValue()
        cancelNotificationDataReference.child(sentFriendEmail).removeValue()
        updateRemoveStatus.setValue(true)
    }

    fun getBlockedFriendList(email: String): MutableLiveData<ArrayList<Friends>> {
        blockList = ArrayList<Friends>()
        dataLists = MutableLiveData<ArrayList<Friends>>()
        fetchBlockFriendsFromDb(email)
        return dataLists
    }

    private fun fetchBlockFriendsFromDb(userEmail: String) {
        //TODO fetch block friend list by fetching the flag=block from database
        val friendsPath =
            "Users/" + extactPathEmail(userEmail) + "/Friends"
        val fetchBlockFriendReference = FirebaseDatabase.getInstance().getReference(friendsPath)
        fetchBlockFriendReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in dataSnapshot.children) {
                    if (i.child("flag").exists()) {
                        if (i.child("flag").value.toString().equals("block", ignoreCase = true)) {
                            val key = i.key
                            var obj: Friends? = Friends()
                            obj = dataSnapshot.child(key!!).getValue(Friends::class.java)
                            blockList.add(obj!!)
                        }
                    }
                }
                dataLists.value = blockList
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}