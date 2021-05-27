package com.parita.chatapplication.model

class Feedback {
    var message: String
    var email: String
    var dateTime: String
    var ratings: Float

    constructor() {
        email = ""
        message = ""
        dateTime = ""
        ratings = 0f
    }

    constructor(message: String, email: String, dateTime: String, ratings: Int) {
        this.message = message
        this.email = email
        this.dateTime = dateTime
        this.ratings = ratings.toFloat()
    }
}
