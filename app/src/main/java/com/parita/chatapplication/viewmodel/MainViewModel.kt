package com.parita.chatapplication.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.parita.chatapplication.databinding.ActivitySignInBinding
import com.parita.chatapplication.databinding.FragmentSignUpBinding
import com.parita.chatapplication.model.Feedback
import com.parita.chatapplication.model.FriendRequest
import com.parita.chatapplication.model.Friends
import com.parita.chatapplication.model.User
import com.parita.chatapplication.repository.Repository
import java.util.*

class MainViewModel : ViewModel() {
    private lateinit var loginStatus: MutableLiveData<Boolean>
    private lateinit var userAlreadyPresent: MutableLiveData<Boolean>
    private lateinit var userMutableLiveData: MutableLiveData<User>
    private lateinit var isImageDownloaded: MutableLiveData<ByteArray>
    private lateinit var isImageRemoved: MutableLiveData<Boolean>
    private lateinit var userMutableData: MutableLiveData<String>
    private lateinit var isImageUploaded: MutableLiveData<Boolean>
    private lateinit var noPreviousImgImageUploaded: MutableLiveData<Boolean>
    private lateinit var data: MutableLiveData<Boolean>
    private lateinit var isAccountDeactivated: MutableLiveData<Boolean>
    private lateinit var splashLoginStatus: MutableLiveData<Boolean>
    private lateinit var feedbackMutableLiveData: MutableLiveData<Boolean>
    private lateinit var dataList: MutableLiveData<ArrayList<User>>
    private lateinit var alreadyFriend: MutableLiveData<Boolean>
    private lateinit var requestAlreadyReceived: MutableLiveData<Boolean>
    private lateinit var requestFlag: MutableLiveData<String>
    private lateinit var sendRequestFlag:MutableLiveData<String>
    private lateinit var blockFlag: MutableLiveData<Boolean>
    private lateinit var friendRequestData:MutableLiveData<Boolean>
    private lateinit var unfriendFlag: MutableLiveData<Boolean>
    private lateinit var removeData: MutableLiveData<Boolean>
    private lateinit var dataLists: MutableLiveData<ArrayList<Friends>>
    private lateinit var blockFlags: MutableLiveData<Boolean>


    fun getLoginStatus(): LiveData<Boolean> {
        return loginStatus
    }

    fun getLoginStatus(email: String, password: String, context: Context) {
        loginStatus = MutableLiveData()
        loginStatus = Repository.fetchLoginStatus(email, password, context)
    }

    fun isOnline(context: Context): Boolean {
        val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = conMgr.activeNetworkInfo
        return if (netInfo == null || !netInfo.isConnected || !netInfo.isAvailable) {
            false
        } else true
    }
    fun getFeedbackCompleted(): LiveData<Boolean> {
        return feedbackMutableLiveData
    }

