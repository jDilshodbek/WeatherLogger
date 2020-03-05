package com.test.weatherlogger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.test.weatherlogger.models.WeatherData
import kotlinx.android.synthetic.main.activity_details.view.*
import kotlinx.android.synthetic.main.card_item.view.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter  (val weahterDataList: List<WeatherData>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.card_item,parent,false)
    )
    override fun getItemCount()=weahterDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(weahterDataList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weatherImage=itemView.imageView
        private val dayWeather=itemView.day
        private val climateWeather=itemView.climate
        private val minTemp=itemView.lowTemp
        private val maxTemp=itemView.highTemp
        private val humidity=itemView.humidityCard

        fun bindData(weatherData: WeatherData){
            Picasso.get().load("http://openweathermap.org/img/w/${weatherData.weatherList[0].icon}.png").fit().into(weatherImage)
            val formatter= NumberFormat.getInstance().apply { maximumFractionDigits=0 }
            dayWeather.text= convertTimeStampToDay(weatherData.dt)
            climateWeather.text= weatherData.weatherList[0].description
            maxTemp.text="High: ${formatter.format(weatherData.main.temp_max)}°F"
            minTemp.text="Low: ${formatter.format(weatherData.main.temp_min)}°F"
            humidity.text="Humidity: ${formatter.format(weatherData.main.humidity/100.0)}"
        }
    }


    companion object{
        private fun convertTimeStampToDay(timeStamp: Long): String? {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp * 1000
            val tz = TimeZone.getDefault()
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))
            val dateFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
            return dateFormatter.format(calendar.time)
        }
    }
}