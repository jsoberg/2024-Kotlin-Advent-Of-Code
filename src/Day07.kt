import Day07.Operator.Add
import Day07.Operator.Concatenate
import Day07.Operator.Multiply
import Day07.Part1
import Day07.Part2
import Day07.calculateSumOfTestValuesAsync
import Day07.calculateSumOfTestValuesAsyncString
import Day07.calculateSumOfTestValuesSync
import Day07.calculateSumOfTestValuesSyncString
import Day07.parseInput
import com.soberg.aoc.utlities.extensions.asyncSumOfBlocking
import kotlin.math.pow
import kotlin.time.measureTimedValue

// https://adventofcode.com/2024/day/7
fun main() {
    val input = readInput(day = 7)
    val equations = parseInput(input)
    val sync1 = measureTimedValue { calculateSumOfTestValuesSync(equations, Part1.Operators) }
    println("Part 1 (Sync): ${sync1.value}, time: ${sync1.duration}")
    val async1 = measureTimedValue { calculateSumOfTestValuesAsync(equations, Part1.Operators) }
    println("Part 1 (Async): ${async1.value}, time: ${async1.duration}")
    val syncString1 =
        measureTimedValue { calculateSumOfTestValuesSyncString(equations, Part1.Operators) }
    print("\n")
    println("Part 1 (Sync) (String Permutations): ${syncString1.value}, time: ${syncString1.duration}")
    val asyncString1 =
        measureTimedValue { calculateSumOfTestValuesAsyncString(equations, Part1.Operators) }
    println("Part 1 (Async) (String Permutations): ${asyncString1.value}, time: ${asyncString1.duration}")

    print("\n\n")

    val sync2 = measureTimedValue { calculateSumOfTestValuesSync(equations, Part2.Operators) }
    println("Part 2 (Sync): ${sync2.value}, time: ${sync2.duration}")
    val async2 = measureTimedValue { calculateSumOfTestValuesAsync(equations, Part2.Operators) }
    println("Part 2 (Async): ${async2.value}, time: ${async2.duration}")
    val syncString2 =
        measureTimedValue { calculateSumOfTestValuesSyncString(equations, Part2.Operators) }
    print("\n")
    println("Part 2 (Sync) (String Permutations): ${syncString2.value}, time: ${syncString2.duration}")
    val asyncString2 =
        measureTimedValue { calculateSumOfTestValuesAsyncString(equations, Part2.Operators) }
    println("Part 2 (Async) (String Permutations): ${asyncString2.value}, time: ${asyncString2.duration}")
}

private object Day07 {

    object Part1 {
        val Operators = listOf(Add, Multiply)
    }

    object Part2 {
        val Operators = listOf(Add, Multiply, Concatenate)
    }

    //region Recursive list permutations

    fun calculateSumOfTestValuesSync(equations: List<Equation>, operators: List<Operator>): Long =
        equations.sumOf { equation ->
            if (canTestValueBeCalculated(equation, operators)) {
                equation.testValue
            } else 0
        }

    fun calculateSumOfTestValuesAsync(equations: List<Equation>, operators: List<Operator>): Long =
        equations.asyncSumOfBlocking { equation ->
            if (canTestValueBeCalculated(equation, operators)) {
                equation.testValue
            } else 0
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
            total = operators[i](total, numbers[i + 1])
            // Short-circuit since no negative numbers in input
            if (total > equation.testValue) {
                return false
            }
        }
        return total == equation.testValue
    }

    //endregion

    //region String permutations

    fun calculateSumOfTestValuesSyncString(
        equations: List<Equation>,
        operators: List<Operator>
    ): Long =
        equations.sumOf { equation ->
            if (canTestValueBeCalculatedStringPermutations(equation, operators)) {
                equation.testValue
            } else 0
        }

    fun calculateSumOfTestValuesAsyncString(
        equations: List<Equation>,
        operators: List<Operator>
    ): Long = equations.asyncSumOfBlocking { equation ->
        if (canTestValueBeCalculatedStringPermutations(equation, operators)) {
            equation.testValue
        } else 0
    }

    fun canTestValueBeCalculatedStringPermutations(
        equation: Equation,
        operators: List<Operator>,
    ): Boolean {
        val numbers = equation.numbers
        val possibleOperatorCombos = operators.size.toDouble().pow(numbers.size - 1).toInt()
        for (i in 0..<possibleOperatorCombos) {
            val operatorCombo = i.toUInt().toString(radix = operators.size)
                .padStart(numbers.size - 1, '0')
            if (calculatesToTestValue(equation, stringToOperatorList(operatorCombo))) {
                return true
            }
        }
        return false
    }

    fun stringToOperatorList(operatorCombo: String) =
        operatorCombo.toCharArray().map { operatorChar ->
            Operator.entries[operatorChar.digitToInt()]
        }

    //endregion

    enum class Operator {
        Add,
        Multiply,
        Concatenate;

        operator fun invoke(first: Long, second: Long): Long =
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