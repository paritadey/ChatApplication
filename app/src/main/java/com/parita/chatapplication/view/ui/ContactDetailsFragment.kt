package com.parita.chatapplication.view.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentContactDetailsBinding
import com.parita.chatapplication.model.FriendRequest
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class ContactDetailsFragment : Fragment() {

    private lateinit var binding: FragmentContactDetailsBinding
    private lateinit var friendType: String
    private val viewModel: MainViewModel by viewModels()
    private lateinit var defaultPrefs: SharedPreferences
    private lateinit var friendRequest: FriendRequest
    private lateinit var c: Date
    private lateinit var df: SimpleDateFormat
    private var counter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            binding.emailId.text = arguments?.getString("userEmail")!!
            friendType = arguments?.getString("userType")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_contact_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        initiateViewForRequest()
        c = Calendar.getInstance().time
        df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.sendRequest.setOnClickListener {
            if (friendType.equals("friend")) {
                if (binding.sendRequest.text.toString() == "Block User") {
                    viewModel.initiateBlockUser(
                        defaultPrefs.email!!,
                        binding.emailId.text.toString().trim()
                    ) //TODO initiate block operation if user want to block the user
                    viewModel.userIsBlocked().observe(viewLifecycleOwner,
                        Observer<Boolean> { aBoolean ->
                            if (aBoolean) {
                                Snackbar.make(
                                    binding.root,
                                    "User has been blocked",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                binding.sendRequest.text = "Unblock User"
                            } else {
                                Snackbar.make(
                                    binding.root,
                                    "User cannot be blocked please try again later!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        })
                } else {
                    viewModel.initiateUnBlockUser(
                        defaultPrefs.email!!,
                        binding.emailId.text.toString().trim()
                    ) //TODO initiate unblock operation if the user is already blocked.
                    viewModel.userIsBlocked().observe(viewLifecycleOwner,
                        Observer<Boolean> { aBoolean ->
                            if (!aBoolean!!) {
                                Snackbar.make(
                                    binding.root,
                                    "User has been unblocked",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                binding.sendRequest.setText("Block User")
                            } else {
                                Snackbar.make(
                                    binding.root,
                                    "User cannot be unblocked please try again later!",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        })
                }
            } else {
                //TODO code to write - on clicking of send request, database call and send notification to the user of request
                // TODO in this section user who will send the request to other user, the other user will get the request under Sent section

                //TODO code to write - on clicking of send request, database call and send notification to the user of request
                // TODO in this section user who will send the request to other user, the other user will get the request under Sent section
                friendRequest = FriendRequest()
                friendRequest.email = binding.emailId.getText().toString().trim()
                friendRequest.date = df.format(c)
                friendRequest.flag = "neutral"
                viewModel.insertFriendRequest(friendRequest, defaultPrefs.email!!)
                viewModel.getUpdateCompleteSendRequest().observe(
                    viewLifecycleOwner, Observer<Boolean> { aBoolean ->
                        if (aBoolean) {
                            Snackbar.make(
                                binding.root,
                                "Friend Request is send",
                                Snackbar.LENGTH_LONG
                            ).show()
                            binding.sendRequest.setText("Request Sent")
                        }
                    })
            }
        }
        binding.cancelRequest.setOnClickListener {
            if (friendType.equals("friend")) {
                viewModel.initateUnfriendProcess(
                    defaultPrefs.email!!,
                    binding.emailId.text.toString().trim()
                )
                viewModel.userIsUnfriended().observe(viewLifecycleOwner,
                    Observer<Boolean> { aBoolean ->
                        if (aBoolean) {
                            counter++
                            if (counter == 2) {
                                findNavController().popBackStack()
                                Snackbar.make(
                                    binding.root,
                                    binding.emailId.text.toString().trim()
                                            + "has been un-friend.", Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                Snackbar.make(binding.root, "processing...", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Snackbar.make(
                                binding.root,
                                "Un-friend request could not be processed..", Snackbar.LENGTH_LONG
                            ).show()
                        }
                    })
            } else {
                //TODO code to write - on clicking of cancel request, remove the node created due to send request
                //TODO code to write - on clicking of cancel request, remove the node created due to send request
                friendRequest = FriendRequest()
                friendRequest.email = binding.emailId.getText().toString().trim()
                viewModel.removeFriendRequest(friendRequest, defaultPrefs.email!!)
                viewModel.getUpdateCompleteCancelRequest().observe(
                    viewLifecycleOwner, Observer<Boolean> { aBoolean ->
                        if (aBoolean) {
                            Snackbar.make(
                                binding.root,
                                "Friend request is cancelled",
                                Snackbar.LENGTH_LONG
                            ).show()
                            binding.sendRequest.text = "Send Request"
                        }
                    })
            }
        }
    }

    private fun initiateViewForRequest() {
        if (friendType.equals("friend")) {
            //TODO if already a friend then showing option and operation to block/unblock or unfriend the user.
            viewModel.getFriendshipStatus(
                defaultPrefs.email!!,
                binding.emailId.text.toString().trim()
            ) //TODO check for friendship status it will be either friend or block.

            viewModel.getUpdateRequest().observe(viewLifecycleOwner,
                Observer<String> { s ->
                    if (s == "block") {
                        binding.sendRequest.text = "Unblock User"
                    } else if (s == "friend") {
                        binding.sendRequest.text = "Block User"
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Unexpected error please try again later!", Snackbar.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                })
            binding.cancelRequest.setText("Unfriend")
        } else {
            friendRequest = FriendRequest()
            friendRequest.email = binding.emailId.text.toString().trim()
            viewModel.requestFlagMethod(friendRequest, defaultPrefs.email!!)
            viewModel.getUpdateRequestMethod().observe(viewLifecycleOwner,
                Observer<String> { s ->
                    if (s == "neutral") {
                        binding.sendRequest.setText("Request Sent")
                    } else if (s == "accepted") {
                        binding.sendRequest.setText("Accepted")
                    } else if (s == "rejected") {
                        binding.sendRequest.setText("Send Request")
                    } else if (s == "no_data") {
                        //TODO noting to change
                    }
                })
        }
    }
}