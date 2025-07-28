package com.example.myweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myweather.data.ForecastItem
import com.example.myweather.data.WeatherResponse
import com.example.myweather.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.WbSunny

// Цвета оформления
private val darkBlue = Color(0xFF1A1A4D)
private val cardBlue = Color(0xFF2E2E70)
private val highlightBlue = Color(0x99B2FFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var forecasts by remember { mutableStateOf<List<ForecastItem>>(emptyList()) }

                // Получаем прогноз из API
                LaunchedEffect(Unit) {
                    RetrofitInstance.api.getForecast("Moscow", "85afd36bff2ab36aac6bdf3a9e0bec09")
                        .enqueue(object : Callback<WeatherResponse> {
                            override fun onResponse(
                                call: Call<WeatherResponse>,
                                response: Response<WeatherResponse>
                            ) {
                                if (response.isSuccessful) {
                                    response.body()?.let {
                                        forecasts = it.list
                                    }
                                }
                            }

                            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                                // Обработка ошибки
                            }
                        })
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(darkBlue)
                ) {
                    item {
                        WeatherHomePage(forecasts)
                    }
                    item {
                        SevenDayForecastScreen(forecasts)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherHomePage(forecasts: List<ForecastItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(24.dp))
        LocationText("Moscow, Russia")
        Spacer(modifier = Modifier.height(24.dp))

        // Текущая погода - берем первый элемент прогноза (можно заменить на отдельный запрос)
        if (forecasts.isNotEmpty()) {
            CurrentWeatherCard(forecasts.first())
        } else {
            Text("Загрузка...", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
        TodayForecast(forecasts)
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* TODO меню */ }) {
            Icon(Icons.Default.Dehaze, contentDescription = "Menu", tint = Color.White)
        }
        IconButton(onClick = { /* TODO профиль */ }) {
            Icon(Icons.Default.PersonOutline, contentDescription = "Profile", tint = Color.White)
        }
    }
}

@Composable
fun LocationText(location: String) {
    Text(
        text = location,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White
    )
}

@Composable
fun CurrentWeatherCard(item: ForecastItem) {
    val temp = item.main.temp.roundToInt()
    val description = item.weather.firstOrNull()?.description ?: ""
    val iconCode = item.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBlue)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = iconUrl,
                contentDescription = description,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description.replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = item.dt_txt, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "$temp°", color = Color.White, fontSize = 80.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            // Можно добавить влажность, ветер и др. из item.main и item.wind, если есть
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherInfo("${item.main.humidity}%", "Humidity")
                WeatherInfo("${item.wind?.speed ?: "0"} m/s", "Wind")
                WeatherInfo("${item.main.pressure} hPa", "Pressure")
            }
        }
    }
}

@Composable
fun WeatherInfo(value: String, type: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = type, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun TodayForecast(forecasts: List<ForecastItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Today", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Next week >", color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Для наглядности показываем максимум 10 ближайших прогнозов
            items(forecasts.take(10)) { item ->
                HourlyForecastItem(item)
            }
        }
    }
}

@Composable
fun HourlyForecastItem(item: ForecastItem) {
    val temp = item.main.temp.roundToInt()
    val description = item.weather.firstOrNull()?.description ?: ""
    val iconCode = item.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBlue)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.dt_txt.substring(11, 16), color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(model = iconUrl, contentDescription = description, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$temp°", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SevenDayForecastScreen(forecasts: List<ForecastItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SevenDayTopBar()
        Spacer(modifier = Modifier.height(24.dp))
        // Выделяем текущий день (можно взять первый элемент)
        if (forecasts.isNotEmpty()) {
            TodayHighlightCard(forecasts.first())
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Отображаем следующие 7 дней
            items(forecasts.take(7)) { item ->
                DailyForecastItem(item)
            }
        }
    }
}

@Composable
fun SevenDayTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = "Next 7 days",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.PersonOutline, contentDescription = "Profile", tint = Color.White)
        }
    }
}

@Composable
fun TodayHighlightCard(item: ForecastItem) {
    val temp = item.main.temp.roundToInt()
    val description = item.weather.firstOrNull()?.description ?: ""
    val iconCode = item.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = highlightBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Today", color = darkBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description.replaceFirstChar { it.uppercase() }, color = darkBlue, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("$temp°", color = darkBlue, fontSize = 48.sp, fontWeight = FontWeight.Bold)
            }
            AsyncImage(
                model = iconUrl,
                contentDescription = description,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

@Composable
fun DailyForecastItem(item: ForecastItem) {
    val temp = item.main.temp.roundToInt()
    val description = item.weather.firstOrNull()?.description ?: ""
    val iconCode = item.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dt_txt.substring(0, 10),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.4f)
        )
        AsyncImage(
            model = iconUrl,
            contentDescription = description,
            modifier = Modifier
                .weight(0.1f)
                .size(40.dp)
        )
        Text(text = description, color = Color.Gray, modifier = Modifier.weight(0.3f))
        Text(text = "$temp°", color = Color.White, modifier = Modifier.weight(0.2f))
    }
}
