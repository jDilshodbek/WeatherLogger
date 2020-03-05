package com.test.weatherlogger.net

import com.test.weatherlogger.models.WeatherApiResult
import com.test.weatherlogger.models.WeatherCurrentDayResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface IRestApi {

    @GET("/data/2.5/weather")
    fun getCurrentDayWeather(@Query("q") cityName:String):Call<WeatherCurrentDayResult>

    @GET("/data/2.5/forecast")
    fun getWeather(@Query("q") cityName:String):Call<WeatherApiResult>




}