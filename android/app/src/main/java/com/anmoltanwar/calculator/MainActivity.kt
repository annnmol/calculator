package com.anmoltanwar.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmoltanwar.calculator.ui.theme.CalculatorTheme
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                CalculatorScreen()
            }
        }
    }
}

private enum class KeyType { Number, Function, Operator, Equals }

private data class CalculatorKey(
    val label: String,
    val type: KeyType = KeyType.Number
)

private val keys = listOf(
    listOf(CalculatorKey("AC", KeyType.Function), CalculatorKey("DEL", KeyType.Function), CalculatorKey("%", KeyType.Function), CalculatorKey("÷", KeyType.Operator)),
    listOf(CalculatorKey("7"), CalculatorKey("8"), CalculatorKey("9"), CalculatorKey("×", KeyType.Operator)),
    listOf(CalculatorKey("4"), CalculatorKey("5"), CalculatorKey("6"), CalculatorKey("−", KeyType.Operator)),
    listOf(CalculatorKey("1"), CalculatorKey("2"), CalculatorKey("3"), CalculatorKey("+", KeyType.Operator)),
    listOf(CalculatorKey("±", KeyType.Function), CalculatorKey("0"), CalculatorKey("."), CalculatorKey("=", KeyType.Equals))
)

@Composable
private fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var storedValue by remember { mutableStateOf<BigDecimal?>(null) }
    var pendingOperator by remember { mutableStateOf<String?>(null) }
    var replaceDisplay by remember { mutableStateOf(false) }
    var expression by remember { mutableStateOf("") }

    fun calculate() {
        val left = storedValue ?: return
        val right = display.toBigDecimalOrNull() ?: return
        val result = when (pendingOperator) {
            "+" -> left + right
            "−" -> left - right
            "×" -> left * right
            "÷" -> if (right.compareTo(BigDecimal.ZERO) == 0) null else left.divide(right, 12, RoundingMode.HALF_UP)
            else -> right
        }
        display = result?.stripTrailingZeros()?.toPlainString() ?: "Error"
        storedValue = null
        pendingOperator = null
        replaceDisplay = true
    }

    fun press(label: String) {
        when {
            label in "0123456789." -> {
                if (display == "Error" || replaceDisplay) display = if (label == ".") "0." else label
                else if (label != "." || !display.contains(".")) display = if (display == "0" && label != ".") label else display + label
                replaceDisplay = false
            }
            label == "AC" -> {
                display = "0"
                storedValue = null
                pendingOperator = null
                expression = ""
                replaceDisplay = false
            }
            label == "DEL" -> {
                if (display == "Error") {
                    display = "0"
                    expression = ""
                    storedValue = null
                    pendingOperator = null
                    replaceDisplay = false
                } else {
                    if (replaceDisplay && pendingOperator != null) {
                        expression = ""
                        storedValue = null
                        pendingOperator = null
                    }
                    replaceDisplay = false
                    display = display.dropLast(1).ifEmpty { "0" }
                }
            }
            label == "%" -> display = display.toBigDecimalOrNull()?.divide(BigDecimal(100), 12, RoundingMode.HALF_UP)?.stripTrailingZeros()?.toPlainString() ?: "Error"
            label == "±" -> if (display != "0" && display != "Error") display = if (display.startsWith("-")) display.drop(1) else "-$display"
            label in listOf("+", "−", "×", "÷") -> {
                if (storedValue == null || pendingOperator == null) {
                    expression = "$display $label"
                } else if (replaceDisplay) {
                    expression = "${expression.substringBeforeLast(" ").trimEnd()} $label"
                } else {
                    expression = "$expression $display $label"
                    calculate()
                }
                storedValue = display.toBigDecimalOrNull()
                pendingOperator = label
                replaceDisplay = true
            }
            label == "=" -> {
                if (storedValue != null && pendingOperator != null) {
                    expression = if (replaceDisplay) "$expression =" else "$expression $display ="
                }
                calculate()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CALCULATOR",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = expression,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        maxLines = 3,
                        textAlign = TextAlign.End,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = display,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 60.sp,
                        lineHeight = 68.sp,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { key ->
                            CalculatorButton(
                                key = key,
                                modifier = Modifier.weight(1f),
                                onClick = { press(key.label) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    key: CalculatorKey,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = when (key.type) {
        KeyType.Number -> MaterialTheme.colorScheme.surfaceVariant
        KeyType.Function -> MaterialTheme.colorScheme.secondaryContainer
        KeyType.Operator -> MaterialTheme.colorScheme.primaryContainer
        KeyType.Equals -> MaterialTheme.colorScheme.primary
    }
    val content = when (key.type) {
        KeyType.Equals -> MaterialTheme.colorScheme.onPrimary
        KeyType.Operator -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyType.Function -> MaterialTheme.colorScheme.onSecondaryContainer
        KeyType.Number -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.size(76.dp),
        shape = CircleShape,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = content
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(
            text = key.label,
            fontSize = if (key.label == "DEL") 14.sp else 24.sp,
            fontWeight = if (key.type == KeyType.Equals) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculatorPreview() {
    CalculatorTheme {
        CalculatorScreen()
    }
}
