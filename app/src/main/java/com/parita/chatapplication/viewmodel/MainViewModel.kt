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
import com.parita.chatapplication.model.User
import com.parita.chatapplication.repository.Repository

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
}