package com.parita.chatapplication.model

class Notification {
    var notificationMessage: String
    var emailFrom: String
    var dateTime: String
    var notificationType: Int
    var isSeenFlag: Boolean

    constructor() {
        notificationMessage = ""
        emailFrom = ""
        dateTime = ""
        notificationType = 0
        isSeenFlag = false
    }

    constructor(
        notificationMessage: String,
        emailFrom: String,
        dateTime: String,
        notificationType: Int,
        seenFlag: Boolean
    ) {
        this.notificationMessage = notificationMessage
        this.emailFrom = emailFrom
        this.dateTime = dateTime
        this.notificationType = notificationType
        isSeenFlag = seenFlag
    }
}
