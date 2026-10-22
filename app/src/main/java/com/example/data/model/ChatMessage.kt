package com.example.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val relatedRouteSummary: String? = null
)
