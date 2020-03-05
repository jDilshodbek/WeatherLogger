package com.test.weatherlogger

import Prefs
import Prefs.get
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.test.weatherlogger.models.WeatherData
import com.test.weatherlogger.utils.Constants.CITY
import com.test.weatherlogger.utils.Constants.HUMIDITY
import com.test.weatherlogger.utils.Constants.MAX_TEMP
import com.test.weatherlogger.utils.Constants.MIN_TEMP
import com.test.weatherlogger.utils.Constants.PRESSURE
import com.test.weatherlogger.utils.Constants.TEMPERATURE
import com.test.weatherlogger.utils.Constants.WEATHER_DATA
import kotlinx.android.synthetic.main.activity_details.*


class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        setSupportActionBar(toolbarDetails)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val preferences=Prefs.getInstance(this)

        city.text=preferences[CITY,""]
        temp.text=preferences[TEMPERATURE,0F].toString()
        minTemp.text=preferences[MIN_TEMP,0F].toString()
        maxTemp.text=preferences[MAX_TEMP,0F].toString()
        humidity.text=preferences[HUMIDITY,0F].toString()
        pressure.text=preferences[PRESSURE,0F].toString()

        val stringList:String=preferences[WEATHER_DATA,""]!!
        // convert to list saved weather data and set it to recyclerview adapter
        val weatherType = object : TypeToken<List<WeatherData>>() {}.type

        val dataList= Gson().fromJson<List<WeatherData>>(stringList, weatherType)

        val newList= mutableListOf<WeatherData>()

        for (i in 0 until dataList.size / 8) {
            newList.add(dataList[i * 8])
        }

        val adapter=WeatherAdapter(newList)
        rvWeather.layoutManager= LinearLayoutManager(this)
        rvWeather.adapter=adapter

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
