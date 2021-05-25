package com.parita.chatapplication.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.ActivitySplashScreenBinding
import com.parita.chatapplication.viewmodel.MainViewModel

class SplashScreen : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var isOnInternet: Boolean = false
    private val REQUEST_CODE = 200
    private var flag: Boolean = true
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen)
        isOnInternet = viewModel.isNetworkAvailable(this)
        if (getRuntimePermissions()) {
            if (isOnInternet)
                moveToLogin()
        }
    }

    private fun getRuntimePermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_CODE
            )
            return false
        } else
            return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == -1) {
                    flag = !flag
                    Log.d("TAG", "All permissions are not provided")
                    Handler().postDelayed({
                        getRuntimePermissions()
                    }, 2000)
                    break
                }
            }
            if (flag) {
                if (!isOnInternet) {
                    Snackbar.make(
                        binding.root,
                        "Please allow all the permissions",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    moveToLogin()
                }
            }
        }
    }

    private fun moveToLogin() {
        Handler().postDelayed({
            startActivity(Intent(this, SignIn::class.java))
            finish()
        }, 4000)
    }
}