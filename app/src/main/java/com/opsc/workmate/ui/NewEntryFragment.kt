package com.opsc.workmate.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.opsc.workmate.R
import com.opsc.workmate.data.Entry
import com.opsc.workmate.data.Global
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Locale

class NewEntryFragment : Fragment() {

    //Variables
    private lateinit var btnStartTime: Button
    private lateinit var btnEndTime: Button
    private lateinit var btnDate: Button
    private lateinit var txtDescription: EditText
    private lateinit var imgEntryImage: ImageView
    private lateinit var btnUploadImg: Button
    private lateinit var btnCreate: Button
    private lateinit var btnCategoryPicker: Button
    private lateinit var categoryNames: List<String>


    private val calendar: Calendar = Calendar.getInstance()

    companion object {
        private const val REQUEST_IMAGE_PICKER = 100 // Constant for image picker request code
        private const val PERMISSION_REQUEST_CODE = 101 // Constant for permission request code
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_entry, container, false)

        btnStartTime = view.findViewById(R.id.btnStartTimePicker)
        btnEndTime = view.findViewById(R.id.btnEndTimePicker)
        btnDate = view.findViewById(R.id.btnDatePicker)
        txtDescription = view.findViewById(R.id.txtDescription)
        imgEntryImage = view.findViewById(R.id.imgEntryImage)
        btnUploadImg = view.findViewById(R.id.btnUploadImg)
        btnCreate = view.findViewById(R.id.btnCreate)

        btnStartTime.setOnClickListener {
            showTimePickerDialog(btnStartTime)
        }

        btnEndTime.setOnClickListener {
            showTimePickerDialog(btnEndTime)
        }

        btnDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnUploadImg.setOnClickListener {
            checkPermissionsAndOpenImagePicker()
        }

        btnCreate.setOnClickListener {
            addEntry()
        }

        btnCategoryPicker = view.findViewById(R.id.btnCategoryPicker)
        categoryNames = Global.categories.map { it.name } // Retrieve category names from Global.categories

        btnCategoryPicker.setOnClickListener {
            showCategoryPickerDialog()
        }


        return view
    }

    private fun showCategoryPickerDialog() {
        val categoryArray = categoryNames.toTypedArray()
        val selectedCategoryIndex = categoryArray.indexOf(btnCategoryPicker.text.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setSingleChoiceItems(categoryArray, selectedCategoryIndex) { dialog, which ->
                btnCategoryPicker.text = categoryArray[which]
                dialog.dismiss()
            }
            .show()
    }

    private fun addEntry() {
        val startTime = btnStartTime.text.toString()
        val endTime = btnEndTime.text.toString()
        val date = btnDate.text.toString()
        val description = txtDescription.text.toString()
        val categoryName = btnCategoryPicker.text.toString()
        val currentUser = Global.currentUser?.username.orEmpty()
        val imageData = convertImageToBase64(imgEntryImage).toString()

        if (startTime.isNotEmpty() && endTime.isNotEmpty() && date.isNotEmpty()) {
            val entry = Entry(
                currentUser,
                categoryName,
                date,
                startTime,
                endTime,
                imageData
            )

            Global.entries.add(entry)
            Toast.makeText(requireContext(), "Entry added successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertImageToBase64(imageView: ImageView): String? {
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            if (bitmap != null) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        }
        return null
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            }
        }
    }

    // Check and request necessary permissions for image upload
    private fun checkPermissionsAndOpenImagePicker() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                PERMISSION_REQUEST_CODE
            )
        } else {
            openImagePicker()
        }
    }

    // Open the image picker (gallery)
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICKER)
    }

    // Handle the result of the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imgEntryImage.setImageURI(imageUri)
        }
    }

    private fun showTimePickerDialog(button: Button) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Update button text with the selected time
                val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
                button.text = formattedTime
            },
            hour,
            minute,
            false
        )

        timePickerDialog.show()
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)

                // Update button text with the selected date
                val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(calendar.time)
                btnDate.text = formattedDate
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.show()
    }

}