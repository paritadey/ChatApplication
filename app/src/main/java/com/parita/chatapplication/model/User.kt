package com.parita.chatapplication.model

class User {
    var name: String
    var userName: String
    var email: String
    var password: String
    var isLoginStatus: Boolean
    var isAccountStatus: Boolean
    var userId: String
    var profileImagePath: String

    constructor() {
        name = ""
        userName = ""
        email = ""
        password = ""
        isLoginStatus = false
        isAccountStatus = false
        userId = ""
        profileImagePath = ""
    }

    constructor(
        name: String,
        userName: String,
        email: String,
        password: String,
        loginStatus: Boolean,
        accountStatus: Boolean,
        userId: String,
        profileImagePath: String
    ) {
        this.name = name
        this.userName = userName
        this.email = email
        this.password = password
        isLoginStatus = loginStatus
        isAccountStatus = accountStatus
        this.userId = userId
        this.profileImagePath = profileImagePath
    }
}
