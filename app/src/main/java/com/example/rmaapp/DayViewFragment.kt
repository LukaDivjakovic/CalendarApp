package com.example.rmaapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import coil.load
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDate

class DayViewFragment : Fragment() {

    private lateinit var locationHelper: LocationHelper
    private lateinit var weatherDataTextView: TextView
    private lateinit var weatherIconImageView: ImageView
    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getSerializable("date") as LocalDate?
        }
        requireActivity().supportFragmentManager.setFragmentResultListener("event_changed_key", this) { _, _ ->
            val dayColumnFragment = childFragmentManager.findFragmentById(R.id.day_column_container) as? DayColumnFragment
            dayColumnFragment?.refreshEvents()
        }
        locationHelper = LocationHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_day_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weatherDataTextView = view.findViewById(R.id.weather_data)
        weatherIconImageView = view.findViewById(R.id.weather_icon)

        val dayToShow = date ?: LocalDate.now()
        val dayColumnFragment = DayColumnFragment.newInstance(dayToShow, showOutline = false)
        childFragmentManager.beginTransaction()
            .replace(R.id.day_column_container, dayColumnFragment)
            .commit()

        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        weatherDataTextView.text = "Weather data loading..."
        lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.weatherapi.com/")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                val weatherApiService = retrofit.create(WeatherApiService::class.java)

                try {
                    val response = weatherApiService.getCurrentWeather(
                        apiKey = BuildConfig.WEATHER_API_KEY,
                        location = "$lat,$lon"
                    )

                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            val temp = weatherResponse.current.tempC
                            val condition = weatherResponse.current.condition.text
                            val precip = weatherResponse.current.precipMm
                            val iconUrl = "https:${weatherResponse.current.condition.icon}"

                            weatherIconImageView.load(iconUrl)
                            weatherDataTextView.text = "$condition, $temp°C, $precip mm"
                        }
                    } else {
                        weatherDataTextView.text = "Failed to fetch weather data: ${response.message()}"
                    }
                } catch (e: Exception) {
                    Log.e("DayViewFragment", "Error fetching weather data", e)
                    weatherDataTextView.text = "An error occurred: ${e.message}"
                }
            } else {
                weatherDataTextView.text = "Could not get location."
            }
        }
    }

    companion object {
        fun newInstance(date: LocalDate): DayViewFragment {
            val fragment = DayViewFragment()
            val args = Bundle()
            args.putSerializable("date", date)
            fragment.arguments = args
            return fragment
        }
    }
}
