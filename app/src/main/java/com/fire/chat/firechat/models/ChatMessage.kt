package com.fire.chat.firechat.models


class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String,val file:String, val timestamp: Long) {
    constructor() : this("", "", "", "","", -1)
}