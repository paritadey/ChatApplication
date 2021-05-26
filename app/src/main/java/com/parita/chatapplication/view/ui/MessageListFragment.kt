package com.parita.chatapplication.view.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message_list, container, false)
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        if (viewModel.isOnline(requireContext())) {
            //loadMessageList()
        } else {
            MessageListActivity().createToast(
                context,
                "No internet connection. Please connect with the internet"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        initView()
    }

    private fun initView() {

    }

}