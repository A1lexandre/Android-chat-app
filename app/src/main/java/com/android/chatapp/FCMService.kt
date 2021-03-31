package com.android.chatapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService

class FCMService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().uid

        if(uid != null) {
            Firebase.firestore.collection("users")
                    .document(uid as String).update("token", token)
        }
    }
}