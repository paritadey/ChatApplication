package com.parita.chatapplication.view.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.blockList.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_blockListFragment)
        }
        binding.feedback.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_feedbackFragment)
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.contactUs.setOnClickListener {
            showEmailDialog()
        }
        binding.help.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_helpFragment)
        }
        binding.refer.setOnClickListener {
           // findNavController().navigate(R.id.action_settingsFragment_to_referFragment)
            Snackbar.make(binding.root, "App is not live in google play store, cannot use this feature", Snackbar.LENGTH_LONG).show()
        }
        binding.privacy.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://drive.google.com/file/d/1kbEtVxI6LSIyxEM_akTNx1dnlMzRVsAZ/view?usp=sharing")
            startActivity(i)
        }
    }
    private fun showEmailDialog() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("paritadey@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us for Chattzz")
        intent.putExtra(Intent.EXTRA_TEXT, "hello developer !!!")
        startActivity(Intent.createChooser(intent, "Send Email"))
    }
}