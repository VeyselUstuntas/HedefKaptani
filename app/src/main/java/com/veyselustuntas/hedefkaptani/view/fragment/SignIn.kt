package com.veyselustuntas.hedefkaptani.view.fragment

import android.content.ContentValues.TAG
import android.content.Intent
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentSignInBinding
import com.veyselustuntas.hedefkaptani.view.activity.UserDrawer
import java.lang.Exception

class SignIn : Fragment() {
    private lateinit var _binding:FragmentSignInBinding
    private val binding get() = _binding.root

    private lateinit var auth : FirebaseAuth
    private var userMailAddress : String? = null
    private var userPassword : String? = null

    //-------------------
    companion object{
        private const val RC_SIGN_IN = 9001
    }
    //-------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSignInBinding.inflate(inflater,container,false)
        _binding.signUpClickTextView.setOnClickListener { signUp(it) }
        _binding.signInButton.setOnClickListener { signIn(it) }
        _binding.googleImageButton.setOnClickListener { signInWithGoogle(it) }
        _binding.xImageButton.setOnClickListener { signInWithTwitter(it) }
        _binding.microsoftImageButton.setOnClickListener { signInWithMicrosoft(it) }
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        val userPasswordEditText : EditText = view.findViewById(R.id.userPasswordEditText)
        userPasswordEditText.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    val isTouchingDrawable = event.rawX >= (userPasswordEditText.right - userPasswordEditText.compoundDrawables[2].bounds.width())

                    if (isTouchingDrawable) {
                        togglePasswordVisibility(userPasswordEditText)
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
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

    private fun signIn(view:View){
        userMailAddress = _binding.userNameEditText.text.toString()
        userPassword = _binding.userPasswordEditText.text.toString()

        if(!userMailAddress.equals("") && !userPassword.equals("")){
            try {
                auth.signInWithEmailAndPassword(userMailAddress!!,userPassword!!)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            val user = auth.currentUser
                            activity?.let {
                                val intent = Intent(it,UserDrawer::class.java)
                                it.startActivity(intent)
                                it.finish()
                            }
                        }
                    }
                    .addOnFailureListener {
                        println("Giriş Yapılırken Sorun Oluştu: ${it.localizedMessage}")
                        Toast.makeText(view.context,"${it.localizedMessage}",Toast.LENGTH_LONG).show()
                    }

            }
            catch (e : Exception){
                Log.e(TAG,"SignIn Fail: ${e.message}")
                println(e.localizedMessage)
                println(e.message)
            }
        }
        else{
            Toast.makeText(view.context,"Lütfen Giriş Bilgileriniz Eksiksiz Giriniz.",Toast.LENGTH_LONG).show()
        }


    }

    private fun signUp(view:View){
        val action = SignInDirections.actionSignInToSignUp()
        Navigation.findNavController(view).navigate(action)

    }


    //-------------
    private fun signInWithGoogle(view:View){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(view.context,gso)
        val signInIntent = googleSignInClient.signInIntent
        activity?.let {
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            }
            catch (e:Exception){
                Toast.makeText(view?.context, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    activity?.let {
                        val user = auth.currentUser
                        Toast.makeText(view?.context, "Oturum Açma Başarılı", Toast.LENGTH_SHORT).show()
                        val intent = Intent(it.applicationContext,UserDrawer::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                }
                else { Toast.makeText(view?.context, "Authentication failed", Toast.LENGTH_SHORT).show() }
            }
    }

    //-------------


    //-------------twitter
    private fun signInWithTwitter(view:View){
        val provider = OAuthProvider.newBuilder("twitter.com")
        provider.addCustomParameter("lang","tr")

        val pendingResultTask = auth.pendingAuthResult
        val currentUser = auth.currentUser

        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {
                    activity?.let {
                        val intent = Intent(it.applicationContext,UserDrawer::class.java)
                        it.startActivity(intent)
                        it.finish()
                        Toast.makeText(view.context,"Oturum Açma Başarılı.",Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(view.context,"Oturum Açma Başarısız: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                }
        } else {
            activity?.let {
                auth
                    .startActivityForSignInWithProvider(it, provider.build())
                    .addOnSuccessListener {
                        activity?.let {
                            val intent = Intent(it.applicationContext,UserDrawer::class.java)
                            it.startActivity(intent)
                            it.finish()
                            Toast.makeText(view.context,"Oturum Açma Başarılı.",Toast.LENGTH_LONG).show()

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(view.context,"Oturum Açma Başarısız: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                    }
            }

        }

    }

    //-------------


    //-------------microsoft
    private fun signInWithMicrosoft(view:View){
        val provider = OAuthProvider.newBuilder("microsoft.com")

        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener {
                    activity?.let {
                        val intent = Intent(it.applicationContext,UserDrawer::class.java)
                        it.startActivity(intent)
                        it.finish()
                        Toast.makeText(view.context,"Oturum Açma Başarılı",Toast.LENGTH_LONG).show()

                    }
                }
                .addOnFailureListener {
                    Toast.makeText(view.context,"Oturum Açma Başarısız: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                    println(it.localizedMessage)

                }
        } else {
            activity?.let {
                auth
                    .startActivityForSignInWithProvider(it, provider.build())
                    .addOnSuccessListener {
                        activity?.let {
                            val intent = Intent(it.applicationContext,UserDrawer::class.java)
                            it.startActivity(intent)
                            it.finish()
                            Toast.makeText(view.context,"Oturum Açma Başarılı",Toast.LENGTH_LONG).show()

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(view.context,"Oturum Açma Başarısız: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
                        println(it.localizedMessage)
                    }
            }

        }

    }

}