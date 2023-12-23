package com.veyselustuntas.hedefkaptani.view.fragment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserStepCounterBinding

class UserStepCounter : Fragment(), SensorEventListener {
    private lateinit var _binding : FragmentUserStepCounterBinding
    private val binding get() = _binding.root

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepCount = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserStepCounterBinding.inflate(inflater,container,false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SensorManager ve adım sensörünü başlat
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Adım sayısını güncelle
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            updateStepCount()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Sensor doğruluğu değiştiğinde
    }

    private fun updateStepCount() {
        // Adım sayısını ekranda göster
        _binding.stepCountTextView.text = "Adım Sayısı: $stepCount"
    }

    override fun onResume() {
        super.onResume()
        // Sensor dinleme işlemini başlat
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Sensor dinleme işlemini durdur
        stepSensor?.let {
            sensorManager.unregisterListener(this, it)
        }
    }

}