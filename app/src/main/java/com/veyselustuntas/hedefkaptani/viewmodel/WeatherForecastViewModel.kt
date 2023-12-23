package com.veyselustuntas.hedefkaptani.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veyselustuntas.hedefkaptani.model.WeatherModel
import com.veyselustuntas.hedefkaptani.service.WeatherService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class WeatherForecastViewModel : ViewModel() {
    val weatherModel = MutableLiveData<WeatherModel>()
    val weatherError = MutableLiveData<Boolean>()
    val weatherLoading = MutableLiveData<Boolean>()

    private val compositeDisposiable = CompositeDisposable()
    private val weatherService = WeatherService()


    fun refreshData(cityName : String){
        getDataFromAPI(cityName)

    }

    private fun getDataFromAPI(cityName : String){
        compositeDisposiable.add(
            weatherService.getApi(cityName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<WeatherModel>(){
                    override fun onSuccess(t: WeatherModel) {
                        weatherModel.postValue(t)
                        weatherError.postValue(false)
                        weatherLoading.postValue(false)
                    }

                    override fun onError(e: Throwable) {
                        weatherError.postValue(true)
                        weatherLoading.postValue(false)
                    }

                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposiable.clear()
    }
}