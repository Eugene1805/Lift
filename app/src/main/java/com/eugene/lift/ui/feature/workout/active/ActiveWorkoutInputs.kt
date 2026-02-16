package com.eugene.lift.ui.feature.workout.active

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        fontSize = 10.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CompactNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    CompactTextInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Number,
        filterInput = { input: String -> input.filter { it.isDigit() } },
        placeholder = placeholder,
        enabled = enabled
    )
}

@Composable
fun CompactDecimalInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    CompactTextInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Decimal,
        filterInput = { input: String ->
            buildString {
                var decimalAdded = false
                for (char in input) {
                    when {
                        char.isDigit() -> append(char)
                        char == '.' && !decimalAdded -> {
                            append(char)
                            decimalAdded = true
                        }
                    }
                }
            }
        },
        placeholder = placeholder,
        enabled = enabled
    )
}

@Composable
fun CompactTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    keyboardType: KeyboardType,
    filterInput: (String) -> String,
    placeholder: @Composable (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val shape = MaterialTheme.shapes.small
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value, isFocused) {
        if (!isFocused && textFieldValue.text != value) {
            textFieldValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            val filtered = filterInput(newTextFieldValue.text)
            val newSelection = if (filtered.length < newTextFieldValue.text.length) {
                TextRange(minOf(newTextFieldValue.selection.start, filtered.length))
            } else {
                newTextFieldValue.selection
            }
            textFieldValue = TextFieldValue(text = filtered, selection = newSelection)
            onValueChange(filtered)
        },
        modifier = modifier
            .background(containerColor, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .onFocusChanged { focusState ->
                val wasFocused = isFocused
                isFocused = focusState.isFocused

                if (focusState.isFocused && textFieldValue.text.isNotEmpty()) {
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                } else if (wasFocused && !focusState.isFocused && textFieldValue.text != value) {
                    textFieldValue = TextFieldValue(
                        text = value,
                        selection = TextRange(value.length)
                    )
                }
            },
        singleLine = true,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    placeholder?.invoke()
                }
                innerTextField()
            }
        }
    )
}

fun formatWeight(weight: Double): String =
    if (weight == weight.toLong().toDouble()) weight.toLong().toString() else weight.toString()
