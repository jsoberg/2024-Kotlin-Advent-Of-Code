import Day11.Part1
import Day11.Part2
import Day11.parseInput

// https://adventofcode.com/2024/day/11
fun main() {
    val input = readInput(day = 11).first()
    val stones = parseInput(input)
    println("Part 1: ${Part1.calculateNumberOfStonesAfterBlinks(stones, blinks = 25)}")
    println("Part 2: ${Part2(blinks = 75).calculateNumberOfStonesAfterBlinksWithCache(stones)}")
}

private object Day11 {

    object Part1 {

        fun calculateNumberOfStonesAfterBlinks(stones: List<Long>, blinks: Int): Int {
            var blinkedStones = stones
            (1..blinks).forEach { _ ->
                blinkedStones = runBlink(blinkedStones)
            }
            return blinkedStones.count()
        }

        private fun runBlink(stones: List<Long>): List<Long> = buildList {
            for (stone in stones) {
                addAll(applyRule(stone))
            }
        }

        private fun applyRule(stone: Long): List<Long> = when {
            (stone == 0L) -> listOf(1)
            ("$stone".length % 2 == 0) -> splitEvenStone(stone)
            else -> listOf(stone * 2024)
        }
    }

    class Part2(
        private val blinks: Int,
    ) {
        private val stoneCountCache = hashMapOf<Pair<Long, Int>, Long>()

        fun calculateNumberOfStonesAfterBlinksWithCache(stones: List<Long>): Long {
            return stones.sumOf { stone ->
                countStonesForBlink(stone, blinks)
            }
        }

        private fun countStonesForBlink(
            stone: Long,
            blinksRemaining: Int,
        ): Long {
            stoneCountCache.getOrPut(stone to blinksRemaining) {
                when {
                    blinksRemaining == 0 -> 1L
                    stone == 0L -> countStonesForBlink(stone = 1L, blinksRemaining - 1)
                    ("$stone".length % 2 == 0) -> {
                        splitEvenStone(stone).sumOf { halfStone ->
                            countStonesForBlink(halfStone, blinksRemaining - 1)
                        }
                    }

                    else -> countStonesForBlink(stone * 2024, blinksRemaining - 1)
                }
            }
            return stoneCountCache[stone to blinksRemaining]
                ?: error("Count should be in cache at this point")
        }
    }

    fun splitEvenStone(stone: Long): List<Long> {
        val stoneString = "$stone"
        return listOf(
            stoneString.substring(0, stoneString.length / 2).toLong(),
            stoneString.substring(stoneString.length / 2).toLong(),
        )
    }

    fun parseInput(input: String): List<Long> =
        input.split(" ").map { it.toLong() }
}