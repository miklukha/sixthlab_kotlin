package com.example.sixthlab

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// вхідні дані
data class ElectricalEquipment(
    val name: String = "",
    val efficiencyFactor: Double = 0.0,
    val loadPowerFactor: Double = 0.0,
    val loadVoltage: Double = 0.0,
    val quantity: Int = 0,
    val ratedPower: Int = 0,
    val utilizationRate: Double = 0.0,
    val reactivePowerFactor: Double = 0.0,
    val calculatedValues: EquipmentCalculatedValues = EquipmentCalculatedValues()
)

// для проміжних розрахунків
data class EquipmentCalculatedValues(
    val powerTotal: Double = 0.0,              // n × Pн
    val utilizationPower: Double = 0.0,        // n × Pн × Кв
    val reactivePower: Double = 0.0,           // n × Pн × Кв × tgφ
    val squaredPower: Double = 0.0,            // n × Pн²
    val current: Double = 0.0                  // Ip
)

// результати
data class WorkshopResults(
    val groupUtilizationRate: Double = 0.0,
    val effectiveEquipmentCount: Double = 0.0,
    val estimatedActivePowerFactor: Double? = 0.0,
    val estimatedActiveLoad: Double = 0.0,
    val estimatedReactiveLoad: Double = 0.0,
    val fullPower: Double = 0.0,
    val estimatedGroupCurrent: Double = 0.0,

    val utilizationRateAll: Double = 0.0,
    val effectiveEquipmentCountAll: Int = 0,
    val estimatedActivePowerFactorAll: Double? = 0.0,
    val estimatedActiveLoadTires: Double = 0.0,
    val estimatedReactiveLoadTires: Double = 0.0,
    val fullPowerTires: Double = 0.0,
    val estimatedGroupCurrentTires: Double = 0.0
)

// для таблиці 6.3 Значення розрахункових коефіцієнтів КР для
// мереж живлення напругою до 1000 В
data class TableEntry(
    val n: Int,
    val coefficients: Map<Double, Double>
)

// для таблиці 6.4 Значення розрахункових коефіцієнтів КР на шинах низької
// напруги цехових трансформаторів і магістральних шинопроводів
data class TableRange(
    val start: Int,
    val end: Int?,
    val coefficients: Map<Double, Double>
)

