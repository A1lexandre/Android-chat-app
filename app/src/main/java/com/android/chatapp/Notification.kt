package com.android.chatapp

data class Notification(
    override val txt: String,
    override val fromId: String,
    override val toId: String,
    override val timestamp: Long,
    val fromName: String
): Message()
