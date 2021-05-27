package com.parita.chatapplication.model

class Friends {
    var email: String
    var flag: String
    var typingStatus: String? = null

    constructor() {
        email = ""
        flag = ""
        typingStatus = ""
    }

    constructor(email: String, flag: String) {
        this.email = email
        this.flag = flag
    }
}
