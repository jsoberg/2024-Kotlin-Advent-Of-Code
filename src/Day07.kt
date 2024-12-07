import Day07.Operator.Add
import Day07.Operator.Concatenate
import Day07.Operator.Multiply
import Day07.Part1
import Day07.Part2
import Day07.parseInput

// https://adventofcode.com/2024/day/7
fun main() {
    val input = readInput(day = 7)
    val equations = parseInput(input)
    println("Part 1: ${Part1.calculateSumOfTestValues(equations)}")
    println("Part 1: ${Part2.calculateSumOfTestValues(equations)}")
}

private object Day07 {

    object Part1 {
        fun calculateSumOfTestValues(equations: List<Equation>): Long =
            equations.sumOf { equation ->
                if (canTestValueBeCalculated(equation, listOf(Add, Multiply))) {
                    equation.testValue
                } else 0
            }
    }

    object Part2 {
        fun calculateSumOfTestValues(equations: List<Equation>): Long =
            equations.sumOf { equation ->
                if (canTestValueBeCalculated(equation, listOf(Add, Multiply, Concatenate))) {
                    equation.testValue
                } else 0
            }
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