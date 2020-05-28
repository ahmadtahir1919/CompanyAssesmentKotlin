package com.example.companytest.Views

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.companytest.R
import com.example.companytest.RxBus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.bottom_sheet_update_profile_image.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetAddImage : BottomSheetDialogFragment() {


    var cameraPermission: PermissionListener? = null
    var galleryPermission: PermissionListener? = null
    var pictureSaveFolderPath: File? = null
    var outputImageFile: File? = null
    var currentPhotoPath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        /*  if (getArguments() != null) {
            Bundle bundle = getArguments();
            isBottomSheetOpen = bundle.getBoolean("is_open");
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_update_profile_image, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        checkPermissions()
        clickListners()
    }

    private fun checkPermissions() {
        galleryPermission = object : PermissionListener {
            override fun onPermissionGranted() {
                val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(i, PICK_PICTURE_FROM_GALLERY)
                MainActivity.isBottomSheetOpen = true
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String?>?) {
                MainActivity.isBottomSheetOpen = true
            }
        }
        cameraPermission = object : PermissionListener {
            override fun onPermissionGranted() {
                MainActivity.isBottomSheetOpen = true
                dispatchTakePictureIntent()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String?>?) {
                MainActivity.isBottomSheetOpen = true
            }
        }
    }

    private fun clickListners() {
        camera1?.setOnClickListener({
            MainActivity.isBottomSheetOpen = true
            if (context != null) TedPermission.with(context)
                    .setPermissionListener(cameraPermission)
                    .setDeniedMessage(R.string.permission_denied)
                    .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check()
        })
        gallery2?.setOnClickListener({
            MainActivity.isBottomSheetOpen = true
            TedPermission.with(context)
                    .setPermissionListener(galleryPermission)
                    .setDeniedMessage(R.string.permission_denied)
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PICTURE_FROM_CAMERA) {
                val f = File(currentPhotoPath)
                RxBus.getInstance()?.sendUserImage(f)
                dismiss()
                MainActivity.isBottomSheetOpen = false
            }
            if (requestCode == PICK_PICTURE_FROM_GALLERY) {
                saveImageFromGalary(data)
            }
        }
    }

    private fun saveImageFromGalary(data: Intent?) {
        if (data?.getData() != null) {
            val selectedImage = data.getData()
            val filePathColumn = arrayOf<String?>(MediaStore.Images.Media.DATA)
            if (context != null) {
                val cursor = selectedImage?.let { context?.getContentResolver()?.query(it, filePathColumn, null, null, null) }
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val picturePath = cursor.getString(columnIndex)
                    cursor.close()
                    val selectedFile = File(picturePath)
                    RxBus.getInstance()?.sendUserImage(selectedFile)
                    dismiss()
                    MainActivity.isBottomSheetOpen = false
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity?.getPackageManager()?.let { takePictureIntent.resolveActivity(it) } != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(activity!!,
                        "com.example.companytest.fileprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, TAKE_PICTURE_FROM_CAMERA)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        )
        currentPhotoPath = image.absolutePath
        return image
    }

    companion object {
        private const val TAKE_PICTURE_FROM_CAMERA = 111
        private const val PICK_PICTURE_FROM_GALLERY = 222
    }
}