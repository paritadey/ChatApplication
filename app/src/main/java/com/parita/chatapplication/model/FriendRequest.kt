package com.parita.chatapplication.model

class FriendRequest {
    var email: String
    var date: String
    var flag: String

    constructor() {
        email = ""
        date = ""
        flag = ""
    }

    constructor(email: String, date: String, flag: String) {
        this.email = email
        this.date = date
        this.flag = flag
    }
}
