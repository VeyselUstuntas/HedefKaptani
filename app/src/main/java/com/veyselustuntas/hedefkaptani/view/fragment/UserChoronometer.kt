package com.veyselustuntas.hedefkaptani.view.fragment

import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserChoronometerBinding

class UserChoronometer : Fragment() {
    private lateinit var _binding: FragmentUserChoronometerBinding
    private val binding get() = _binding.root
    private lateinit var chronometer: Chronometer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserChoronometerBinding.inflate(inflater, container, false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chronometer = view.findViewById(R.id.chronometer)

        var running = false

        var stopTime : Long = 0
        var startTime : Long = 0
        var runningTime : Long = 0
        _binding.startButton.setOnClickListener {
            if(running){
                _binding.startButton.text = "START"
                running = false
                chronometer.stop()
                stopTime = SystemClock.elapsedRealtime()
                runningTime = chronometer.base - stopTime
            }
            else{
                _binding.startButton.text = "STOP"
                running = true
                chronometer.start()
                startTime = SystemClock.elapsedRealtime()
                chronometer.base = startTime + runningTime
            }
        }

        _binding.resetButton.setOnClickListener {
            running = false
            _binding.startButton.text = "START"
            runningTime = 0L
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.stop()

        }
    }
}