package com.veyselustuntas.hedefkaptani.service

import com.veyselustuntas.hedefkaptani.model.WeatherModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    //https://api.openweathermap.org/data/2.5/weather?q=ankara&units=metric&appid=f5cc34957246d11d5aaf35060662738c
    @GET("data/2.5/weather?units=metric&appid=f5cc34957246d11d5aaf35060662738c")
    fun getData(
        @Query("q") cityName : String
    ): Single<WeatherModel>

}