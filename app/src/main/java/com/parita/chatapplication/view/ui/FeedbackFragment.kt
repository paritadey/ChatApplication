package com.parita.chatapplication.view.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentFeedbackBinding
import com.parita.chatapplication.model.Feedback
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private var ratingStep = 0f
    private lateinit var defaultPrefs: SharedPreferences
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feedback, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.ratingBar.setOnRatingBarChangeListener(OnRatingBarChangeListener { ratingBar, rating, fromUser ->
            ratingStep = rating
        })
        binding.submit.setOnClickListener {
            val today = Date()
            val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val dateToString = format.format(today)
            Log.d("TAG", "Ratings: $ratingStep")
            val feedback = Feedback()
            feedback.email = defaultPrefs.email!!
            feedback.ratings = ratingStep
            feedback.dateTime = dateToString
            feedback.message = binding.message.getText().toString().trim()
            viewModel.initiateUploadFeedback(feedback)
            viewModel.getFeedbackCompleted().observe(viewLifecycleOwner,
                androidx.lifecycle.Observer<Boolean> { aBoolean ->
                    if (aBoolean) {
                        Snackbar.make(binding.root, "Feedback is updated", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(binding.root, "Error in uploading Feedback", Snackbar.LENGTH_LONG).show()
                    }
                    clearFields()
                })
        }
    }

    private fun clearFields() {
        binding.message.setText("")
        binding.message.setHint(R.string.please_enter_your_message)
        binding.ratingBar.setStepSize(0.5f)
    }

}