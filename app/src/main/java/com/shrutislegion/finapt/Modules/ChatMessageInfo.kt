package com.shrutislegion.finapt.Modules

data class ChatMessageInfo(

    var senderId: String? = null,
    var receiverId: String? = null,
    var message: String? = null,
    var messageTime: Long = 0

)
