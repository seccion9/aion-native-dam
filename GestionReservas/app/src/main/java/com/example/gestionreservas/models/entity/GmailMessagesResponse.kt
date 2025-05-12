package com.example.gestionreservas.models.entity

data class GmailMessagesResponse(
    val messages: List<GmailMessage>?,
    val nextPageToken: String?,
    val resultSizeEstimate: Int
)
data class GmailMessage(
    val id: String,
    val threadId: String
)