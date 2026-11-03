package com.sktech.wastetrack.domain.model.notification

data class AgroNotification(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val urgencyBadge: String,
    val timestampText: String,
    val actionText: String? = null,
    val subsidyAmount: Double? = null,
    val isRead: Boolean = false
)

