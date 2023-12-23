package com.veyselustuntas.hedefkaptani.view.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentSignUpBinding
import java.lang.Exception


class SignUp : Fragment() {
    private lateinit var _binding : FragmentSignUpBinding
    private val binding get() = _binding.root
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    private var userName : String? = null
    private var userSurname : String? = null
    private var userMailAddress : String? = null
    private var userPassword : String? = null
    private var userPhoneNumber : Long? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSignUpBinding.inflate(inflater,container,false)
        _binding.signUpButton.setOnClickListener { signUp(it) }
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore



        _binding.userPasswordEditText.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    val isTouchingDrawable = event.rawX >= (_binding.userPasswordEditText.right - _binding.userPasswordEditText.compoundDrawables[2].bounds.width())

                    if (isTouchingDrawable) {
                        togglePasswordVisibility(_binding.userPasswordEditText)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }

    }

    private fun signUp(view:View){
        userName = _binding.userNameEditText.text.toString()
        userSurname = _binding.userSurnameEditText.text.toString()
        userMailAddress = _binding.userMailAddressEditText.text.toString()
        userPassword = _binding.userPasswordEditText.text.toString()
        userPhoneNumber = _binding.userPhoneNumberEditText.text.toString().toLongOrNull()

        if(!userMailAddress.equals("") && !userPassword.equals("") && !userName.equals("") && !userSurname.equals("") && userPhoneNumber!=null){
            auth.createUserWithEmailAndPassword(userMailAddress!!,userPassword!!)
                .addOnSuccessListener {
                    val user = Firebase.auth.currentUser
                    println(user?:"gelmedi")
                    var displayNickName = "${userName!!.lowercase()}${userSurname!!.lowercase()}"
                    val profileupdates = userProfileChangeRequest {
                        displayNickName = turkishToEnglish(displayNickName)
                        displayName = displayNickName
                    }
                    user!!.updateProfile(profileupdates)
                        .addOnCompleteListener {
                            if(it.isSuccessful)
                                Log.w(TAG,"Profil güncellendi")
                        }

                    val userHashMap = HashMap<String,Any>()
                    userHashMap.put("userMailAddress",userMailAddress!!)
                    userHashMap.put("userPassword",userPassword!!)
                    userHashMap.put("userName",userName!!)
                    userHashMap.put("userSurname",userSurname!!)
                    userHashMap.put("userPhoneNumber",userPhoneNumber!!)
                    userHashMap.put("userDisplayName",displayNickName)
                    userHashMap.put("userProfilePicture","")

                    firestore.collection("Users").add(userHashMap)
                        .addOnSuccessListener {
                            Toast.makeText(view.context,"Hesap Oluşturuldu Giriş Ekranına Yönlendiriliyorsunuz.",Toast.LENGTH_LONG).show()
                            val action = SignUpDirections.actionSignUpToSignIn()
                            Navigation.findNavController(view).navigate(action)
                        }
                        .addOnFailureListener {
                            //Toast.makeText(view.context,"${it.localizedMessage}",Toast.LENGTH_LONG).show()
                            println("Users Koleksiyonuna eklenemedi: ${it.localizedMessage}")

                        }
                }
                .addOnFailureListener {
                    //Toast.makeText(view.context,"${it.localizedMessage}",Toast.LENGTH_LONG).show()
                    println("Hesap Oluşturlamadı: ${it.localizedMessage}")
                    Toast.makeText(view.context,"${it.localizedMessage}",Toast.LENGTH_LONG).show()

                }

        }
        else{
            Toast.makeText(view.context,"Lütfen Kişisel Bilgilerinizi Eksiksiz Bir Şekilde Giriniz",Toast.LENGTH_LONG).show()
        }
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


    private fun turkishToEnglish(input: String): String {
        val turkceKarakterler = "ğĞüÜşŞıİöÖçÇ"
        val ingilizceKarakterler = "gGuUsSiIoOcC"

        val charArray = input.toCharArray()

        for (i in charArray.indices) {
            val index = turkceKarakterler.indexOf(charArray[i])
            if (index != -1) {
                charArray[i] = ingilizceKarakterler[index]
            }
        }

        return String(charArray)
    }

}