import Day01.Part1.calculateTotalDistance
import Day01.Part2.calculateSimilarityScore
import kotlin.math.absoluteValue

// https://adventofcode.com/2024/day/1
fun main() {
    val input = readInput(day = 1)
    println("Part 1: ${calculateTotalDistance(input)}")
    println("Part 2: ${calculateSimilarityScore(input)}")
}

private object Day01 {

    object Part1 {
        fun calculateTotalDistance(input: List<String>): Int {
            val (leftIds, rightIds) = parseLists(input)
            val sortedLeftIds = leftIds.sorted()
            val sortedRightIds = rightIds.sorted()
            var totalDistance = 0
            for (i in sortedLeftIds.indices) {
                totalDistance += ((sortedRightIds[i] - sortedLeftIds[i]).absoluteValue)
            }
            return totalDistance
        }
    }

    object Part2 {
        fun calculateSimilarityScore(input: List<String>): Int {
            val (leftIds, rightIds) = parseLists(input)
            val rightOccurrenceCountMap = occurrenceCountMap(rightIds)

            var similarityScore = 0
            leftIds.forEach { locationId ->
                val rightCount = rightOccurrenceCountMap[locationId] ?: 0
                similarityScore += (locationId * rightCount)
            }
            return similarityScore
        }

        fun occurrenceCountMap(list: List<Int>): Map<Int, Int> {
            val occurrenceCountMap = hashMapOf<Int, Int>()
            list.forEach { locationId ->
                val currentCount = occurrenceCountMap[locationId] ?: 0
                occurrenceCountMap[locationId] = currentCount + 1
            }
            return occurrenceCountMap
        }
    }

    fun parseLists(input: List<String>): HistoricalLocationIdLists {
        val leftIds = mutableListOf<Int>()
        val rightIds = mutableListOf<Int>()
        input.forEach { line ->
            val numbers = line.split("   ")
            leftIds.add(numbers[0].toInt())
            rightIds.add(numbers[1].toInt())
        }
        return HistoricalLocationIdLists(leftIds, rightIds)
    }

    data class HistoricalLocationIdLists(
        val leftIds: List<Int>,
        val rightIds: List<Int>,
    )
}
