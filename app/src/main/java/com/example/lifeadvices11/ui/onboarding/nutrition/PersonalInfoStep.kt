package com.example.lifeadvices11.ui.onboarding.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lifeadvices11.ui.common.InputValidation

@Composable
fun PersonalInfoStep(
    viewModel: NutritionOnboardingViewModel,
    onNext: () -> Unit
) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val heightError = InputValidation.validateHeight(height)
    val weightError = InputValidation.validateWeight(weight)
    val ageError = InputValidation.validateAge(age)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Шаг 1 из 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ваши параметры",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = height,
            onValueChange = {
                val sanitized = InputValidation.sanitizeIntegerInput(it)
                height = sanitized
                viewModel.updateHeight(sanitized)
            },
            label = { Text("Рост (см)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = heightError != null,
            supportingText = { heightError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = {
                val sanitized = InputValidation.sanitizeDecimalInput(it)
                weight = sanitized
                viewModel.updateWeight(sanitized)
            },
            label = { Text("Вес (кг)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = weightError != null,
            supportingText = { weightError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = {
                val sanitized = InputValidation.sanitizeIntegerInput(it)
                age = sanitized
                viewModel.updateAge(sanitized)
            },
            label = { Text("Возраст") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = ageError != null,
            supportingText = { ageError?.let { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Пол",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "male",
                    onClick = {
                        gender = "male"
                        viewModel.updateGender("male")
                    }
                )
                Text("Мужской")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "female",
                    onClick = {
                        gender = "female"
                        viewModel.updateGender("female")
                    }
                )
                Text("Женский")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            enabled = height.isNotBlank() &&
                weight.isNotBlank() &&
                age.isNotBlank() &&
                gender.isNotBlank() &&
                heightError == null &&
                weightError == null &&
                ageError == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Далее")
        }
    }
}
