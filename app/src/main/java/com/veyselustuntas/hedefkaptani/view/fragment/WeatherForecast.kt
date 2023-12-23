package com.veyselustuntas.hedefkaptani.view.fragment

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentWeatherForecastBinding
import com.veyselustuntas.hedefkaptani.model.WeatherModel
import com.veyselustuntas.hedefkaptani.viewmodel.WeatherForecastViewModel
import java.text.DecimalFormat

class WeatherForecast : Fragment() {
    private lateinit var _binding : FragmentWeatherForecastBinding
    private val binding get() = _binding.root
    private var weatherForecastViewModel = WeatherForecastViewModel()
    private var decimalFormatDegree = DecimalFormat("###.#")
    private lateinit var sharedPreferences : SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeatherForecastBinding.inflate(inflater,container,false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let{
            sharedPreferences = it.getSharedPreferences(it.packageName,Context.MODE_PRIVATE)
            val cName = sharedPreferences.getString("cityName","Ankara")
            if(cName != null)
                _binding.cityNameEditText.setText(cName)
            weatherForecastViewModel = ViewModelProvider(it).get(WeatherForecastViewModel::class.java)
            weatherForecastViewModel.refreshData(cName!!)
            obserLiveData()

        }
        _binding.swipeRefreshLayout.setOnRefreshListener {
            _binding.swipeRefreshLayout.isRefreshing = false
            val cityName = sharedPreferences.getString("cityName","ankara")
            weatherForecastViewModel.refreshData(cityName!!)
            obserLiveData()
        }


        _binding.citySearchImageView.setOnClickListener {
            val cityName = _binding.cityNameEditText.text.toString()
            sharedPreferences.edit().putString("cityName",cityName).apply()
            _binding.cityNameEditText.setText(cityName)
            weatherForecastViewModel.refreshData(cityName)
            obserLiveData()
        }

    }

    private fun obserLiveData(){
        weatherForecastViewModel.weatherModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                _binding.dataViewLinearLayout.visibility = View.VISIBLE
                _binding.degreeTextView.text = "${decimalFormatDegree.format(it.main.temp)}°C"
                _binding.humidityTextView.text = "%${it.main.humidity}"
                _binding.speedTextView.text = "${it.wind.speed} km/h"
                _binding.latTextView.text = it.coord.lat.toString()
                _binding.lonTextView.text = it.coord.lon.toString()
                _binding.countryCodeTextView.text = it.sys.country
                _binding.cityNameTextView.text = it.name
                var imageView : ImageView = _binding.weatherIconImageView
                val icon = it.weather.get(0).icon
                var imageUrl = "http://openweathermap.org/img/wn/$icon@2x.png"
                Picasso.get().load(imageUrl).into(imageView)
            }
        })

        weatherForecastViewModel.weatherError.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(it){
                    _binding.errorMessageTextView.visibility = View.VISIBLE
                    _binding.dataViewLinearLayout.visibility = View.GONE
                    _binding.progressBar.visibility = View.GONE
                }
                else{
                    _binding.errorMessageTextView.visibility = View.GONE
                }
            }
        })

        weatherForecastViewModel.weatherLoading.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(it){
                    _binding.progressBar.visibility = View.VISIBLE
                    _binding.dataViewLinearLayout.visibility = View.GONE
                    _binding.errorMessageTextView.visibility = View.GONE
                }
                else{
                    _binding.progressBar.visibility = View.GONE
                }
            }
        })
    }

}