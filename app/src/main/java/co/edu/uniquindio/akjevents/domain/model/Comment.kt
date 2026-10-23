package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

data class Comment(
    val id: String,
    val eventId: String,
    val authorId: String,
    val text: String,
    val createdAt: LocalDateTime
)
