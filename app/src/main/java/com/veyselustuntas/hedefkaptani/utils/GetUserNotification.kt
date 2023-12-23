package com.veyselustuntas.hedefkaptani.utils

data class GetUserNotification(
    val docId : String,
    val messageTitle : String,
    val messageBody : String,
    val messageSendTime : Long
)