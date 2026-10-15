package com.przebarcore.notificationservice.delivery.sender

import com.przebarcore.notificationservice.dto.NotificationDto

interface NotificationSender {
    fun send(notificationDto: NotificationDto)
}