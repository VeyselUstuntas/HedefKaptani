package com.veyselustuntas.hedefkaptani.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.adapter.UserNotificationAdapter
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserNotificationBinding
import com.veyselustuntas.hedefkaptani.utils.GetUserNotification
import com.veyselustuntas.hedefkaptani.utils.Notification
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserNotification : Fragment() {
    private lateinit var _binding : FragmentUserNotificationBinding
    private val binding get () = _binding.root

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var currentUser : FirebaseUser
    private lateinit var  userNotificationArraylist : ArrayList<GetUserNotification>
    private lateinit var adapter : UserNotificationAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserNotificationBinding.inflate(inflater,container,false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userNotificationArraylist = ArrayList()
        currentUser = auth.currentUser!!

        _binding.swipeRefreshLayout.setOnRefreshListener {
            _binding.swipeRefreshLayout.isRefreshing = false
            lifecycleScope.launch{
                getUserNotification()
                adapter = UserNotificationAdapter(userNotificationArraylist)
                adapter.notifyDataSetChanged()
            }
            if(userNotificationArraylist.size == 0){
                _binding.userNotificationRecyclerView.visibility = View.GONE
                _binding.notificationNotFoundTextView.visibility = View.VISIBLE
            }
            else{
                _binding.userNotificationRecyclerView.visibility = View.VISIBLE
                _binding.notificationNotFoundTextView.visibility = View.GONE
                adapter = UserNotificationAdapter(userNotificationArraylist)
                _binding.userNotificationRecyclerView.layoutManager = LinearLayoutManager(context)
                _binding.userNotificationRecyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }

        lifecycleScope.launch {
            getUserNotification()
        }

        if(userNotificationArraylist.size == 0){
            _binding.userNotificationRecyclerView.visibility = View.GONE
            _binding.notificationNotFoundTextView.visibility = View.VISIBLE
        }
        else{
            _binding.userNotificationRecyclerView.visibility = View.VISIBLE
            _binding.notificationNotFoundTextView.visibility = View.GONE
            adapter = UserNotificationAdapter(userNotificationArraylist)
            _binding.userNotificationRecyclerView.layoutManager = LinearLayoutManager(context)
            _binding.userNotificationRecyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

    }





    suspend private fun getUserNotification(){
        val deferred = CompletableDeferred<Unit>()
        userNotificationArraylist.clear()
        CoroutineScope(Dispatchers.IO).launch{
            if(currentUser != null) {
                val document = firestore.collection("Notifications")
                    .whereEqualTo("userMailAddress", currentUser.email).get().await()
                if (document != null) {
                    if (!document.isEmpty) {
                        for (doc in document) {
                            val messageTitle = doc.get("notificationTitle").toString()
                            val messageBody = doc.get("notificationBody").toString()
                            val messageSendTime =
                                doc.get("notificationSendTime").toString().toLong()

                            val docId = doc.id
                            val notif = GetUserNotification(docId,messageTitle, messageBody, messageSendTime)
                            userNotificationArraylist.add(notif)
                            withContext(Dispatchers.Main){
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }
}