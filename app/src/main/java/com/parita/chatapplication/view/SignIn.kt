package com.parita.chatapplication.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseUser
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        initView()
    }

    private fun initView() {
//        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        binding.signupTxt.setOnClickListener {
            replaceFragment(SignUp())
        }
    }
    private fun replaceFragment(someFragment: Fragment) {
        val transaction: FragmentTransaction =
            supportFragmentManager.beginTransaction() as FragmentTransaction
        transaction.replace(R.id.frame_container, someFragment).addToBackStack(null).commit()
    }

}