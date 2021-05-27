package com.parita.chatapplication.view.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentMessageListBinding
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.view.MessageListActivity
import com.parita.chatapplication.viewmodel.MainViewModel

class MessageListFragment : Fragment() {
    private lateinit var binding: FragmentMessageListBinding
    private lateinit var defaultPrefs: SharedPreferences
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_message_list, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isOnline(requireContext())) {
            //loadMessageList()
        } else {
            Snackbar.make(binding.root,
                "No internet connection. Please connect with the internet", Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        initView()
    }

    private fun initView() {
        binding.profile.setOnClickListener {
            findNavController().navigate(R.id.action_messageListFragment_to_profileFragment)
        }
        binding.friends.setOnClickListener {
            findNavController().navigate(R.id.action_messageListFragment_to_friendsFragment)
        }
        binding.addFriend.setOnClickListener {
            findNavController().navigate(R.id.action_messageListFragment_to_addContactsFragment)
        }
        binding.notification.setOnClickListener {
            findNavController().navigate(R.id.action_messageListFragment_to_notificationFragment)
        }
        binding.sync.setOnClickListener {
            if (viewModel.isOnline(requireContext())) {
                // loadMessageList()
            } else {
                Snackbar.make(
                    binding.root,
                    "No internet connection. Please connect with the internet", Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

}