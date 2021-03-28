package com.android.chatapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.android.chatapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var mSelectedUri: Uri? = null

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

        btnSignup.setOnClickListener {
            val userName = editName.text.trim()
            val userEmail = editEmail.text.toString()
            val userPassword = editPassword.text.toString()

            if (userName.isNotEmpty() && userEmail.isNotEmpty()
                && userPassword.isNotEmpty() && mSelectedUri != null)
                createUser(userEmail, userPassword)
            else
                Toast.makeText(this@RegisterActivity, "Preencha todos os dados do formulário", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUser(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    saveUser()
                }
                .addOnFailureListener {
                    Toast.makeText(this@RegisterActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
    }

    private fun saveUser() {
        val filename = UUID.randomUUID().toString()
        val ref = Firebase.storage.getReference("images/$filename")
            ref.putFile(mSelectedUri as Uri)
                .addOnSuccessListener {
                        ref.downloadUrl
                                .addOnSuccessListener {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid as String
                                val userName = binding.editName.text.toString()

                                Firebase.firestore.collection("users")
                                    .document(uid)
                                        .set(User(uid, userName, it.toString()))
                                        .addOnCompleteListener {
                                            if(it.isSuccessful) {
                                                val intent = Intent(this@RegisterActivity, MessagesActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startActivity(intent)
                                            } else {
                                                FirebaseAuth.getInstance().currentUser?.delete()
                                                ref.delete()
                                                Toast.makeText(this@RegisterActivity, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                                .addOnFailureListener {
                                    FirebaseAuth.getInstance().currentUser?.delete()
                                    ref.delete()
                                    Toast.makeText(this@RegisterActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                }
                    .addOnFailureListener {
                        FirebaseAuth.getInstance().currentUser?.delete()
                        Toast.makeText(this@RegisterActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
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