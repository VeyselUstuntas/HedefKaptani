package com.veyselustuntas.hedefkaptani.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.veyselustuntas.hedefkaptani.databinding.ActivityUpdateMyInformationBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateMyInformation : AppCompatActivity() {
    private lateinit var binding : ActivityUpdateMyInformationBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private var userDisplayName : String = ""
    private var userMailAddress : String = ""
    private var userName : String = ""
    private var userSurname : String = ""
    private var userPhoneNumber : String = ""
    private var currentUserDocId : String = ""
    private val deferred = CompletableDeferred<Unit>()
    private lateinit var userTargetsArrayList : ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateMyInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userTargetsArrayList = ArrayList<String>()


        runBlocking {
            async {
                getUserInfo()
                getUserTargetsInfo()

            }.await()
        }

        binding.userDisplayNameEditText.setText(userDisplayName)
        binding.userMailAddressNameEditText.setText(userMailAddress)
        binding.userNameEditText.setText(userName)
        binding.userSurnameEditText.setText(userSurname)
        binding.userPhoneEditText.setText(userPhoneNumber)
        println(currentUserDocId)

        binding.updateButton.setOnClickListener { updateMyInfo() }

        binding.userMailAddressNameEditText.setOnClickListener {
            Toast.makeText(this@UpdateMyInformation,"Mail adresinizi değiştiremezsiniz.",Toast.LENGTH_LONG).show()
        }
    }

    fun updateMyInfo(){
        if(auth.currentUser != null){
            userDisplayName = binding.userDisplayNameEditText.text.toString()
            userMailAddress = binding.userMailAddressNameEditText.text.toString()
            userName = binding.userNameEditText.text.toString()
            userSurname = binding.userSurnameEditText.text.toString()
            userPhoneNumber = binding.userPhoneEditText.text.toString()


            val updatedHashMap = HashMap<String,String>()
            updatedHashMap.put("userDisplayName",userDisplayName)
            updatedHashMap.put("userName",userName)
            updatedHashMap.put("userSurname",userSurname)
            updatedHashMap.put("userPhoneNumber",userPhoneNumber)


            firestore.collection("Users").document(currentUserDocId).update(updatedHashMap as Map<String, Any>)
                .addOnSuccessListener {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userDisplayName
                    }
                    auth.currentUser!!.updateProfile(profileUpdates)
                        .addOnSuccessListener {

                            if(userTargetsArrayList.isNotEmpty()){
                                for(i in userTargetsArrayList){
                                    firestore.collection("Targets").document(i).update("userDisplayName",userDisplayName)
                                }
                            }
                            Toast.makeText(this@UpdateMyInformation,"Kullanıcı Bilgileriniz Güncellendi ",Toast.LENGTH_LONG).show()
                            finish()

                        }
                        .addOnFailureListener {
                            Toast.makeText(this@UpdateMyInformation,"Kullanıcı Bilgileriniz Güncellenemdi: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                            println("displayname Bilgileriniz Güncellenemedi${it.localizedMessage}")
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this@UpdateMyInformation,"Bilgileriniz Güncellenemdi: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                }
        }

    }

    suspend private fun getUserInfo(){
        val currentUser = auth.currentUser
        if(currentUser != null){
            CoroutineScope(Dispatchers.IO).launch {
                val docRef = firestore.collection("Users").get().await()

                val document = docRef.documents

                for(i in document){

                    val doc = i.data
                    if(doc != null){
                        if(!doc.isEmpty()){
                            userDisplayName = doc.get("userDisplayName") as String
                            userMailAddress = doc.get("userMailAddress") as String
                            if(auth.currentUser!!.email!!.equals(userMailAddress) && auth.currentUser!!.displayName!!.equals(userDisplayName)){
                                currentUserDocId = i.id
                                userName = doc.get("userName") as String
                                userSurname = doc.get("userSurname") as String
                                userPhoneNumber = doc.get("userPhoneNumber").toString()
                                break
                            }
                            else{
                                println("Kullanıcı Dökümanı Bulunamadı")
                            }
                        }
                    }
                }
                deferred.complete(Unit)
            }
            deferred.await()


        }

    }

    suspend private fun getUserTargetsInfo(){
        val currentUser = auth.currentUser
        userTargetsArrayList.clear()
        val deferred = CompletableDeferred<Unit>()
        CoroutineScope(Dispatchers.IO).launch{
            val docRef = firestore.collection("Targets").get().await()

            val document = docRef.documents

            for(doc in document){
                if(doc.exists() && doc!=null){
                    if(doc.get("userMailAddress")!!.equals(currentUser!!.email) && doc.get("userDisplayName")!!.equals(currentUser.displayName)){
                        userTargetsArrayList.add(doc.id)
                    }
                }
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }
}