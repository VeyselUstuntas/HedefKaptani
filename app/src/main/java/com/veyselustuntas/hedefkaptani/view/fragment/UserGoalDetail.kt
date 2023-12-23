package com.veyselustuntas.hedefkaptani.view.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.LocaleData
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserGoalDetailBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale


class UserGoalDetail : Fragment() {

    private lateinit var _binding : FragmentUserGoalDetailBinding
    private val binding get() = _binding.root
    private var currentDocumentId : String? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private var numberOfDayElaps : String? = null
    private var targetName : String? = null
    private var targetCategory : String? = null
    private var targetDate : String? = null
    private var targetTime : String? = null
    private lateinit var spinner : Spinner
    private lateinit var calendar : Calendar
    private var updatedCategory : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserGoalDetailBinding.inflate(inflater,container,false)
        _binding.updateTargetButton.setOnClickListener { updateTarget(it) }
        _binding.deleteTargetButton.setOnClickListener { deleteTarget(it) }
        return binding.rootView
    }


    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = Firebase.auth
        firestore = Firebase.firestore
        calendar = Calendar.getInstance()
        spinner = view.findViewById(R.id.targetCategorySpinner)
        val spinnerArray = arrayListOf<String>("Alışkanlık","Bırak","Kutla","Yap")
        val spinnerAdapter = ArrayAdapter(view.context,android.R.layout.simple_spinner_item,spinnerArray)
        spinnerAdapter.also {adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = spinnerAdapter

        arguments?.let {
            currentDocumentId = UserGoalDetailArgs.fromBundle(it).documentId

        }

        runBlocking {
            async{
                getCurrentUserTargetInfo()
            }.await()
        }
        //println(targetName)
        _binding.targetNameEditText.setText(targetName)
        _binding.targetDateEditText.setText(targetDate)
        _binding.targetTimeEditText.setText(targetTime)

        spinnerArray.forEach {
            if(targetCategory!!.equals(it)){
                spinner.setSelection(spinnerArray.indexOf(it))
            }
        }

        _binding.targetDateEditText.setOnClickListener {
            val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR,year)
                calendar.set(Calendar.MONTH,month)
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                _binding.targetDateEditText.setText(sdf.format(calendar.time))

            }
            DatePickerDialog(
                it.context, datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        _binding.targetTimeEditText.setOnClickListener {
            val timePicker = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay)
                calendar.set(Calendar.MINUTE,minute)

                val sdf = SimpleDateFormat("HH:mm",Locale.getDefault())
                _binding.targetTimeEditText.setText(sdf.format(calendar.time))
            }
            TimePickerDialog(
                it.context, timePicker,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updatedCategory = parent!!.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                updatedCategory = targetCategory
            }

        }
        var date = Calendar.getInstance().time
        val format = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(format,Locale.getDefault())
        var currentDate = dateFormat.format(date)

        var targetDateFormatDay = dateFormat.parse(targetDate).time / (1000*60*60*24)
        var currentDateFormatDay = dateFormat.parse(currentDate).time / (1000*60*60*24)

        if(targetDateFormatDay>currentDateFormatDay){
            val elapsed = targetDateFormatDay - currentDateFormatDay
            _binding.tagetStateTextView.text = "Hedefinizin Başlamasına $elapsed Gün Kaldı"
        }
        else{
            val elapsed = currentDateFormatDay - targetDateFormatDay
            _binding.tagetStateTextView.text = "Hedefinizin Üzerinden $elapsed Gün Geçti"
        }




    }

    private fun updateTarget(view:View){
        targetName = _binding.targetNameEditText.text.toString()
        targetDate = _binding.targetDateEditText.text.toString()
        targetTime = _binding.targetTimeEditText.text.toString()
        val currentUser = auth.currentUser
        if(currentUser != null){
            if(!updatedCategory.equals("") && !targetName.equals("") && !targetDate.equals("") && !targetTime.equals("") && !currentDocumentId.equals("")) {
                val updatedTargetHashMap = hashMapOf<String,String>(
                    "goalCategory" to updatedCategory.toString(),
                    "goalName" to targetName.toString(),
                    "goalDate" to targetDate.toString(),
                    "goalTime" to targetTime.toString()
                )
                val action = UserGoalDetailDirections.actionUserGoalDetailToNavMyGoals()

                firestore.collection("Targets").document(currentDocumentId!!).update(
                    updatedTargetHashMap as Map<String, Any>
                )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Hedef Bilgileriniz Güncellendi", Toast.LENGTH_LONG).show()
                        Navigation.findNavController(view).navigate(action)
                    }
                    else{
                        Toast.makeText(context, "Hedef Bilgileriniz Güncellenemedi Daha sonra tekrar deneyin", Toast.LENGTH_LONG).show()
                        Navigation.findNavController(view).navigate(action)
                    }
                }
            }
        }
    }

    private fun deleteTarget(view:View){
        val currentUser = auth.currentUser
        if(currentUser != null){
            if(currentDocumentId != null){
                val action = UserGoalDetailDirections.actionUserGoalDetailToNavMyGoals()
                firestore.collection("Targets").document(currentDocumentId!!).delete()
                    .addOnSuccessListener {
                        Toast.makeText(view.context,"Hefediniz Silindi",Toast.LENGTH_LONG).show()
                        Navigation.findNavController(view).navigate(action)
                    }
                    .addOnFailureListener {
                        Toast.makeText(view.context,"Hefediniz Silinemedi. Daha Sonra Tekrar Deneyin.",Toast.LENGTH_LONG).show()
                        Navigation.findNavController(view).navigate(action)

                    }
            }
        }
    }


    suspend private fun getCurrentUserTargetInfo(){
        val deferred = CompletableDeferred<Unit>()
        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = auth.currentUser
            if(currentUser != null){
                if(currentDocumentId != null){
                    val docRef = firestore.collection("Targets").document(currentDocumentId!!).get().await()

                    val document = docRef.data
                    if(document != null){
                        if(!document.isEmpty()){
                            targetName = document.get("goalName").toString()
                            targetCategory = document.get("goalCategory").toString()
                            targetDate = document.get("goalDate").toString()
                            targetTime = document.get("goalTime").toString()
                        }
                    }
                }
            }
            deferred.complete(Unit)
        }
        deferred.await()
    }
}