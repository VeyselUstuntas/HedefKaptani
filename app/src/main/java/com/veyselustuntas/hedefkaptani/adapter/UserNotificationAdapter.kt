package com.veyselustuntas.hedefkaptani.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.utils.GetUserNotification
import com.veyselustuntas.hedefkaptani.utils.Notification
import java.text.SimpleDateFormat
import java.util.Date

class UserNotificationAdapter(val userNotificationArray : ArrayList<GetUserNotification>) : RecyclerView.Adapter<UserNotificationAdapter.UserNotificationVH>() {
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    class UserNotificationVH(itemView : View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserNotificationVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_notification_recycler_row,parent,false)
        return UserNotificationVH(itemView)
    }

    override fun getItemCount(): Int {
        return userNotificationArray.size
    }

    override fun onBindViewHolder(holder: UserNotificationVH, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        holder.itemView.findViewById<TextView>(R.id.notificationTitleTextView).text = userNotificationArray.get(position).messageTitle
        holder.itemView.findViewById<TextView>(R.id.notificationBodyTextView).text = userNotificationArray.get(position).messageBody
        var notificationEventTime = userNotificationArray.get(position).messageSendTime
        val date = Date(notificationEventTime)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = dateFormat.format(date)
        holder.itemView.findViewById<TextView>(R.id.notificationEventTimeTextView).text = formattedDate
        val currentNotID = userNotificationArray.get(position).docId

        holder.itemView.findViewById<ImageButton>(R.id.deleteNotification).setOnClickListener {
            if(auth.currentUser != null){
                firestore.collection("Notifications").document(currentNotID).delete()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context,"Bildirim Silindi",Toast.LENGTH_LONG).show()
                        notifyDataSetChanged()
                    }
            }
        }
    }
}