package com.example.gestionreservas.models.entity

data class GmailMessageDetailResponse(
    val id: String,
    val threadId: String,
    val payload: Payload,
    val internalDate: String?
)

data class Payload(
    val headers: List<Header>,
    val body: Body?,
    val parts: List<PayloadPart>?
)

data class Header(
    val name: String,
    val value: String
)

data class Body(
    val size: Int,
    val data: String?
)

data class PayloadPart(
    val mimeType: String,
    val filename: String,
    val headers: List<Header>,
    val body: Body
)

