package com.android.chatapp

data class Contact(
        val uuid: String,
        val userName: String,
        val userPhotoUrl: String,
        val lastMessage: String,
        val timestamp: Long
) {
    constructor(): this("", "", "", "", 0)
}
