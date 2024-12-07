import Day07.Part1
import Day07.parseInput

// https://adventofcode.com/2024/day/7
fun main() {
    val input = readInput(day = 7)
    val equations = parseInput(input)
    println("Part 1: ${Part1.calculateSumOfTestValues(equations)}")
}

private object Day07 {

    object Part1 {
        fun calculateSumOfTestValues(equations: List<Equation>): Long =
            equations.sumOf { equation ->
                if (canTestValueBeCalculated(equation)) {
                    equation.testValue
                } else 0
            }

        private fun canTestValueBeCalculated(equation: Equation): Boolean {
            val possibleOperatorCombos = findPossibleOperatorCombinations(equation)
            for (operatorCombo in possibleOperatorCombos) {
                // Short-circuit return when we recognize one possibility.
                if (calculatesToTestValue(equation, operatorCombo)) {
                    return true
                }
            }
            return false
        }

        private fun findPossibleOperatorCombinations(equation: Equation): List<List<Operator>> {
            val operatorSlots = equation.numbers.size - 1
            val operatorComboList = mutableListOf<List<Operator>>()
            operatorList(operatorComboList, emptyList(), operatorSlots)
            return operatorComboList
        }

        private fun operatorList(
            operatorComboList: MutableList<List<Operator>>,
            currentPermutation: List<Operator>,
            operatorSlotsLeft: Int,
        ) {
            if (operatorSlotsLeft == 0) {
                operatorComboList.add(currentPermutation)
                return
            }

            Operator.entries.forEach { operator ->
                val withNewOperator = currentPermutation + operator
                operatorList(operatorComboList, withNewOperator, operatorSlotsLeft - 1)
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

    enum class Operator {
        Add,
        Multiply;

        fun calculate(first: Long, second: Long): Long =
            when (this) {
                Add -> first + second
                Multiply -> first * second
            }
    }

    data class Equation(
        val testValue: Long,
        val numbers: List<Long>,
    )
}