import Day05.Part1
import Day05.Part2
import Day05.SafetyManualPrintUpdate.PageOrderingRules
import Day05.parseInput

// https://adventofcode.com/2024/day/5
fun main() {
    val input = readInput(day = 5)
    val printUpdate = parseInput(input)
    println("Part 1: ${Part1.correctlyOrderedMiddleNumberSum(printUpdate)}")
    println("Part 2: ${Part2.incorrectlyOrderedMiddleNumberSum(printUpdate)}")
}

private object Day05 {

    object Part1 {
        fun correctlyOrderedMiddleNumberSum(update: SafetyManualPrintUpdate): Int {
            val correctlyOrdered = update.pagesToProduce.filter { pages ->
                isCorrectlyOrdered(pages, update.orderingRules)
            }
            return middleNumberSum(correctlyOrdered)
        }
    }

    object Part2 {
        fun incorrectlyOrderedMiddleNumberSum(update: SafetyManualPrintUpdate): Int {
            val comparator = PageOrderingComparator(update.orderingRules)
            val reOrderedPages = update.pagesToProduce.filterNot { pages ->
                isCorrectlyOrdered(pages, update.orderingRules)
            }.map { pages ->
                pages.sortedWith(comparator)
            }
            return middleNumberSum(reOrderedPages)
        }

        class PageOrderingComparator(
            private val orderingRules: PageOrderingRules,
        ) : Comparator<Int> {
            override fun compare(first: Int, second: Int): Int = when {
                first == second -> 0
                orderingRules.isBefore(first, second) -> -1
                else -> 1
            }
        }
    }

    fun parseInput(input: List<String>): SafetyManualPrintUpdate {
        val comesBeforeMap = hashMapOf<Int, Set<Int>>()
        val pagesToProduce = mutableListOf<List<Int>>()
        input.forEach { line ->
            when {
                line.contains('|') -> {
                    val split = line.split("|")
                    val before = split[0].toInt()
                    val after = split[1].toInt()
                    val current = comesBeforeMap[before]
                    comesBeforeMap[before] = (current ?: hashSetOf()) + after
                }

                line.contains(',') -> {
                    pagesToProduce.add(line.split(",").map { it.toInt() })
                }
            }
        }
        return SafetyManualPrintUpdate(
            orderingRules = PageOrderingRules(comesBeforeMap),
            pagesToProduce = pagesToProduce,
        )
    }

    fun isCorrectlyOrdered(
        pages: List<Int>,
        orderingRules: PageOrderingRules,
    ): Boolean {
        for (i in pages.lastIndex downTo 1) {
            val current = pages[i]
            val before = pages[i - 1]
            val isBeforeCurrent = orderingRules.isBefore(before, current)
            if (!isBeforeCurrent) {
                return false
            }
        }
        return true
    }

    fun middleNumberSum(pagesToProduce: List<List<Int>>): Int =
        pagesToProduce.sumOf { pages ->
            pages[pages.size / 2]
        }

    data class SafetyManualPrintUpdate(
        val orderingRules: PageOrderingRules,
        val pagesToProduce: List<List<Int>>,
    ) {
        data class PageOrderingRules(
            val comesBefore: Map<Int, Set<Int>>,
        ) : Map<Int, Set<Int>> by comesBefore {

            fun isBefore(page: Int, otherPage: Int): Boolean =
                comesBefore[page]?.contains(otherPage) ?: false

        }
    }
}