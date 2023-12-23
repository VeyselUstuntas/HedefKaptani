package com.veyselustuntas.hedefkaptani.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        createNotificationChannel()
        // Bildirimi işleme al

        val notificationTitle = message.notification?.title
        val notificationBody = message.notification?.body
        val notificationTime = message.notification?.eventTime


        if(notificationBody != null && notificationTitle != null){
            val notification = Notification(notificationTitle,notificationBody,notificationTime?:-1)
            sendNotificationFirestore(notification)
        }
        else{
        }

    }


    private fun sendNotificationFirestore(notification : Notification ){
        auth = Firebase.auth
        firestore = Firebase.firestore
        val currentUser = auth.currentUser
        if(currentUser != null){
            val currentUserEmail = currentUser.email
            val notificationHashMap = hashMapOf<String,String>(
                "userMailAddress" to currentUserEmail!!,
                "notificationTitle" to notification.messageTitle,
                "notificationBody" to notification.messageBody,
                "notificationSendTime" to notification.messageSendTime.toString())

            firestore.collection("Notifications").add(notificationHashMap)
                .addOnSuccessListener {
                    println("BİLDİRİMLER KAYDEDİLDİ")
                }
                .addOnFailureListener {
                    println("BİLDİRİMLER KAYDEDİLMEDİ!!!!")

                }
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "200707048",
                "vustuntas_hedef_kaptani",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}