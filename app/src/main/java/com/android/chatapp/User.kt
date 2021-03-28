package com.android.chatapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uuid: String,
    val name: String,
    val profileUrl: String): Parcelable {

    constructor() : this("", "", "")
}
