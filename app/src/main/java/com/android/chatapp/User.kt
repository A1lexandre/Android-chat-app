package com.android.chatapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val token: String,
    val online: Boolean): Parcelable {

    constructor() : this("", "", "", "", true)

    constructor(uuid: String, name: String, profileUrl: String): this(uuid, name, profileUrl, "", true)
}
