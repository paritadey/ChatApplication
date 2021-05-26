package com.parita.chatapplication.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentSignUpBinding
import com.parita.chatapplication.viewmodel.MainViewModel

class SignUp : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signupButton.setOnClickListener {
            if (viewModel.validateDetails(
                    binding.email.text.toString().trim(),
                    binding.password.text.toString().trim(),
                    binding.cpassword.text.toString().trim(), binding
                )
            ) {
                viewModel.initiateSignUp(
                    binding.email.text.toString().trim(),
                    binding.password.text.toString().trim()
                )
                clearFields()
                hideSoftKeyboard(requireActivity())
                viewModel.getSignupStatus().observe(viewLifecycleOwner, Observer {
                    if (!it) {
                        Log.d("TAG", "Account Successfully Created")
                        fragmentManager?.popBackStack()
                    } else {
                        Log.d("TAG", "Account Already Exists")
                        Snackbar.make(
                            binding.root,
                            "Account already exists! Please verify account to login.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        fragmentManager?.popBackStack()
                    }
                })
            } else {
                hideSoftKeyboard(requireActivity())
                Log.d("TAG", "all validation messages are provided")
            }
        }
    }
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputMethodManager.isActive){
            if(activity.currentFocus!=null)
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    private fun clearFields() {
        binding.email.text.clear()
        binding.password.text.clear()
        binding.cpassword.text.clear()
    }
}