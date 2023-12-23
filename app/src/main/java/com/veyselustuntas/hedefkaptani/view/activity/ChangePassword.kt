package com.veyselustuntas.hedefkaptani.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.veyselustuntas.hedefkaptani.databinding.ActivityChangePasswordBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ChangePassword : AppCompatActivity() {

    private lateinit var binding : ActivityChangePasswordBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var currentUser : FirebaseUser
    private var firebaseCurrentPassword = ""
    private var currentUserDocId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        binding.userNewPasswordEditText.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    val isTouchingDrawable = event.rawX >= (binding.userNewPasswordEditText.right - binding.userNewPasswordEditText.compoundDrawables[2].bounds.width())

                    if (isTouchingDrawable) {
                        togglePasswordVisibility(binding.userNewPasswordEditText)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }

        binding.userNewPasswordRepeatEditText.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    val isTouchingDrawable = event.rawX >= (binding.userNewPasswordRepeatEditText.right - binding.userNewPasswordRepeatEditText.compoundDrawables[2].bounds.width())

                    if (isTouchingDrawable) {
                        togglePasswordVisibility(binding.userNewPasswordRepeatEditText)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }


        runBlocking {
            async {
                getUserPassword()
                if(firebaseCurrentPassword.equals("")){
                    binding.userCurrentPasswordEditText.visibility = View.GONE
                }
            }.await()
        }



        println("oldumu bakalim :$firebaseCurrentPassword")
        println("docId ${currentUserDocId}")

    }


    fun changePassword(view:View){
        currentUser = auth.currentUser!!

        val newPassword = binding.userNewPasswordEditText.text.toString()
        val newPasswordRepeat = binding.userNewPasswordRepeatEditText.text.toString()

        if(currentUser != null){
            if(!newPassword.equals("") && !newPasswordRepeat.equals("")){
                if(!firebaseCurrentPassword.equals("")){
                    var currentPassword = binding.userCurrentPasswordEditText.text.toString()
                    if(firebaseCurrentPassword!!.equals(currentPassword)){
                        if(newPassword.equals(newPasswordRepeat)){
                            currentUser!!.updatePassword(newPassword)
                                .addOnSuccessListener { task ->
                                    val update = FirebaseFirestore.getInstance().collection("Users").document(currentUserDocId!!).update("userPassword",newPassword)
                                    update.addOnCompleteListener {task->
                                        if(task.isSuccessful){
                                            println("firestoredaki güncellendi")
                                            Log.e(TAG,task.isSuccessful.toString())
                                            Toast.makeText(this@ChangePassword,"Şifreniz Değiştirildi.",Toast.LENGTH_LONG).show()
                                            val intent = Intent(this@ChangePassword,UserDrawer::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else{
                                            println("firestoredaki güncellenemdi: ${task.exception}")
                                            Log.e(TAG,"HATA ${task.exception}")

                                        }

                                    }
                                }
                                .addOnFailureListener{
                                    Toast.makeText(this@ChangePassword,"${it.localizedMessage}",Toast.LENGTH_LONG).show()
                                    println("Şifre değişmedi: ${it.localizedMessage}")
                                }
                        }
                        else{
                            Toast.makeText(this@ChangePassword,"Şifreler Eşleşmiyor",Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(this@ChangePassword,"Eski parolanızı doğru girdiğinizden emin olun",Toast.LENGTH_LONG).show()
                    }

                }
                else{
                    if(newPassword.equals(newPasswordRepeat)){
                        currentUser!!.updatePassword(newPassword)
                            .addOnSuccessListener { task ->
                                val update = FirebaseFirestore.getInstance().collection("Users").document(currentUserDocId!!).update("userPassword",newPassword)
                                update.addOnCompleteListener {task->
                                    if(task.isSuccessful){
                                        println("firestoredaki güncellendi")
                                        Toast.makeText(this@ChangePassword,"Şifreniz Değiştirildi.",Toast.LENGTH_LONG).show()

                                        Log.e(TAG,task.isSuccessful.toString())

                                        val intent = Intent(this@ChangePassword,UserDrawer::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    else{
                                        println("firestoredaki güncellenemdi: ${task.exception}")
                                        Log.e(TAG,"HATA ${task.exception}")

                                    }

                                }
                            }
                            .addOnFailureListener{
                                Toast.makeText(this@ChangePassword,"${it.localizedMessage}",Toast.LENGTH_LONG).show()
                                println("Şifre değişmedi: ${it.localizedMessage}")
                            }
                    }
                    else{
                        Toast.makeText(this@ChangePassword,"Şifreler Eşleşmiyor",Toast.LENGTH_LONG).show()
                    }

                }
            }
            else{
                Toast.makeText(this@ChangePassword,"Lütfen Yeni Şifrenizi Girin.",Toast.LENGTH_LONG).show()

            }


        }
    }

    suspend private fun getUserPassword(){
        val deferred = CompletableDeferred<Unit>()
        CoroutineScope(Dispatchers.IO).launch {
            val docRef = FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo("userMailAddress", auth.currentUser!!.email).get().await()

            currentUserDocId = docRef.documents[0].id
            for (i in docRef) {
                firebaseCurrentPassword = i.get("userPassword") as String
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }

    private fun togglePasswordVisibility(editText: EditText) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        editText.setSelection(selectionStart, selectionEnd)
    }

}