package com.parita.chatapplication.view.ui

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.adapter.BlockListAdapter
import com.parita.chatapplication.databinding.FragmentBlockListBinding
import com.parita.chatapplication.model.Friends
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.viewmodel.MainViewModel
import java.util.*

class BlockListFragment : Fragment() {
    private lateinit var defaultPrefs: SharedPreferences
    private lateinit var binding: FragmentBlockListBinding
    private lateinit var list: ArrayList<Friends>
    private lateinit var blockListAdapter: BlockListAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_block_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.setLayoutManager(layoutManager)
        viewModel.initiateBlockList(defaultPrefs.email!!)
        viewModel.getBlockedFriendDataList().observe(viewLifecycleOwner,
            androidx.lifecycle.Observer<ArrayList<Friends>> { friends ->
                list = friends
                viewModel.initiateBlockList(defaultPrefs.email!!)
                viewModel.getBlockedFriendDataList().observe(viewLifecycleOwner,
                    androidx.lifecycle.Observer<ArrayList<Friends>> { friends ->
                        list = friends
                        if (list.size > 0) {
                            binding.noDataImg.setVisibility(View.GONE)
                            blockListAdapter = BlockListAdapter(list)
                            binding.recyclerView.setAdapter(blockListAdapter)
                            blockListAdapter.notifyDataSetChanged()
                            onClickRecyclerItem()
                        } else {
                            binding.noDataImg.setVisibility(View.VISIBLE)
                            binding.recyclerView.setVisibility(View.GONE)
                        }
                    })
            })
    }
    private fun onClickRecyclerItem() {
        blockListAdapter.clickListenerAction(object : BlockListAdapter.onCClicked {
           override fun onClick(v: View) {
                Log.d(
                    "TAG",
                    "Clicked blocked email address: " + list[blockListAdapter.position].email
                )
                showUnblockDialog(list[blockListAdapter.position].email)
            }
        })
    }
    private fun showUnblockDialog(friendEmail: String) {
        val dialogBuilder = AlertDialog.Builder(context).create()
        val inflater = this.layoutInflater
        Objects.requireNonNull(dialogBuilder.window)!!
            .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dialogView: View = inflater.inflate(R.layout.custom_block_layout, null)
        val no = dialogView.findViewById<TextView>(R.id.no_bt)
        val yes = dialogView.findViewById<TextView>(R.id.yes_bt)
        yes.setOnClickListener {
            viewModel.initiateUnblock(defaultPrefs.email!!, friendEmail)
            viewModel.userIsblocked().observe(viewLifecycleOwner,
                androidx.lifecycle.Observer<Boolean?> { aBoolean ->
                    if (!aBoolean!!) {
                        Snackbar.make(binding.root, "User has been unblocked", Snackbar.LENGTH_LONG).show()
                        updateBlockList()
                    } else {
                        Snackbar.make(
                            binding.root,
                            "User cannot be unblocked please try again later!", Snackbar.LENGTH_LONG
                        ).show()
                    }
                })
            dialogBuilder.dismiss()
        }
        no.setOnClickListener { dialogBuilder.dismiss() }
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
        dialogBuilder.setCancelable(true)
    }

    // TODO this section will update the recyclerview by calling the method of fetching the blocklist from Friends node
    private fun updateBlockList() {
        viewModel.initiateBlockList(defaultPrefs.email!!)
        viewModel.getBlockedFriendDataList().observe(viewLifecycleOwner,
            androidx.lifecycle.Observer<ArrayList<Friends>> { friends ->
                list = friends
                if (list.size > 0) {
                    binding.noDataImg.setVisibility(View.GONE)
                    blockListAdapter = BlockListAdapter(list)
                    binding.recyclerView.setAdapter(blockListAdapter)
                    blockListAdapter.notifyDataSetChanged()
                    onClickRecyclerItem()
                } else {
                    binding.noDataImg.setVisibility(View.VISIBLE)
                    binding.recyclerView.setVisibility(View.GONE)
                }
            })
    }

}