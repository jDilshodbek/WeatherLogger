package com.test.weatherlogger.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class WeatherCurrentDayResult(
    @SerializedName("main")
    @Expose
    val currentDay: CurrentDay
)

data class CurrentDay(
    @SerializedName("temp_min")
    @Expose
    val temp_min: Float,
    @SerializedName("temp_max")
    @Expose
    val temp_max: Float,
    @SerializedName("temp")
    @Expose
    val temperature: Float,
    @SerializedName("pressure")
    @Expose
    val pressure: Float,
    @SerializedName("humidity")
    @Expose
    val humidity: Float
)