class ElectricalLoadCalculator {
    // значення з таблиці 6.3
    private val tableData = listOf(
        TableEntry(1, mapOf(
            0.1 to 8.00, 0.15 to 5.33, 0.2 to 4.00, 0.3 to 2.67,
            0.4 to 2.00, 0.5 to 1.60, 0.6 to 1.33, 0.7 to 1.14, 0.8 to 1.0
        )),
        TableEntry(2, mapOf(
            0.1 to 6.22, 0.15 to 4.33, 0.2 to 3.39, 0.3 to 2.45,
            0.4 to 1.98, 0.5 to 1.60, 0.6 to 1.33, 0.7 to 1.14, 0.8 to 1.0
        )),
        TableEntry(3, mapOf(
            0.1 to 4.06, 0.15 to 2.89, 0.2 to 2.31, 0.3 to 1.74,
            0.4 to 1.45, 0.5 to 1.34, 0.6 to 1.22, 0.7 to 1.14, 0.8 to 1.0
        )),
        TableEntry(4, mapOf(
            0.1 to 3.23, 0.15 to 2.29, 0.2 to 1.83, 0.3 to 1.39,
            0.4 to 1.21, 0.5 to 1.13, 0.6 to 1.08, 0.7 to 1.03, 0.8 to 1.0
        )),
        TableEntry(5, mapOf(
            0.1 to 2.84, 0.15 to 2.06, 0.2 to 1.65, 0.3 to 1.31,
            0.4 to 1.15, 0.5 to 1.10, 0.6 to 1.05, 0.7 to 1.01, 0.8 to 1.0
        )),
        TableEntry(6, mapOf(
            0.1 to 2.64, 0.15 to 1.96, 0.2 to 1.62, 0.3 to 1.28,
            0.4 to 1.14, 0.5 to 1.13, 0.6 to 1.06, 0.7 to 1.01, 0.8 to 1.0
        )),
        TableEntry(7, mapOf(
            0.1 to 2.49, 0.15 to 1.86, 0.2 to 1.54, 0.3 to 1.23,
            0.4 to 1.12, 0.5 to 1.10, 0.6 to 1.04, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(8, mapOf(
            0.1 to 2.37, 0.15 to 1.78, 0.2 to 1.48, 0.3 to 1.19,
            0.4 to 1.10, 0.5 to 1.08, 0.6 to 1.02, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(9, mapOf(
            0.1 to 2.27, 0.15 to 1.71, 0.2 to 1.43, 0.3 to 1.16,
            0.4 to 1.09, 0.5 to 1.07, 0.6 to 1.01, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(10, mapOf(
            0.1 to 2.18, 0.15 to 1.65, 0.2 to 1.39, 0.3 to 1.13,
            0.4 to 1.07, 0.5 to 1.05, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(12, mapOf(
            0.1 to 2.04, 0.15 to 1.56, 0.2 to 1.32, 0.3 to 1.08,
            0.4 to 1.05, 0.5 to 1.03, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(14, mapOf(
            0.1 to 1.94, 0.15 to 1.49, 0.2 to 1.27, 0.3 to 1.05,
            0.4 to 1.02, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(16, mapOf(
            0.1 to 1.85, 0.15 to 1.43, 0.2 to 1.23, 0.3 to 1.02,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(18, mapOf(
            0.1 to 1.78, 0.15 to 1.39, 0.2 to 1.19, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(20, mapOf(
            0.1 to 1.72, 0.15 to 1.35, 0.2 to 1.16, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(25, mapOf(
            0.1 to 1.60, 0.15 to 1.27, 0.2 to 1.10, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(30, mapOf(
            0.1 to 1.51, 0.15 to 1.21, 0.2 to 1.05, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(35, mapOf(
            0.1 to 1.44, 0.15 to 1.16, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(40, mapOf(
            0.1 to 1.40, 0.15 to 1.13, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(50, mapOf(
            0.1 to 1.30, 0.15 to 1.07, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(60, mapOf(
            0.1 to 1.25, 0.15 to 1.03, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(80, mapOf(
            0.1 to 1.16, 0.15 to 1.0, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        )),
        TableEntry(100, mapOf(
            0.1 to 1.0, 0.15 to 1.0, 0.2 to 1.0, 0.3 to 1.0,
            0.4 to 1.0, 0.5 to 1.0, 0.6 to 1.0, 0.7 to 1.0, 0.8 to 1.0
        ))
    )

    // значення з таблиці 6.4
    private val tableDataRange = listOf(
        TableRange(1, 1, mapOf(
            0.1 to 8.00,
            0.15 to 5.33,
            0.2 to 4.00,
            0.3 to 2.67,
            0.4 to 2.00,
            0.5 to 1.60,
            0.6 to 1.33,
            0.7 to 1.14
        )),
        TableRange(2, 2, mapOf(
            0.1 to 5.01,
            0.15 to 3.44,
            0.2 to 2.69,
            0.3 to 1.90,
            0.4 to 1.52,
            0.5 to 1.24,
            0.6 to 1.11,
            0.7 to 1.0
        )),
        TableRange(3, 3, mapOf(
            0.1 to 2.40,
            0.15 to 2.17,
            0.2 to 1.80,
            0.3 to 1.42,
            0.4 to 1.23,
            0.5 to 1.14,
            0.6 to 1.08,
            0.7 to 1.0
        )),
        TableRange(4, 4, mapOf(
            0.1 to 2.28,
            0.15 to 1.73,
            0.2 to 1.46,
            0.3 to 1.19,
            0.4 to 1.06,
            0.5 to 1.04,
            0.6 to 1.0,
            0.7 to 0.97
        )),
        TableRange(5, 5, mapOf(
            0.1 to 1.31,
            0.15 to 1.12,
            0.2 to 1.02,
            0.3 to 1.0,
            0.4 to 0.98,
            0.5 to 0.96,
            0.6 to 0.94,
            0.7 to 0.93
        )),
        TableRange(6, 8, mapOf(
            0.1 to 1.20,
            0.15 to 1.0,
            0.2 to 0.96,
            0.3 to 0.95,
            0.4 to 0.94,
            0.5 to 0.93,
            0.6 to 0.92,
            0.7 to 0.91
        )),
        TableRange(9, 10, mapOf(
            0.1 to 1.10,
            0.15 to 0.97,
            0.2 to 0.91,
            0.3 to 0.90,
            0.4 to 0.90,
            0.5 to 0.90,
            0.6 to 0.90,
            0.7 to 0.90
        )),
        TableRange(10, 25, mapOf(
            0.1 to 0.80,
            0.15 to 0.80,
            0.2 to 0.80,
            0.3 to 0.85,
            0.4 to 0.85,
            0.5 to 0.85,
            0.6 to 0.90,
            0.7 to 0.90
        )),
        TableRange(25, 50, mapOf(
            0.1 to 0.75,
            0.15 to 0.75,
            0.2 to 0.75,
            0.3 to 0.75,
            0.4 to 0.75,
            0.5 to 0.80,
            0.6 to 0.85,
            0.7 to 0.85
        )),
        TableRange(50, null, mapOf(
            0.1 to 0.65,
            0.15 to 0.65,
            0.2 to 0.65,
            0.3 to 0.70,
            0.4 to 0.70,
            0.5 to 0.75,
            0.6 to 0.80,
            0.7 to 0.80
        ))
    )

    // функція розрахунку проміжних результатів по кожному ЕП
    fun calculateEquipmentValues(equipment: ElectricalEquipment): ElectricalEquipment {
        // n * Pн
        val powerTotal = equipment.quantity.toDouble() * equipment.ratedPower
        // n * Pн * Кв
        val utilizationPower = powerTotal * equipment.utilizationRate
        // n * Pн * Кв * tgφ
        val reactivePower = utilizationPower * equipment.reactivePowerFactor
        // n * Pн^2
        val squaredPower = equipment.quantity * Math.pow(equipment.ratedPower.toDouble(), 2.0)
        // (n * Pн) / √3 * Uн * cosφ * nн
        val current = powerTotal / (Math.sqrt(3.0) * equipment.loadVoltage *
                equipment.loadPowerFactor * equipment.efficiencyFactor)
        val currentTruncate = Math.floor(current * 10) / 10

        return equipment.copy(
            calculatedValues = EquipmentCalculatedValues(
                powerTotal = powerTotal,
                utilizationPower = utilizationPower,
                reactivePower = reactivePower,
                squaredPower = squaredPower,
                current = currentTruncate
            )
        )
    }

    // функція для пошуку розрахункового коефіцієнту активної потужності по таблиці 6.3
    private fun findCoefficient(n: Int, coefficient: Double): Double? {
        return tableData
            .find { it.n == n }
            ?.coefficients
            ?.get(coefficient)
    }

    // функція для пошуку розрахункового коефіцієнту активної потужності по таблиці 6.4
    private fun findCoefficientRange(n: Int, coefficient: Double): Double? {
        return tableDataRange
            .find { range ->
                n >= range.start && (range.end == null || n <= range.end)
            }
            ?.coefficients
            ?.get(coefficient)
    }

    @SuppressLint("DefaultLocale")
    fun calculateWorkshopResults(equipmentList: List<ElectricalEquipment>): WorkshopResults {
        // Σ n * Pн
        val totalPower = equipmentList.sumOf { it.calculatedValues.powerTotal }
        // Σ n * Pн * Кв
        val totalUtilizationPower = equipmentList.sumOf { it.calculatedValues.utilizationPower }
        // Σ n * Pн^2
        val totalSquaredPower = equipmentList.sumOf { it.calculatedValues.squaredPower }
        // Σ n * Pн * Кв * tgφ
        val totalReactivePower = equipmentList.sumOf { it.calculatedValues.reactivePower }

        // Груповий коефіцієнт використання
        val groupUtilizationRate = totalUtilizationPower / totalPower

        // Ефективна кількість ЕП
        val effectiveEquipmentCount = Math.ceil(Math.pow(totalPower, 2.0) / totalSquaredPower)

        // Розрахунковий коефіцієнт активної потужності
        val roundedEffectiveEquipmentCount = Math.ceil(effectiveEquipmentCount).toInt()

        // Округлення groupUtilizationRate до першого знаку після коми для пошуку в таблиці
        val roundedGroupUtilizationRate = (Math.round(groupUtilizationRate * 10.0) / 10.0)

        // Отримання коефіцієнту з таблиці з перевіркою на null
        val estimatedActivePowerFactor = findCoefficient(roundedEffectiveEquipmentCount, roundedGroupUtilizationRate) ?: 1.25

        // Розрахункове активне навантаження
        val estimatedActiveLoad = estimatedActivePowerFactor * totalUtilizationPower

        // Розрахункове реактивне навантаження
        val estimatedReactiveLoad = totalReactivePower

        // Повна потужність
        val fullPower = Math.sqrt(Math.pow(estimatedActiveLoad, 2.0) +
                Math.pow(estimatedReactiveLoad, 2.0))

        // Розрахунковий груповий струм
        val estimatedGroupCurrent = if (equipmentList.isNotEmpty()) {
            estimatedActiveLoad / equipmentList.first().loadVoltage
        } else 0.0

        // цех в цілому
        // кількість ЕП
        val equipmentNumber = 81
        // n * Pн
        val powerAll = 2330
        // n * Pн * Кв
        val utilizationPowerAll = 752
        // n * Pн * Кв * tgφ
        val reactivePowerAll = 657
        // n * Pн^2
        val squaredPowerAll = 96388

        // Коефіцієнти використання цеху в цілому
        val utilizationRateAll = utilizationPowerAll.toDouble() / powerAll

        // Ефективна кількість ЕП цеху в цілому
        val effectiveEquipmentCountAll = (Math.pow(powerAll.toDouble(), 2.0) / squaredPowerAll).toInt()

        // Округлення utilizationRateAll до першого знаку після коми для пошуку в таблиці
        val roundedUtilizationRateAll = (Math.round(utilizationRateAll * 10.0) / 10.0)

        // Розрахунковий коефіцієнт активної потужності цеху в цілому
        val estimatedActivePowerFactorAll = findCoefficientRange(effectiveEquipmentCountAll, roundedUtilizationRateAll) ?: 0.7

        // Розрахункове активне навантаження на шинах 0,38 кВ ТП
        val estimatedActiveLoadTires = estimatedActivePowerFactorAll * utilizationPowerAll

        // Розрахункове реактивне навантаження на шинах 0,38 кВ ТП
        val estimatedReactiveLoadTires = estimatedActivePowerFactorAll * reactivePowerAll

        // Повна потужність на шинах 0,38 кВ ТП
        val fullPowerTires = Math.sqrt(Math.pow(estimatedActiveLoadTires, 2.0) +
                Math.pow(estimatedReactiveLoadTires, 2.0))

        // Розрахунковий груповий струм на шинах 0,38 кВ ТП
        val estimatedGroupCurrentTires = if (equipmentList.isNotEmpty()) {
            estimatedActiveLoadTires / equipmentList.first().loadVoltage
        } else 0.0

        return WorkshopResults(
            groupUtilizationRate = groupUtilizationRate,
            effectiveEquipmentCount = effectiveEquipmentCount,
            estimatedActivePowerFactor = estimatedActivePowerFactor,
            estimatedActiveLoad = estimatedActiveLoad,
            estimatedReactiveLoad = estimatedReactiveLoad,
            fullPower = fullPower,
            estimatedGroupCurrent = estimatedGroupCurrent,

            utilizationRateAll = roundedUtilizationRateAll,
            effectiveEquipmentCountAll = effectiveEquipmentCountAll,
            estimatedActivePowerFactorAll = estimatedActivePowerFactorAll,
            estimatedActiveLoadTires = estimatedActiveLoadTires,
            estimatedReactiveLoadTires = estimatedReactiveLoadTires,
            fullPowerTires = fullPowerTires,
            estimatedGroupCurrentTires = estimatedGroupCurrentTires,
        )
    }
}

@Composable
fun CalculatorScreen() {
    val calculator = remember { ElectricalLoadCalculator() }
    var equipmentList by remember { mutableStateOf(List(8) { ElectricalEquipment() }) }
    var workshopResults by remember { mutableStateOf<WorkshopResults?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Калькулятор для розрахунку електричних навантажень",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        equipmentList.forEachIndexed { index, equipment ->
            EquipmentInputCard(
                equipment = equipment,
                index = index,
                onValueChange = { newEquipment ->
                    equipmentList = equipmentList.toMutableList().also {
                        it[index] = calculator.calculateEquipmentValues(newEquipment)
                    }
                }
            )
        }

        Button(
            onClick = {
                workshopResults = calculator.calculateWorkshopResults(equipmentList)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .size(width = 300.dp, height = 50.dp)
        ) {
            Text("Розрахувати загальні результати")
        }

        workshopResults?.let { DisplayWorkshopResults(it) }
    }
}

@Composable
fun EquipmentInputCard(
    equipment: ElectricalEquipment,
    index: Int,
    onValueChange: (ElectricalEquipment) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Електроприймач ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            InputFieldString(
                "Найменування ЕП",
                equipment.name
            ) {
                onValueChange(equipment.copy(name = it))
            }

            InputFieldDouble(
                "Номінальне значення коефіцієнта корисної дії ЕП, ηн",
                equipment.efficiencyFactor
            ) {
                onValueChange(equipment.copy(efficiencyFactor = it))
            }

            InputFieldDouble(
                "Коефіцієнт потужності навантаження, cos φ",
                equipment.loadPowerFactor
            ) {
                onValueChange(equipment.copy(loadPowerFactor = it))
            }

            InputFieldDouble(
                "Напруга навантаження: Uн, кВ",
                equipment.loadVoltage
            ) {
                onValueChange(equipment.copy(loadVoltage = it))
            }

            InputFieldInt(
                "Кількість ЕП: n, шт",
                equipment.quantity
            ) {
                onValueChange(equipment.copy(quantity = it))
            }

            InputFieldInt (
                "Номінальна потужність ЕП: Рн, кВт",
                equipment.ratedPower
            ) {
                onValueChange(equipment.copy(ratedPower = it))
            }

            InputFieldDouble(
                "Коефіцієнт використання: КВ",
                equipment.utilizationRate
            ) {
                onValueChange(equipment.copy(utilizationRate = it))
            }

            InputFieldDouble(
                "Коефіцієнт реактивної потужності: tgφ",
                equipment.reactivePowerFactor
            ) {
                onValueChange(equipment.copy(reactivePowerFactor = it))
            }

            if (equipment.calculatedValues.powerTotal > 0) {
                DisplayEquipmentResults(equipment.calculatedValues)
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DisplayEquipmentResults(values: EquipmentCalculatedValues) {
    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text("Проміжні розрахунки:", style = MaterialTheme.typography.titleSmall)
        Text("n × Pн = ${String.format("%.2f", values.powerTotal.toDouble())} кВт")
        Text("n × Pн × Кв = ${String.format("%.2f", values.utilizationPower)} кВт")
        Text("n × Pн × Кв × tgφ = ${String.format("%.2f", values.reactivePower)} квар")
        Text("n × Pн² = ${String.format("%.2f", values.squaredPower)}")
        Text("Ip = ${String.format("%.2f", values.current)} А")

    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DisplayWorkshopResults(results: WorkshopResults) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Загальні результати розрахунків:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text("Груповий коефіцієнт використання: ${String.format("%.4f", results.groupUtilizationRate)}")
            Text("Ефективна кількість ЕП: ${String.format("%.2f", results.effectiveEquipmentCount)}")
            Text("Розрахунковий коефіцієнт активної потужності: ${String.format("%.2f", results.estimatedActivePowerFactor ?: 0.0)}")
            Text("Розрахункове активне навантаження: ${String.format("%.2f", results.estimatedActiveLoad)} кВт")
            Text("Розрахункове реактивне навантаження: ${String.format("%.2f", results.estimatedReactiveLoad)} квар")
            Text("Повна потужність: ${String.format("%.2f", results.fullPower)} кВ*А")
            Text("Розрахунковий груповий струм: ${String.format("%.2f", results.estimatedGroupCurrent)} А")

            Text("Коефіцієнти використання цеху в цілому: ${results.utilizationRateAll}")
            Text("Ефективна кількість ЕП цеху в цілому: ${results.effectiveEquipmentCountAll}")
            Text("Розрахунковий коефіцієнт активної потужності цеху в цілому: ${String.format("%.2f", results.estimatedActivePowerFactorAll)}")
            Text("Розрахункове активне навантаження на шинах 0,38 кВ ТП: ${String.format("%.2f", results.estimatedActiveLoadTires)} кВт")
            Text("Розрахункове реактивне навантаження на шинах 0,38 кВ ТП: ${String.format("%.2f", results.estimatedReactiveLoadTires)} квар")
            Text("Повна потужність на шинах 0,38 кВ ТП: ${String.format("%.2f", results.fullPowerTires)} кВ*А")
            Text("Розрахунковий груповий струм на шинах 0,38 кВ ТП: ${String.format("%.2f", results.estimatedGroupCurrentTires)} А")
        }
    }
}

@Composable
fun InputFieldString(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
/** Шліфувальний верстат
 * Свердлильний верстат
 * Фугувальний верстат
 * Циркулярна пила
 * Прес
 * Полірувальний верстат
 * Фрезерний верстат
 * Вентилятор
*/

@Composable
fun InputFieldDouble(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0.0) "" else value.toString(),
        onValueChange = { newValue ->
            try {
                if (newValue.isEmpty()) {
                    onValueChange(0.0)
                } else {
                    newValue.toDoubleOrNull()?.let { onValueChange(it) }
                }
            } catch (e: NumberFormatException) {
                //
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun InputFieldInt(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { newValue ->
            try {
                if (newValue.isEmpty()) {
                    onValueChange(0)
                } else {
                    onValueChange(newValue.toInt())
                }
            } catch (e: NumberFormatException) {
                //
            }
        },

        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorScreen()
        }
    }
}



