package com.android.chatapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatApplication : Application(), Application.ActivityLifecycleCallbacks {

    private fun setOnline(enabled: Boolean) {
        val uid = FirebaseAuth.getInstance().uid
        if(uid != null) {
            Firebase.firestore.collection("users")
                .document(uid).update("online", enabled)
        }
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        setOnline(true)
    }

    override fun onActivityPaused(activity: Activity) {
        setOnline(false)
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}