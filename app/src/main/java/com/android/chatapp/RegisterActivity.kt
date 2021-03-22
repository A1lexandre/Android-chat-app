package com.android.chatapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.android.chatapp.databinding.ActivityRegisterBinding
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mSelectedUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
    }

    private fun setupButtonListeners() = with(binding) {
        btnSelectPhoto.setOnClickListener {
            pickPhotoFromGallery()
        }
    }

    private fun pickPhotoFromGallery() {
//        val pickIntent = Intent(Intent.ACTION_PICK)
//        pickIntent.type = "image/*"
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, PICK_PHOTO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PHOTO_CODE)
            mSelectedUri = data?.data as Uri

        try {
            with(binding) {
                imgSelectedPhoto.setImageURI(mSelectedUri)
                btnSelectPhoto.alpha = 0f
            }
        } catch (e: IOException) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val PICK_PHOTO_CODE = 12
    }
}