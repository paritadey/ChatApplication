package com.parita.chatapplication.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.parita.chatapplication.R
import com.parita.chatapplication.adapter.HelpAdapter
import com.parita.chatapplication.databinding.FragmentHelpBinding
import com.parita.chatapplication.model.Help

class HelpFragment : Fragment() {

    private lateinit var binding: FragmentHelpBinding
    private val helpList: ArrayList<Help> = ArrayList()
    private lateinit var helpAdapter: HelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helpAdapter = HelpAdapter(helpList)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.setLayoutManager(layoutManager)
        binding.recyclerView.setItemAnimator(DefaultItemAnimator())
        binding.recyclerView.setAdapter(helpAdapter)
        prepareHelp()
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun prepareHelp() {
        var helps =
            Help(resources.getString(R.string.dashboard_help), R.drawable.messagelisthelp)
        helpList.add(helps)
        helps = Help(resources.getString(R.string.profile_help), R.drawable.profilehelp)
        helpList.add(helps)
        helps = Help(resources.getString(R.string.search_help), R.drawable.searchhelp)
        helpList.add(helps)
        helps = Help(resources.getString(R.string.settings_help), R.drawable.settingshelp)
        helpList.add(helps)
        helps = Help(resources.getString(R.string.contact_us_help), R.drawable.contacthelp)
        helpList.add(helps)
        helps = Help(resources.getString(R.string.feedback_help), R.drawable.feedbackhelp)
        helpList.add(helps)
        helpAdapter.notifyDataSetChanged()
    }

}