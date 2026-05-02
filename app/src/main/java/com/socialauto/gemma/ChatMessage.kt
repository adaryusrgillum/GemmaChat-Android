package com.socialauto.gemma

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val isLoading: Boolean = false
)
