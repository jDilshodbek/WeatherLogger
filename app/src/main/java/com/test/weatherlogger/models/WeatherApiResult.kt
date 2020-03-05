package com.test.weatherlogger.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WeatherApiResult (
    @SerializedName("city")
    @Expose
    val city: City,
    @SerializedName("list")
    @Expose
    val weatherDataList:List<WeatherData>
)

data class City(
    @SerializedName("id")
    @Expose
    val id:Int,
    @SerializedName("name")
    @Expose
    val name:String
)

data class WeatherData(
    @SerializedName("dt")
    @Expose
    val dt:Long,
    @SerializedName("main")
    @Expose
    val main: Main,
    @SerializedName("weather")
    @Expose
    val weatherList:List<Weather>

)

data class Main(
    @SerializedName("temp_min")
    @Expose
    val temp_min:Double,
    @SerializedName("temp_max")
    @Expose
    val temp_max:Double,
    @SerializedName("humidity")
    @Expose
    val humidity:Double

)

data class Weather(
    @SerializedName("id")
    @Expose
    val id:Int,
    @SerializedName("description")
    @Expose
    val description:String,
    @SerializedName("icon")
    @Expose
    val icon:String
)