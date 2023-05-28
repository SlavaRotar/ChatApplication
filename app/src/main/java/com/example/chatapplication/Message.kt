package com.example.chatapplication

class Message {
    var message: String? = null
    var senderId: String? = null

    constructor() {
        // Required empty constructor for Firebase deserialization
    }

    constructor(message: String?, senderId: String?) {
        this.message = message
        this.senderId = senderId
    }
}
