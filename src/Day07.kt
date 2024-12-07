import Day07.Operator.Add
import Day07.Operator.Concatenate
import Day07.Operator.Multiply
import Day07.Part1
import Day07.Part2
import Day07.calculateSumOfTestValuesAsync
import Day07.calculateSumOfTestValuesSync
import Day07.parseInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.measureTimedValue

// https://adventofcode.com/2024/day/7
fun main() {
    val input = readInput(day = 7)
    val equations = parseInput(input)
    val sync1 = measureTimedValue { calculateSumOfTestValuesSync(equations, Part1.Operators) }
    println("Part 1 (Sync): ${sync1.value}, time: ${sync1.duration}")
    val async1 = measureTimedValue { calculateSumOfTestValuesAsync(equations, Part1.Operators) }
    println("Part 1 (Async): ${async1.value}, time: ${async1.duration}")

    val sync2 = measureTimedValue { calculateSumOfTestValuesSync(equations, Part2.Operators) }
    println("Part 2 (Sync): ${sync2.value}, time: ${sync2.duration}")
    val async2 = measureTimedValue { calculateSumOfTestValuesAsync(equations, Part2.Operators) }
    println("Part 2 (Async): ${async2.value}, time: ${async2.duration}")
}

private object Day07 {

    object Part1 {
        val Operators = listOf(Add, Multiply)
    }

    object Part2 {
        val Operators = listOf(Add, Multiply, Concatenate)
    }

    fun calculateSumOfTestValuesSync(equations: List<Equation>, operators: List<Operator>): Long =
        equations.sumOf { equation ->
            if (canTestValueBeCalculated(equation, operators)) {
                equation.testValue
            } else 0
        }

    fun calculateSumOfTestValuesAsync(equations: List<Equation>, operators: List<Operator>): Long {
        val result = AtomicLong(0)
        runBlocking(Dispatchers.Default) {
            equations.forEach { equation ->
                launch {
                    if (canTestValueBeCalculated(equation, operators)) {
                        result.addAndGet(equation.testValue)
                    }
                }
            }
        }
        return result.get()
    }

    fun parseInput(input: List<String>): List<Equation> = input.map { line ->
        val resultToNumbersSplit = line.split(":")
        val numbers = resultToNumbersSplit[1].split(" ")
            .mapNotNull { number ->
                if (number.isNotBlank()) {
                    number.toLong()
                } else null
            }
        Equation(
            testValue = resultToNumbersSplit[0].toLong(),
            numbers = numbers,
        )
    }

    fun canTestValueBeCalculated(
        equation: Equation,
        operators: List<Operator>,
    ): Boolean {
        val possibleOperatorCombos = findPossibleOperatorCombinations(equation, operators)
        for (operatorCombo in possibleOperatorCombos) {
            // Short-circuit return when we recognize one possibility.
            if (calculatesToTestValue(equation, operatorCombo)) {
                return true
            }
        }
        return false
    }

    private fun findPossibleOperatorCombinations(
        equation: Equation,
        operators: List<Operator>,
    ): List<List<Operator>> {
        val operatorSlots = equation.numbers.size - 1
        val operatorComboList = mutableListOf<List<Operator>>()
        operatorList(operators, operatorComboList, emptyList(), operatorSlots)
        return operatorComboList
    }

    private fun operatorList(
        operators: List<Operator>,
        operatorComboList: MutableList<List<Operator>>,
        currentPermutation: List<Operator>,
        operatorSlotsLeft: Int,
    ) {
        if (operatorSlotsLeft == 0) {
            operatorComboList.add(currentPermutation)
            return
        }

        operators.forEach { operator ->
            val withNewOperator = currentPermutation + operator
            operatorList(operators, operatorComboList, withNewOperator, operatorSlotsLeft - 1)
        }
    }

    private fun calculatesToTestValue(equation: Equation, operators: List<Operator>): Boolean {
        val numbers = equation.numbers
        var total = numbers[0]
        for (i in 0..<numbers.lastIndex) {
            total = operators[i].calculate(total, numbers[i + 1])
            // Short-circuit since no negative numbers in input
            if (total > equation.testValue) {
                return false
            }
        }
        return total == equation.testValue
    }

    enum class Operator {
        Add,
        Multiply,
        Concatenate;

        fun calculate(first: Long, second: Long): Long =
            when (this) {
                Add -> first + second
                Multiply -> first * second
                Concatenate -> "$first$second".toLong()
            }
    }

    data class Equation(
        val testValue: Long,
        val numbers: List<Long>,
    )
}