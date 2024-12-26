package com.example.sixthlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sixthlab.services.CalculatorService
import com.example.sixthlab.ui.theme.Purple40


@Composable
fun CalculatorScreen(
    calculatorService: CalculatorService
) {
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    fun calculateSum() {
        val formattedValue1 = value1.toIntOrNull() ?: 0
        val formattedValue2 = value2.toIntOrNull() ?: 0

        result = calculatorService.sumValues(formattedValue1, formattedValue2).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = value1,
            onValueChange = { value1 = it },
            label = { Text("Введіть перше число") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        TextField(
            value = value2,
            onValueChange = { value2 = it },
            label = { Text("Введіть друге число") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(Modifier.height(10.dp))
        if (result.isNotEmpty()) {
            Text(
                text = "Результат: $result",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Button(
            onClick = { calculateSum() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .size(width = 300.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(
                text = "Розрахувати",
                fontSize = 16.sp,
            )
        }

    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val calculatorService = CalculatorService()
            CalculatorScreen(
                calculatorService = calculatorService
            )
        }
    }
}



