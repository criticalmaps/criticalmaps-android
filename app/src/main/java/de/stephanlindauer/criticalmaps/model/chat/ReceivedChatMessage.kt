package de.stephanlindauer.criticalmaps.model.chat

import java.util.Date

data class ReceivedChatMessage(
    val message: String,
    val timestamp: Date
)