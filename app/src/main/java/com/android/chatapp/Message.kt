package com.android.chatapp

open class Message(
    open val txt: String,
    open val fromId: String,
    open val toId: String,
    open val timestamp: Long
) {
    constructor(): this("", "", "", 0)
}
