package com.veyselustuntas.hedefkaptani.view.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.provider.CalendarContract.CalendarEntity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.adapter.UserGoalAdapter
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserGoalsBinding
import com.veyselustuntas.hedefkaptani.utils.Target
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.Year
import java.util.Calendar
import java.util.Locale
import javax.annotation.meta.When

class UserGoals : Fragment() {
    private lateinit var _binding : FragmentUserGoalsBinding
    private val binding get() = _binding.root
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var currentUser : FirebaseUser
    private lateinit var arrayOfTarget : ArrayList<Target>
    private lateinit var userGoalAdapter : UserGoalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserGoalsBinding.inflate(inflater,container,false)
        _binding.goalFilterImageButton.setOnClickListener { goalFilter(it) }
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        currentUser = auth.currentUser!!
        arrayOfTarget = ArrayList()

        _binding.swipeRefreshLayout.setOnRefreshListener {
            arrayOfTarget.clear()
            _binding.swipeRefreshLayout.isRefreshing = false
            lifecycleScope.launch {
                getCurrentUserGoal("Yok")
            }
            userGoalAdapter.notifyDataSetChanged()
        }


        _binding.fab.setOnClickListener {
            showCustomDialog()
        }

        lifecycleScope.launch {
            getCurrentUserGoal("Yok")
        }
        userGoalAdapter = UserGoalAdapter(arrayOfTarget)
        _binding.userGoalsRecyclerView.layoutManager = LinearLayoutManager(context)
        _binding.userGoalsRecyclerView.adapter = userGoalAdapter

    }

    private fun goalFilter(view:View){
        showFilterOptionsMenu(view)

    }

    private fun showFilterOptionsMenu(view:View){
        val popUpMenu : PopupMenu = PopupMenu(view.context,view)
        popUpMenu.inflate(R.menu.category_menu)
        popUpMenu.setOnMenuItemClickListener {item ->

            CoroutineScope(Dispatchers.IO).launch {
                when(item.itemId){
                    R.id.category_none -> getCurrentUserGoal("Yok")
                    R.id.category_celebrate -> getCurrentUserGoal("Kutla")
                    R.id.category_do -> getCurrentUserGoal("Yap")
                    R.id.category_quit -> getCurrentUserGoal("Bırak")
                    R.id.category_habit -> getCurrentUserGoal("Alışkanlık")

                }
            }
            true
        }
        popUpMenu.show()

    }


    private fun showCustomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_goal_alert_dialog,null)
        // açılır dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        //---
        val spinner :Spinner = dialogView.findViewById(R.id.categorySpinner)
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),R.array.category,android.R.layout.simple_spinner_item)
        spinnerAdapter.also {adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = spinnerAdapter
        //---

        val calendar = Calendar.getInstance()
        val targetName: EditText = dialogView.findViewById(R.id.targetNameEditText)
        val targetDate: EditText = dialogView.findViewById(R.id.targetDateEditText)
        val targetTime : EditText = dialogView.findViewById(R.id.targetTimeEditText)
        val addTarget: Button = dialogView.findViewById(R.id.saveTarget)
        var selectedDate : String = ""
        var selectedTime : String = ""
        var selectedCategory : String = ""

        targetDate.setOnClickListener {
            val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "dd/MM/yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                targetDate.setText(sdf.format(calendar.time))
                selectedDate = sdf.format(calendar.time)

            }
            DatePickerDialog(
                it.context, datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        targetTime.setOnClickListener {
            val timePicker = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                calendar.set(Calendar.HOUR,hourOfDay)
                calendar.set(Calendar.MINUTE,minute)
                val myFormat = "HH:mm"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                selectedTime =sdf.format(calendar.timeInMillis)

                val clockBeforeDel = selectedTime!!.substringBefore(":")
                val clockAfterDel = selectedTime!!.substringAfter(":")

                var clock = clockBeforeDel.toInt()
                if(clock<12 && clock>=0){
                    clock += 12
                }
                else if(clock<=23){
                    clock -= 12
                }

                if(clock<=9)
                    selectedTime = "0$clock:$clockAfterDel"
                else
                    selectedTime = "$clock:$clockAfterDel"

                targetTime.setText(selectedTime)
            }
            TimePickerDialog(it.context,timePicker, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = parent!!.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        addTarget.setOnClickListener {view->
            if(!selectedCategory.equals("") && !selectedDate.equals("") && !selectedTime.equals("") && !targetName.text.toString().equals("")) {
                if(auth.currentUser != null){
                    val userGoalHashMap = hashMapOf<String,Any>(
                        "userDisplayName" to auth.currentUser!!.displayName!!,
                        "userMailAddress" to auth.currentUser!!.email!!,
                        "goalCategory" to selectedCategory,
                        "goalName" to targetName.text.toString(),
                        "goalDate" to selectedDate,
                        "goalTime" to selectedTime,
                    )

                    firestore.collection("Targets").add(userGoalHashMap)
                        .addOnSuccessListener {
                            Toast.makeText(context,"Yeni Hedef Eklendi",Toast.LENGTH_LONG).show()
                            arrayOfTarget.clear()
                            _binding.swipeRefreshLayout.isRefreshing = false
                            lifecycleScope.launch {
                                getCurrentUserGoal("Yok")
                            }
                            userGoalAdapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context,"Yeni Hedef Eklenmedi daha sonra tekrar deneyin",Toast.LENGTH_LONG).show()
                            println("hedef eklenemedi ${it.localizedMessage}")
                        }
                }
                // Dialog'u kapat
                dialog.dismiss()
            }
            else
                Toast.makeText(context,"Lütfen Bütün Bilgileri Girin",Toast.LENGTH_LONG).show()
        }
        dialog.show()//popUp dilog göster
    }

    private suspend fun getCurrentUserGoal(category:String) {
        withContext(Dispatchers.Main){
            _binding.userGoalsRecyclerView.visibility = View.VISIBLE
            _binding.notFoundTargetTextView.visibility = View.GONE
        }
        arrayOfTarget.clear()
        var docRef : QuerySnapshot? = null
        if(category.equals("Yok")){
            docRef = firestore.collection("Targets").get().await()
        }
        else{
            docRef = firestore.collection("Targets").whereEqualTo("goalCategory",category).get().await()
        }

        if(docRef != null && !docRef.isEmpty()){

            var status = false
            for (document in docRef.documents) {
                if (document.get("userDisplayName") == currentUser.displayName && document.get("userMailAddress") == currentUser.email) {
                    status = true
                    if(document.exists() && document!=null){
                        val targetCategory = document.get("goalCategory").toString()
                        val targetName = document.get("goalName").toString()
                        val targetDate = document.get("goalDate").toString()
                        val targetTime = document.get("goalTime").toString()

                        withContext(Dispatchers.Main) {
                            val targetObject = Target(targetCategory, targetName, targetDate, targetTime, document.id)
                            arrayOfTarget.add(targetObject)

                            // Bu kısmı ekledim: Eklendikçe yazdır
                            println("Hedef eklendi: $targetName, $targetCategory, $targetDate, $targetTime, ${document.id}")
                            userGoalAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            if (!status){
                withContext(Dispatchers.Main){
                    arrayOfTarget.clear()
                    userGoalAdapter.notifyDataSetChanged()
                    _binding.userGoalsRecyclerView.visibility = View.INVISIBLE
                    _binding.notFoundTargetTextView.visibility = View.VISIBLE
                    println("silbaştan")
                }
            }

        }
        else{
            withContext(Dispatchers.Main){
                _binding.userGoalsRecyclerView.visibility = View.INVISIBLE
                _binding.notFoundTargetTextView.visibility = View.VISIBLE
            }
        }


    }



}