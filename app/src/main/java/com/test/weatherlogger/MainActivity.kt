package com.test.weatherlogger

import Prefs
import Prefs.set
import Prefs.get
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.test.weatherlogger.models.WeatherApiResult
import com.test.weatherlogger.models.WeatherCurrentDayResult
import com.test.weatherlogger.net.RestApi
import com.test.weatherlogger.utils.Constants.CITY
import com.test.weatherlogger.utils.Constants.DATE
import com.test.weatherlogger.utils.Constants.HUMIDITY
import com.test.weatherlogger.utils.Constants.MAX_TEMP
import com.test.weatherlogger.utils.Constants.MIN_TEMP
import com.test.weatherlogger.utils.Constants.PRESSURE
import com.test.weatherlogger.utils.Constants.TEMPERATURE
import com.test.weatherlogger.utils.Constants.WEATHER_DATA
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val PERMISSION_ID = 42
    val REQUIRED_SDK_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val REQUEST_CODE_ASK_PERMISSIONS = 1
    val REQUEST_CHECK_SETTINGS = 1
    private lateinit var missingPermissions: ArrayList<String>
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private lateinit var locationRequest:LocationRequest
    private var locationCallback:LocationCallback?=null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private lateinit var dialog:Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbarMain)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        dialog= Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)

        temperatureTxt.text="${Prefs.getInstance(this@MainActivity)[TEMPERATURE,0F]} F"
        dateTxt.text=Prefs.getInstance(this@MainActivity)[DATE,""]

        // Go do More details
        moreDetailsLabel.setOnClickListener {
            startActivity(Intent(this,DetailsActivity::class.java))
        }
        // Go do More details
        cardWeatherMain.setOnClickListener {
            startActivity(Intent(this,DetailsActivity::class.java))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            // check app permission
            checkPermission()
            return true
        }

        return false
    }

    private fun checkPermission() {
        missingPermissions = ArrayList()
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }

        if (!missingPermissions.isEmpty()) {
            val permissions = missingPermissions.toTypedArray()
            ActivityCompat.requestPermissions(
                this@MainActivity,
                permissions,
                REQUEST_CODE_ASK_PERMISSIONS
            )
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(
                REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                grantResults
            );

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            for (index in permissions.indices) {
                if ((grantResults[index] != PackageManager.PERMISSION_GRANTED)) {
                    finish()
                    return
                }
            }

            // Request Location update
                dialog.show()
            checkForLocationRequest()
            checkForLocationSettings()
        }


    }

// try to turn on location services
    private fun checkForLocationSettings(){
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            task.addOnSuccessListener { locationSettingsResponse ->
                requestLocationUpate()
            }

            task.addOnFailureListener{exception ->
                if (exception is ResolvableApiException){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(this,
                            REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        dialog.dismiss()
                        // Ignore the error.
                        sendEx.printStackTrace()
                    }
                }
            }


    }

// get weather data from given city
    private fun retrieveWeatherData(city:String){

        val call=RestApi.instance.getCurrentDayWeather(city)
        call.enqueue(object:Callback<WeatherCurrentDayResult>{
            override fun onFailure(call: Call<WeatherCurrentDayResult>, t: Throwable) {
                Toast.makeText(this@MainActivity,getString(R.string.no_internet),Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<WeatherCurrentDayResult>,
                response: Response<WeatherCurrentDayResult>
            ) {

                if(response.isSuccessful){
                    // save retrieved dat to app prefence
                    Prefs.getInstance(this@MainActivity)[CITY]=city
                    Prefs.getInstance(this@MainActivity)[TEMPERATURE]=response.body()!!.currentDay.temperature
                    Prefs.getInstance(this@MainActivity)[HUMIDITY]= response.body()!!.currentDay.humidity
                    Prefs.getInstance(this@MainActivity)[PRESSURE]= response.body()!!.currentDay.pressure
                    Prefs.getInstance(this@MainActivity)[MAX_TEMP]= response.body()!!.currentDay.temp_max
                    Prefs.getInstance(this@MainActivity)[MIN_TEMP]= response.body()!!.currentDay.temp_min
                    Prefs.getInstance(this@MainActivity)[DATE]=dateFormat.format(Date())
                    temperatureTxt.text="${Prefs.getInstance(this@MainActivity)[TEMPERATURE,0F]} F"
                    dateTxt.text=Prefs.getInstance(this@MainActivity)[DATE,""]
                } else{
                    Toast.makeText(this@MainActivity,getString(R.string.error),Toast.LENGTH_SHORT).show()
                }

            }

        })


    }




// get 5 day weather data
    private fun getForecast(city:String){

        val call=RestApi.instance.getWeather(city)
        call.enqueue(object:Callback<WeatherApiResult>{
            override fun onFailure(call: Call<WeatherApiResult>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@MainActivity,getString(R.string.no_internet),Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<WeatherApiResult>,
                response: Response<WeatherApiResult>
            ) {
                dialog.dismiss()

                if(response.isSuccessful){
                    // save weather data
                    val weatherDataList=response.body()!!.weatherDataList
                    val weatherDataJson= Gson().toJson(weatherDataList)
                    Prefs.getInstance(this@MainActivity)[WEATHER_DATA]=weatherDataJson
                } else{
                    Toast.makeText(this@MainActivity,getString(R.string.error),Toast.LENGTH_SHORT).show()
                }

            }

        })

    }


        // get current city name from lattitude and longitude
        fun getCurrentCity(lat:Double,lng:Double):String{
            var city=""
            val geocoder=Geocoder(this@MainActivity,Locale.getDefault())
            try{
                val addressList=geocoder.getFromLocation(lat,lng,10)
                if(addressList.size>0){
                    for(address in addressList){
                        if(address.locality!=null && address.locality.length>0){
                            city=address.locality
                        }
                    }
                }
            } catch (ex:IOException){
                ex.printStackTrace()
            }
            return city

        }


// send location request
    fun checkForLocationRequest(){
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            numUpdates=1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    //force request location update
    fun requestLocationUpate() {
        locationCallback=object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    val lat=it.lastLocation.latitude
                    val lng=it.lastLocation.longitude
                    retrieveWeatherData(getCurrentCity(lat,lng))
                    getForecast(getCurrentCity(lat,lng))
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    override fun onStop() {
        super.onStop()
        // remove registered any observers
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }

    }

    // get updates if user acceppted using location services
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                requestLocationUpate()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }

}
