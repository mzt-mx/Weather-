package com.example.myweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myweather.data.ForecastItem
import com.example.myweather.data.WeatherResponse
import com.example.myweather.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var forecasts by remember { mutableStateOf<List<ForecastItem>>(emptyList()) }

                // Запрос к API
                LaunchedEffect(Unit) {
                    RetrofitInstance.api.getForecast("Moscow", "85afd36bff2ab36aac6bdf3a9e0bec09").enqueue(object : Callback<WeatherResponse> {
                        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    forecasts = it.list
                                }
                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            // Обработка ошибки (например, лог или Toast)
                        }
                    })
                }

                Scaffold { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                    ) {
                        items(forecasts) { item ->
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = item.dt_txt)
                                Text(text = "${item.main.temp} °C")
                                Text(text = item.weather.firstOrNull()?.description ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}
