package com.parita.chatapplication.view.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mikhaellopez.circularimageview.CircularImageView
import com.parita.chatapplication.R
import com.parita.chatapplication.databinding.FragmentProfileBinding
import com.parita.chatapplication.utils.SharedPreferenceHelper
import com.parita.chatapplication.utils.SharedPreferenceHelper.email
import com.parita.chatapplication.view.MessageListActivity
import com.parita.chatapplication.viewmodel.MainViewModel
import java.io.*

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var profileImagePath: String
    private lateinit var defaultPrefs: SharedPreferences
    private val PICK_IMAGE = 100
    private val CROP_PIC = 2
    private val CLICK_PIC = 3
    private lateinit var outputStream: OutputStream
    private lateinit var returnUri: Uri
    private lateinit var photoURI: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.isOnline(requireContext())) {
            initView(view)
        } else {
            MessageListActivity().createToast(
                context,
                "No internet connection, Please connect with the internet"
            )
        }
    }

    private fun initView(view: View) {
        defaultPrefs = SharedPreferenceHelper.defaultPreference(requireContext())
        viewModel.fetchUserDetails(requireContext())
        viewModel.getUserData().observe(viewLifecycleOwner, Observer { user ->
            binding.userId.setText(user.userId)
            binding.profileName.setText(user.email)
            if (!user.profileImagePath.equals("")) {
                profileImagePath = user.profileImagePath
                loadProfileImage()
            } else {
                //progressBar.setVisibility(View.GONE)
                Log.d("TAG", "Please upload an image")
                MessageListActivity().createToast(context, "Please upload an image")
            }
        })
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.logout.setOnClickListener {

        }
        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        binding.aboutApp.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_aboutAppFragment)
        }
        binding.deactivate.setOnClickListener {

        }
        binding.profilePicture.setOnClickListener {
            showImageChoiceDialog(binding.profilePicture)
        }
    }

    private fun showImageChoiceDialog(profilePicture: CircularImageView) {
        val content: TextView
        val newImage: TextView
        val removeImage: TextView
        val openCamera: TextView
        val dialog = Dialog(requireContext(), R.style.WideDialog)
        dialog.setContentView(R.layout.custom_upload_profile_image_layout)
        dialog.show()
        content = dialog.findViewById<TextView>(R.id.content)
        newImage = dialog.findViewById<TextView>(R.id.new_image)
        removeImage = dialog.findViewById<TextView>(R.id.remove_image)
        openCamera = dialog.findViewById<TextView>(R.id.open_camera)
        newImage.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            } else {
                openGallery()
                dialog.dismiss()
            }
        })
        openCamera.setOnClickListener(View.OnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 2000
                )
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CLICK_PIC)
                dialog.dismiss()
            }
        })

        removeImage.setOnClickListener(View.OnClickListener {
            Log.d("TAG", "Profile image path: $profileImagePath")
            if (profileImagePath != null) {
                defaultPrefs.email?.let { it1 ->
                    viewModel.removeProfileImage(
                        it1,
                        profileImagePath
                    )
                }
                viewModel.getCompleteUpdateRemoveImage().observe(viewLifecycleOwner,
                    Observer<Boolean> { aBoolean ->
                        if (aBoolean) {
                            binding.profilePicture.setImageDrawable(resources.getDrawable(R.drawable.ic_profile_picture))
                            MessageListActivity().createToast(context, "Profile image removed")
                        } else {
                            MessageListActivity().createToast(context, "Error occur")
                        }
                    })
            } else {
                MessageListActivity().createToast(
                    context,
                    "Error in finding the image. Please try after some time"
                )
            }
            dialog.dismiss()
        })
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    private fun loadProfileImage() {
        if (profileImagePath != null) {
            //progressBar.setVisibility(View.VISIBLE)
            defaultPrefs.email?.let {
                viewModel.initiateDownloadImage(
                    it,
                    profileImagePath
                )
            }
            viewModel.getDownloadedImage().observe(viewLifecycleOwner,
                Observer<ByteArray> { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.profilePicture.setImageBitmap(bitmap)
                    //progressBar.setVisibility(View.GONE)
                })
        } else {
            //   progressBar.setVisibility(View.GONE)
            Log.d("TAG", "Profile picture is not set")
        }
    }

    private fun performCrop(picUri: Uri) {
        Log.d("TAG", "Picture uri: $picUri")
        try {
            val cropIntent = Intent("com.android.camera.action.CROP")
            cropIntent.setDataAndType(picUri, "image/*")
            cropIntent.putExtra("crop", "true")
            cropIntent.putExtra("aspectX", 4)
            cropIntent.putExtra("aspectY", 3)
            cropIntent.putExtra("outputX", 240)
            cropIntent.putExtra("outputY", 240)
            cropIntent.putExtra("return-data", true)
            startActivityForResult(cropIntent, CROP_PIC)
        } catch (anfe: ActivityNotFoundException) {
            val toast = Toast.makeText(
                context,
                "This device doesn't support the crop action!",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }

    fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                inImage,
                "Title",
                null
            )
        return Uri.parse(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                returnUri = data!!.data!!
                performCrop(returnUri)
            } else if (requestCode == CROP_PIC) {
                if (data != null) {
                    val bundle = data.extras
                    val bitmap = bundle!!.getParcelable<Bitmap>("data")
                    photoURI =
                        bitmap?.let { getImageUri(it) }!! // TODO convert the cropped bitmap to uri and upload to database
                    Log.d("TAG", "Photo uri: $photoURI")
                    binding.profilePicture.setImageBitmap(bitmap)
                    uploadImageToFirebase()
                } else {
                    MessageListActivity().createToast(context, "Error in uploading image")
                }
            } else if (requestCode == CLICK_PIC) {
                val extra = data!!.extras
                val bitmap = extra!!["data"] as Bitmap?
                val filePath = Environment.getExternalStorageDirectory().toString() + "/Capture/"
                val dir = File(filePath)
                if (!dir.exists()) dir.mkdir()
                val file = File(dir, System.currentTimeMillis().toString() + ".jpg")
                try {
                    outputStream = FileOutputStream(file)
                    Log.d("TAG", "Output Stream: $outputStream")
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "FileNotFoundException: " + e.message)
                }
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val finalBitmapImage: Bitmap = resize(bitmap, 540, 540)
                binding.profilePicture.setImageBitmap(finalBitmapImage)
                photoURI = getImageUri(finalBitmapImage)
                Log.d("TAG", "Uri details: $photoURI")
                uploadImageToFirebase()
                try {
                    outputStream.flush()
                } catch (e: IOException) {
                    Log.d("TAG", "IOException:" + e.message)
                }
                try {
                    outputStream.close()
                } catch (e: IOException) {
                    Log.d("TAG", "IOException :" + e.message)
                }
            }
        }
    }

    private fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var width = image.width
        var height = image.height
        Log.d("TAG", "Width and height are:$width\t$height")
        //TODO just setting the height and width to 540, otherwise picture is pixeled into parts
        height = maxHeight
        width = maxWidth
        Log.d("Pictures", "after scaling Width and height are $width--$height")
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun uploadImageToFirebase() {
        Log.d("TAG", "Picture uri: $photoURI")
        if (photoURI != null) {
            //  progressBar.setVisibility(View.VISIBLE)
            defaultPrefs.email?.let { viewModel.getPreviousImage(it) }
            viewModel.getCompletePreviousImage().observe(viewLifecycleOwner,
                Observer<String?> { s ->
                    if (s != null) {
                        defaultPrefs.email?.let {
                            viewModel.initiateImageUpload(
                                photoURI,
                                it,
                                s
                            )
                        }
                        viewModel.getUpdateOnImageUpload().observe(viewLifecycleOwner,
                            Observer<Boolean> { aBoolean ->
                                if (aBoolean) {
                                    //progressBar.setVisibility(View.GONE)
                                    Log.d("TAG", "User Profile picture is uploaded")
                                    MessageListActivity().createToast(
                                        context,
                                        "Profile picture is uploaded successfully."
                                    )
                                } else {
                                    // progressBar.setVisibility(View.GONE)
                                    Log.d("TAG", "Error")
                                    MessageListActivity().createToast(context, "Please try again.")
                                }
                            })
                    } else {
                        Log.d("TAG", "NO PROFILE PICTURE PATH IS PRESENT")
                        //progressBar.setVisibility(View.VISIBLE)
                        defaultPrefs.email?.let {
                            if (s != null) {
                                viewModel.initiateImageUpload(
                                    photoURI,
                                    it,
                                    s
                                )
                            }
                        }
                        viewModel.getUpdateOnImageUpload().observe(viewLifecycleOwner,
                            Observer<Boolean> { aBoolean ->
                                if (aBoolean) {
                                    // progressBar.setVisibility(View.GONE)
                                    Log.d("TAG", "User Profile picture is uploaded")
                                    MessageListActivity().createToast(
                                        context,
                                        "Profile picture is uploaded successfully."
                                    )
                                } else {
                                    // progressBar.setVisibility(View.GONE)
                                    Log.d("TAG", "Error")
                                    MessageListActivity().createToast(context, "Please try again.")
                                }
                            })
                    }
                })
        }
    }

}