    fun initiateUploadFeedback(feedback: Feedback) {
        feedbackMutableLiveData = MutableLiveData()
        feedbackMutableLiveData = Repository().initiateFeedback(feedback)
    }
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }

    fun getSignupStatus(): LiveData<Boolean> {
        return userAlreadyPresent
    }

    fun initiateSignUp(email: String, password: String) {
        userAlreadyPresent = MutableLiveData()
        userAlreadyPresent = Repository.initateSignUp(email, password)
    }

    fun validateLoginDetails(
        email: String,
        password: String,
        binding: ActivitySignInBinding
    ): Boolean {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            if (isEmailValid(email) && isValidPassword(password)) {
                return true
            } else
                if (!isEmailValid(email)) {
                    binding.loginEmail.error = "Please enter a valid email address"
                    return false
                } else if (!isValidPassword(password)) {
                    binding.loginPassword.error = "Please enter a valid password"
                    return false
                } else return false
        } else {
            if (TextUtils.isEmpty(email)) {
                binding.loginEmail.error = "Please enter an email address. Field cannot be empty"
                return false
            } else if (TextUtils.isEmpty(password)) {
                binding.loginPassword.error = "Please enter a password. Field cannot be empty"
                return false
            } else
                return false
        }
    }

    fun validateDetails(
        email: String,
        password: String,
        cpassword: String,
        binding: FragmentSignUpBinding
    ): Boolean {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(
                cpassword
            )
        ) {
            if (isEmailValid(email) && isValidPassword(password) && password.equals(cpassword)) {
                return true
            } else
                if (!isEmailValid(email)) {
                    binding.email.error = "Please enter a valid email address"
                    return false
                } else if (!isValidPassword(password)) {
                    binding.password.error = "Please enter a valid password"
                    return false
                } else if (password != cpassword) {
                    binding.cpassword.error = "Confirm password does not match with password"
                    return false
                } else return false
        } else {
            if (TextUtils.isEmpty(email)) {
                binding.email.error = "Please enter an email address. Field cannot be empty"
                return false
            } else if (TextUtils.isEmpty(password)) {
                binding.password.error = "Please enter a password. Field cannot be empty"
                return false
            } else if (TextUtils.isEmpty(cpassword)) {
                binding.cpassword.error =
                    "Please enter a confirm matching password. Field cannot be empty"
                return false
            } else
                return false
        }
    }

    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String?): Boolean {
        password?.let {
            val passwordPattern =
                "^(?=.*[0-6])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
            val passwordMatcher = Regex(passwordPattern)

            return passwordMatcher.find(password) != null
        } ?: return false
    }

    fun fetchUserDetails(context: Context) {
        userMutableLiveData = MutableLiveData()
        userMutableLiveData = Repository().getUserProfileData(context)
    }

    fun getUserData(): LiveData<User> {
        return userMutableLiveData
    }

    fun initiateDownloadImage(userEmail: String, imagePath: String) {
        isImageDownloaded = MutableLiveData()
        isImageDownloaded = Repository().getDownloadImage(userEmail, imagePath)
    }

    fun getDownloadedImage(): LiveData<ByteArray> {
        return isImageDownloaded
    }

    fun removeProfileImage(email: String, imagePath: String) {
        isImageRemoved = MutableLiveData()
        isImageRemoved = Repository().removeImageProcess(email, imagePath)
    }

    fun getCompleteUpdateRemoveImage(): LiveData<Boolean> {
        return isImageRemoved
    }

    fun getPreviousImage(userEmail: String) {
        userMutableData = MutableLiveData()
        userMutableData = Repository().getUserPreviousImageDetails(userEmail)
    }

    fun getCompletePreviousImage(): LiveData<String> {
        return userMutableData
    }

    fun initiateImageUpload(pictureUri: Uri, userEmail: String, previousImagePath: String) {
        isImageUploaded = MutableLiveData()
        isImageUploaded = Repository()
            .uploadImageProcess(pictureUri, userEmail, previousImagePath)
    }

    fun getUpdateOnImageUpload(): LiveData<Boolean> {
        return isImageUploaded
    }

    fun initiateDeactivation(userEmail: String) {
        isAccountDeactivated = MutableLiveData()
        isAccountDeactivated = Repository().initiateDeactivationProcess(userEmail)
    }

    fun getUpdateCompleteDeactivate(): LiveData<Boolean> {
        return isAccountDeactivated
    }

    fun uploadImageInitiation(photoURI: Uri, email: String) {
        noPreviousImgImageUploaded = MutableLiveData()
        noPreviousImgImageUploaded = Repository().uploadFirstImage(photoURI, email)
    }

    fun getUpdateOnFirstImageUpload(): LiveData<Boolean> {
        return noPreviousImgImageUploaded
    }

    fun initiateLogoutUser(userEmail: String) {
        data = MutableLiveData()
        data = Repository().initiateLogout(userEmail)
    }

    fun getUpdateCompleteLogout(): LiveData<Boolean> {
        return data
    }

    fun getSplashLogin(email: String) {
        splashLoginStatus = MutableLiveData()
        splashLoginStatus = Repository().getLoginStatus(email)
    }

    fun getSplashLoginStatus(): LiveData<Boolean> {
        return splashLoginStatus
    }

    fun initiateSearch(search: String) {
        dataList = MutableLiveData<ArrayList<User>>()
        if (search != "") dataList = Repository().getSearchedUserList(search)
    }

    fun initiateFriendSearch(userEmail: String, friendEmail: String) {
        alreadyFriend = MutableLiveData<Boolean>()
        alreadyFriend = Repository().isAlreadyFriend(userEmail, friendEmail)
    }

    fun checkForAlreadyRequestReceived(userEmail: String, friendEmail: String) {
        requestAlreadyReceived = MutableLiveData<Boolean>()
        requestAlreadyReceived =
            Repository().getRequestAlreadyReceived(userEmail, friendEmail)
    }
    fun getSearchList(): LiveData<ArrayList<User>> {
        return dataList
    }

    fun getAlreadyFriend(): LiveData<Boolean> {
        return alreadyFriend
    }

    fun getAlreadyRequestReceived(): LiveData<Boolean> {
        return requestAlreadyReceived
    }
    fun getFriendshipStatus(userEmail: String, friendEmail: String) {
        requestFlag = MutableLiveData<String>()
        requestFlag =
            Repository().getFriendshipStatus(userEmail, friendEmail)
    }
    fun getUpdateRequest(): LiveData<String> {
        return requestFlag
    }

    fun requestFlagMethod(friendRequest: FriendRequest, email: String) {
        sendRequestFlag = MutableLiveData()
        sendRequestFlag = Repository().loadRequestData(friendRequest, email)
    }
    fun getUpdateRequestMethod(): LiveData<String> {
        return sendRequestFlag
    }

    fun initiateBlockUser(email: String, friendEmail: String) {
        blockFlag = MutableLiveData<Boolean>()
        blockFlag = Repository().initiateBlockUser(email, friendEmail)
    }
    fun userIsBlocked(): LiveData<Boolean> {
        return blockFlag
    }

    fun initiateUnBlockUser(userEmail: String, friendEmail: String) {
        blockFlag = MutableLiveData()
        blockFlag = Repository().initiateUnBlockUser(userEmail, friendEmail)
    }
    fun getUpdateCompleteSendRequest(): LiveData<Boolean> {
        return friendRequestData
    }
    fun insertFriendRequest(friendRequest: FriendRequest, userEmail: String) {
        friendRequestData =
            Repository().initiateFriendRequest(friendRequest, userEmail)

    }
    fun userIsUnfriended(): LiveData<Boolean>{
        return unfriendFlag
    }
    fun initateUnfriendProcess(userEmail: String, friendEmail: String) {
        unfriendFlag = MutableLiveData<Boolean>()
        unfriendFlag =
            Repository().initiateUnfriendProcess(userEmail, friendEmail)
    }
    fun getUpdateCompleteCancelRequest(): LiveData<Boolean> {
        return removeData
    }
    fun removeFriendRequest(friendRequest: FriendRequest, userEmail: String) {
        removeData = MutableLiveData()
        removeData = Repository().removeFriendRequest(friendRequest, userEmail)
    }

    fun initiateBlockList(email: String) {
        dataLists = MutableLiveData<ArrayList<Friends>>()
        dataLists = Repository().getBlockedFriendList(email)
    }
    fun getBlockedFriendDataList(): LiveData<ArrayList<Friends>> {
        return dataLists
    }
    fun userIsblocked(): LiveData<Boolean> {
        return blockFlags
    }

    fun initiateUnblock(userEmail: String, friendEmail: String) {
        blockFlags = MutableLiveData()
        blockFlags = Repository().initiateUnBlockUser(userEmail, friendEmail)
    }

}