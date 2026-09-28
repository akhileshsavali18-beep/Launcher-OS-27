package com.example.data

import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.model.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

/** Fetches live weather from Open-Meteo. No API key is required. */
class WeatherRepository(private val context: Context) {
  private val client = OkHttpClient()

  suspend fun fetchCurrentWeather(): WeatherState? = withContext(Dispatchers.IO) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@withContext null

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = locationManager.getProviders(true)
    val location = providers.asSequence()
      .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
      .maxByOrNull { it.time } ?: return@withContext null

    val lat = String.format(Locale.US, "%.5f", location.latitude)
    val lon = String.format(Locale.US, "%.5f", location.longitude)
    val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min,uv_index_max&forecast_days=1&timezone=auto"
    val request = Request.Builder().url(url).get().build()
    val body = client.newCall(request).execute().use { response -> if (!response.isSuccessful) return@withContext null else response.body?.string() }
      ?: return@withContext null

    val root = JSONObject(body)
    val current = root.getJSONObject("current")
    val daily = root.getJSONObject("daily")
    val locationName = reverseGeocodeName(location.latitude, location.longitude)
    WeatherState(
      location = locationName ?: "Current Location",
      temperatureCelsius = current.optDouble("temperature_2m", 0.0).toInt(),
      condition = weatherCodeToText(current.optInt("weather_code", 0)),
      highTemp = daily.getJSONArray("temperature_2m_max").optDouble(0, 0.0).toInt(),
      lowTemp = daily.getJSONArray("temperature_2m_min").optDouble(0, 0.0).toInt(),
      uvIndex = daily.getJSONArray("uv_index_max").optDouble(0, 0.0).toInt(),
      windSpeedKmh = current.optDouble("wind_speed_10m", 0.0).toInt()
    )
  }

  private fun reverseGeocodeName(latitude: Double, longitude: Double): String? {
    return runCatching {
      val address = android.location.Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)?.firstOrNull()
      address?.locality ?: address?.subAdminArea
    }.getOrNull()
  }

  private fun weatherCodeToText(code: Int): String = when (code) {
    0 -> "Clear Sky"
    1, 2 -> "Partly Cloudy"
    3 -> "Overcast"
    45, 48 -> "Foggy"
    51, 53, 55, 56, 57 -> "Drizzle"
    61, 63, 65, 66, 67 -> "Rain"
    71, 73, 75, 77 -> "Snow"
    80, 81, 82 -> "Rain Showers"
    85, 86 -> "Snow Showers"
    95, 96, 99 -> "Thunderstorm"
    else -> "Unknown"
  }
}
