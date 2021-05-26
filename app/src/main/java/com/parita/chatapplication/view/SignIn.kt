package com.parita.chatapplication.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.ActivitySignInBinding
import com.parita.chatapplication.viewmodel.MainViewModel

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        initView()
    }

    private fun initView() {
        binding.signupTxt.setOnClickListener {
            replaceFragment(SignUp())
        }
        binding.loginButton.setOnClickListener {
            if (viewModel.validateLoginDetails(
                    binding.loginEmail.text.toString().trim(),
                    binding.loginPassword.text.toString().trim(),
                    binding
                )
            ) {
                viewModel.getLoginStatus(
                    binding.loginEmail.text.toString().trim(),
                    binding.loginPassword.text.toString().trim(), this
                )
                clearFields()
                hideSoftKeyboard(this)
                viewModel.getLoginStatus().observe(this, Observer {
                    if (it) {
                        startActivity(Intent(this@SignIn, MessageListActivity::class.java))
                        finish()
                    } else {
                        Snackbar.make(binding.root, "Login failed due to error", Snackbar.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun replaceFragment(someFragment: Fragment) {
        val transaction: FragmentTransaction =
            supportFragmentManager.beginTransaction() as FragmentTransaction
        transaction.replace(R.id.frame_container, someFragment).addToBackStack(null).commit()
    }

    private fun clearFields() {
        binding.loginEmail.text.clear()
        binding.loginPassword.text.clear()
    }
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputMethodManager.isActive){
            if(activity.currentFocus!=null)
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

}