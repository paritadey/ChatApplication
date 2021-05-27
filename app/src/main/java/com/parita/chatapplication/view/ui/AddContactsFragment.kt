package com.parita.chatapplication.view.ui

import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.parita.chatapplication.R
import com.parita.chatapplication.adapter.AddContactsAdapter
import com.parita.chatapplication.databinding.FragmentAddContactsBinding
import com.parita.chatapplication.model.User
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.viewmodel.MainViewModel
import java.util.*
import kotlin.collections.ArrayList

class AddContactsFragment : Fragment() {

    private lateinit var binding: FragmentAddContactsBinding
    private val viewModel: MainViewModel by viewModels()
    private var searchString = ""
    private lateinit var userEmail: String
    private lateinit var defaultPrefs: SharedPreferences
    private lateinit var list: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_contacts, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(resources.getColor(R.color.white))
        gradientDrawable.cornerRadius = 24f
        binding.searchBar.setBackground(gradientDrawable)
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.setLayoutManager(layoutManager)
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.searchImg.setVisibility(View.GONE)
                userEmail = defaultPrefs.email!!
                searchString = s.toString() + ""
                viewModel.initiateSearch(searchString)
                viewModel.getSearchList()
                    .observe(viewLifecycleOwner, Observer<ArrayList<User>> { users ->
                        val hashSet: LinkedHashSet<User> =
                            LinkedHashSet(users) //TODO this is used to remove duplicates in an array list
                        list = ArrayList(hashSet)
                        val addContactsAdapter = AddContactsAdapter(list)
                        binding.recyclerView.setAdapter(addContactsAdapter)
                        addContactsAdapter.notifyDataSetChanged()
                        addContactsAdapter.clickListenerAction(object : AddContactsAdapter.onCClicked {
                            override fun onClick(v: View) {
                                //TODO action when elements of the recycler view is clicked
                                Log.d(
                                    "TAG",
                                    "Clicked on: " + list.get(addContactsAdapter.position)
                                        .email + "\t" + list.get(addContactsAdapter.position)
                                        .isAccountStatus
                                )
                                if (list.get(addContactsAdapter.position).email.equals(userEmail)) {
                                    //TODO action when user clicked on his/her emailid on search then it will redirect to the user's profile
                                    findNavController().navigate(R.id.action_addContactsFragment_to_profileFragment)
                                } else {
                                    //TODO Check for whether a friend request has been received from the user that is ben searched
                                    viewModel.checkForAlreadyRequestReceived(userEmail, list.get(addContactsAdapter.position).email)
                                    viewModel.getAlreadyRequestReceived()
                                        .observe(viewLifecycleOwner,
                                            Observer<Boolean> { aBoolean ->
                                                if (aBoolean) { //TODO if true the navigate to the friend request section.
                                                    findNavController().navigate(R.id.action_addContactsFragment_to_sentAndReceivedConnectionRequest)
                                                } else {
                                                    //TODO to check if the user that is being sercehed for is already a friend or not.
                                                    viewModel.initiateFriendSearch(
                                                        userEmail,
                                                        list.get(addContactsAdapter.position)
                                                            .email
                                                    )
                                                    viewModel.getAlreadyFriend()
                                                        .observe(viewLifecycleOwner,
                                                            Observer<Boolean> { aBoolean ->
                                                                val bundle = Bundle()
                                                                bundle.putString(
                                                                    "userEmail",
                                                                    list.get(addContactsAdapter.position)
                                                                        .email
                                                                )
                                                                if (aBoolean) { //TODO if true then send friend to the next page(ContactDetailsFragment)
                                                                    bundle.putString(
                                                                        "userType",
                                                                        "friend"
                                                                    )
                                                                } else { //TODO if true then send no_friend to the next page(ContactDetailsFragment)
                                                                    bundle.putString(
                                                                        "userType",
                                                                        "not_friend"
                                                                    )
                                                                }
                                                                findNavController().navigate(
                                                                    R.id.action_addContactsFragment_to_contactDetailsFragment,
                                                                    bundle
                                                                )
                                                            })
                                                }
                                            })
                                }
                            }
                        })
                    })
            }

            override fun afterTextChanged(s: Editable) {}
        })

    }
}