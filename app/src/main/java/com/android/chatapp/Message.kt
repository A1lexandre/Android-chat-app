package com.android.chatapp

data class Message(
    val txt: String,
    val fromId: String,
    val toId: String,
    val timestamp: Long
) {
    constructor(): this("", "", "", 0)
}
