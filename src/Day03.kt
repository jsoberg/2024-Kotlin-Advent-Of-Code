import Day03.Part1
import Day03.Part2

// https://adventofcode.com/2024/day/3
fun main() {
    val input = readInput(day = 3)
    println("Part 1: ${Part1.calculateMulInstructions(input)}")
    println("Part 2: ${Part2.calculateMulInstructionsWithConditionals(input)}")
}

private object Day03 {

    private val MulInstructionRegex = "mul\\(\\d+,\\d+\\)".toRegex()

    object Part1 {

        fun calculateMulInstructions(input: List<String>): Int =
            input.sumOf(::calculateLine)

        fun calculateLine(line: String): Int {
            var sum = 0
            var startIndex = 0
            var match = MulInstructionRegex.find(line, startIndex = startIndex)
            while (match != null) {
                val contents = line.substring(
                    startIndex = match.range.first + 4,
                    endIndex = match.range.last,
                )
                sum += calculateMultContents(contents)

                startIndex = match.range.last
                match = MulInstructionRegex.find(line, startIndex = startIndex)
            }
            return sum
        }

        private fun calculateMultContents(multContents: String): Int {
            val numbers = multContents.split(",")
            return numbers[0].toInt() * numbers[1].toInt()
        }
    }

    object Part2 {

        private val ConditionalDontRegex = "don't\\(\\)".toRegex()
        private val ConditionalDoRegex = "do\\(\\)".toRegex()

        fun calculateMulInstructionsWithConditionals(input: List<String>): Int {
            // don't() and do() span across lines, so join all input lines into a single instruction set.
            val fullInput = input.joinToString("")
            val sanitized = sanitizeInputLine(fullInput)
            return Part1.calculateLine(sanitized)
        }

        private fun sanitizeInputLine(line: String): String {
            val sanitizedLine = StringBuilder()
            var startIndex = 0
            var match = ConditionalDontRegex.find(line, startIndex = startIndex)
            while (match != null) {
                sanitizedLine.append(line.substring(startIndex, match.range.first))

                val nextDoMatch = ConditionalDoRegex.find(line, startIndex = match.range.last)
                if (nextDoMatch != null) {
                    startIndex = nextDoMatch.range.last
                    match = ConditionalDontRegex.find(line, startIndex = startIndex)
                } else {
                    break
                }
            }
            return sanitizedLine.toString()
        }
    }
}