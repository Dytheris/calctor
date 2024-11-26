package com.example.test2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private var expression = ""
    private var lastCharacterWasDot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)


        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        numberButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                appendToExpression((it as Button).text.toString())
            }
        }


        val operatorButtons = listOf(
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide
        )
        operatorButtons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                appendToExpression(" ${(it as Button).text} ")
                lastCharacterWasDot = false
            }
        }


        findViewById<Button>(R.id.btnLeftBracket).setOnClickListener { appendToExpression("(") }
        findViewById<Button>(R.id.btnRightBracket).setOnClickListener { appendToExpression(")") }


        findViewById<Button>(R.id.btnDot).setOnClickListener { appendDot() }


        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearExpression() }
    }

    private fun appendToExpression(value: String) {
        expression += value
        tvResult.text = expression
    }

    private fun appendDot() {

        if (!lastCharacterWasDot && expression.isNotEmpty() && expression.last().isDigit()) {
            appendToExpression(".")
            lastCharacterWasDot = true
        } else if (expression.isEmpty()) {

            appendToExpression("0.")
            lastCharacterWasDot = true
        }
    }

    private fun clearExpression() {
        expression = ""
        tvResult.text = "0"
        lastCharacterWasDot = false
    }

    private fun calculateResult() {
        try {
            val result = evaluateExpression(expression)
            tvResult.text = result.toString()
            expression = result.toString()
        } catch (e: Exception) {
            tvResult.text = "Error"
            expression = ""
        }
        lastCharacterWasDot = false
    }


    private fun evaluateExpression(expr: String): Double {
        val postfix = infixToPostfix(expr)
        return evaluatePostfix(postfix)
    }


    private fun infixToPostfix(expression: String): List<String> {
        val precedence = mapOf(
            "+" to 1, "-" to 1,
            "*" to 2, "/" to 2
        )
        val output = mutableListOf<String>()
        val operators = Stack<String>()
        val tokens = tokenize(expression)

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    operators.pop()
                }
                precedence.containsKey(token) -> {
                    while (
                        operators.isNotEmpty() &&
                        precedence[operators.peek()] ?: 0 >= precedence[token] ?: 0
                    ) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }

        return output
    }


    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            when {
                token.toDoubleOrNull() != null -> stack.push(token.toDouble())
                else -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> throw IllegalArgumentException("Unknown operator")
                    }
                    stack.push(result)
                }
            }
        }

        return stack.pop()
    }


    private fun tokenize(expression: String): List<String> {
        val regex = Regex("([+\\-*/()])|\\d+\\.?\\d*")
        return regex.findAll(expression)
            .map { it.value }
            .toList()
    }
}