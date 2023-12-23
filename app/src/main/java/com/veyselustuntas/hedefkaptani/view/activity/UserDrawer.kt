package com.veyselustuntas.hedefkaptani.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.veyselustuntas.hedefkaptani.databinding.ActivityUserDrawerBinding
import com.veyselustuntas.hedefkaptani.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class UserDrawer : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityUserDrawerBinding

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher : ActivityResultLauncher<String>

    private var userPhotoImageView : ImageView? = null
    private var userPhotoImageUri : Uri? = null
    private var userPicture : String? = null
    private var userNickName : String = ""
    private var userMailAddress: String = ""
    private lateinit var currentUser : FirebaseUser

    private var userNameSurnameTextView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarUserDrawer.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_user_drawer)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_myGoals, R.id.nav_myProgress, R.id.nav_notification
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        userNameSurnameTextView = headerView.findViewById<TextView>(R.id.userNameSurnameTextView)
        userPhotoImageView = headerView.findViewById<ImageView>(R.id.userPhotoImageView)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUser = auth.currentUser!!

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        resultLauncher()


        if(currentUser!=null){
            runBlocking {
                launch(Dispatchers.IO) {
                    async {
                        userControl()
                    }.await()
                }
            }

            lifecycleScope.launch {
                loadUserImage()
                val userPictureImageView = headerView.findViewById<ImageView>(R.id.userPhotoImageView)
                if(!userPicture.equals(""))
                    Picasso.get().load(userPicture).into(userPictureImageView)
                else
                    userPictureImageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }
        //println("${userNickName} ${userMailAddress}")
        //println("currentUser: ${currentUser.displayName!!} ${currentUser.email}")
        Toast.makeText(this,"Hoş Geldin ${currentUser.displayName}",Toast.LENGTH_LONG).show()
        userNameSurnameTextView!!.text = userNickName

    }

    override fun onRestart() {
        super.onRestart()
        resultLauncher()
        if(currentUser!=null){
            runBlocking {
                launch(Dispatchers.IO) {
                    async {
                        userControl()
                    }.await()

                    async {
                        loadUserImage()
                    }.await()
                }
            }
        }
        userNameSurnameTextView!!.setText(userNickName)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.user_drawer, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        currentUser = auth.currentUser!!
        if(item.itemId == R.id.logOutAccountMenuToolbar){
            if(currentUser != null){
                auth.signOut()
                mGoogleSignInClient.signOut()
                val intent = Intent(this@UserDrawer,MainActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
        else if (item.itemId == R.id.changePassworMenuToolbar){
            val intent = Intent(this@UserDrawer,ChangePassword::class.java)
            startActivity(intent)

        }
        else if(item.itemId == R.id.updateMyInformationMenuToolbar){
            val intent = Intent(this@UserDrawer, UpdateMyInformation::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_user_drawer)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    fun selectPhoto(view: View){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU ){
            if(ContextCompat.checkSelfPermission(this@UserDrawer,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@UserDrawer,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"İzin Verilmemiş İzin Verilsin mi?",Snackbar.LENGTH_INDEFINITE).setAction("Evet"){
                        permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                    }.show()
                }
                else{
                    permissionResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }
            }
            else{
                val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)

            }
        }
        else{
            if(ContextCompat.checkSelfPermission(this@UserDrawer,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this@UserDrawer,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"İzin Verilmemiş İzin Verilsin mi?",Snackbar.LENGTH_INDEFINITE).setAction("Evet"){
                        permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }.show()
                }
                else{
                    permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else{
                val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)
            }

        }

    }

    private fun resultLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result!=null){
                if(result.resultCode == RESULT_OK){
                    val intentToGallery = result.data
                    if(intentToGallery!=null){
                        val imageUri = intentToGallery!!.data
                        if(imageUri!=null){
                            if(userPhotoImageView!=null){
                                userPhotoImageUri = imageUri
                                if(Build.VERSION.SDK_INT > 28){
                                    val imageDecoder = ImageDecoder.createSource(this.contentResolver,imageUri)
                                    userPhotoImageView!!.setImageBitmap(ImageDecoder.decodeBitmap(imageDecoder))

                                    runBlocking {
                                        launch {
                                            async {
                                                uploadUserImageFirebase()
                                            }.await()
                                        }
                                    }
                                }
                                else{
                                    userPhotoImageView!!.setImageBitmap(MediaStore.Images.Media.getBitmap(this.contentResolver,imageUri))
                                    runBlocking {
                                        launch {
                                            async {
                                                uploadUserImageFirebase()
                                            }.await()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result!=null){
                if(result){
                    val intentGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentGallery)

                }
                else{
                    Toast.makeText(this,"İzin Verilmedi",Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    suspend private fun userControl() {
        currentUser = auth.currentUser!!
        val deferred = CompletableDeferred<Unit>()
        var state : Boolean = false
        CoroutineScope(Dispatchers.IO).launch {
            val doc = firestore.collection("Users").get().await()
            if(currentUser != null){
                for (i in doc) {
                    if(currentUser.displayName.equals(i.get("userDisplayName").toString()) && currentUser.email!!.equals(i.get("userMailAddress").toString())){
                        userNickName = i.get("userDisplayName") as String
                        userMailAddress = i.get("userMailAddress") as String
                        state = true
                        break
                    }
                    else{
                        userNickName = "null"
                        userMailAddress ="null"
                    }
                }
                if(!state){
                    val userRegis = HashMap<String,Any>()
                    userRegis.put("userDisplayName",currentUser.displayName!!)
                    userRegis.put("userMailAddress",currentUser.email!!)
                    userRegis.put("userName","")
                    userRegis.put("userSurname","")
                    userRegis.put("userProfilePicture","")
                    userRegis.put("userPhoneNumber","")
                    userRegis.put("userPassword","")




                    firestore.collection("Users").add(userRegis)
                        .addOnCompleteListener{

                        }
                        .addOnFailureListener{
                            println(it.localizedMessage)

                        }
                }
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }

    suspend private fun uploadUserImageFirebase(){
        currentUser = auth.currentUser!!
        if(userPhotoImageUri != null){
            val deferred = CompletableDeferred<Unit>()

            val uuid = UUID.randomUUID()
            val imageName = "${uuid}.png"
            val referance = storage.reference.child("userpp").child(imageName)

            referance.putFile(userPhotoImageUri!!)
                .addOnSuccessListener {
                    val uploadImage = storage.reference.child("userpp").child(imageName)
                    uploadImage.downloadUrl.addOnCompleteListener {
                        val downloadUri = it.result

                        CoroutineScope(Dispatchers.IO).launch {
                            val userDocRef = FirebaseFirestore.getInstance().collection("Users").whereEqualTo("userMailAddress",currentUser!!.email).get().await()

                            if(!userDocRef.isEmpty){
                                val docId = userDocRef.documents[0].id
                                FirebaseFirestore.getInstance().collection("Users").document(docId).update("userProfilePicture",downloadUri).await()
                                withContext(Dispatchers.Main){
                                    Toast.makeText(applicationContext, "Profil Fotoğrafı Güncellendi.", Toast.LENGTH_LONG).show()

                                }
                            }
                        }
                    }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext, "Profil Fotoğrafı Güncellenemedi!: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    println("Resim Storage Yüklenemedi: ${it.localizedMessage}")
                }
            deferred.complete(Unit)
            deferred.await()
        }
    }

    suspend private fun loadUserImage(){
        currentUser = auth.currentUser!!
        val deferred = CompletableDeferred<Unit>()
        CoroutineScope(Dispatchers.IO).launch {
            val reference = firestore.collection("Users").whereEqualTo("userMailAddress",currentUser!!.email).get().await()

            for(i in reference){
                userPicture = i.get("userProfilePicture") as String
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }



}