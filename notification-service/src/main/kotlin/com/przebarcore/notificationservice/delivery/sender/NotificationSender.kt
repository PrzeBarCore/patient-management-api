package com.przebarcore.notificationservice.delivery.sender

import com.przebarcore.notificationservice.delivery.model.NotificationMessage

interface NotificationSender {
    fun send(notificationMessage: NotificationMessage)
}