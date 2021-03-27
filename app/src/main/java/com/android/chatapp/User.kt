package com.android.chatapp

data class User(
    val uuid: String,
    val name: String,
    val profileUrl: String) {

    constructor() : this("", "", "")
}
