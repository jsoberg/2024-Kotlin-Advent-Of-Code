import Day14.BathroomGrid.Quadrant.LowerLeft
import Day14.BathroomGrid.Quadrant.LowerRight
import Day14.BathroomGrid.Quadrant.UpperLeft
import Day14.BathroomGrid.Quadrant.UpperRight
import Day14.BathroomGrid.Robot
import Day14.Part1
import Day14.Part2
import Day14.parseInput

// https://adventofcode.com/2024/day/14
fun main() {
    val input = readInput(day = 14)
    val grid = parseInput(w = 101, h = 103, input)
    println("Part 1: ${Part1.calculateSafetyFactor(grid, steps = 100)}")
    println("Part 2: ${Part2.findChristmasTree(grid)}")
}

private object Day14 {

    object Part1 {

        fun calculateSafetyFactor(grid: BathroomGrid, steps: Int): Int {
            val newGrid = grid.afterSteps(steps)
            return newGrid.safetyFactor
        }
    }

    object Part2 {

        // "Hail mary" solution for finding the christmas tree.
        fun findChristmasTree(grid: BathroomGrid): Int {
            var longestContinuousRow = 0
            var longestContinuousRowStep = 0
            // Arbitrarily large count to check.
            for (step in 1..100_000) {
                val longestRow = grid.afterSteps(step).longestContinuousRow()
                if (longestRow > longestContinuousRow) {
                    longestContinuousRow = longestRow
                    longestContinuousRowStep = step
                }
            }
            grid.afterSteps(longestContinuousRowStep).print()
            return longestContinuousRowStep
        }
    }

    fun parseInput(w: Int, h: Int, input: List<String>): BathroomGrid = BathroomGrid(
        width = w,
        height = h,
        robots = input.map(::parseRobot)
    )

    private fun parseRobot(line: String): Robot {
        val pAndV = line.split(" ")
        val pValues = pAndV[0].substring(2).split(",")
        val vValues = pAndV[1].substring(2).split(",")
        return Robot(
            position = Robot.Position(
                x = pValues[0].toInt(),
                y = pValues[1].toInt(),
            ),
            velocity = Robot.Velocity(
                x = vValues[0].toInt(),
                y = vValues[1].toInt(),
            )
        )
    }

    data class BathroomGrid(
        val width: Int,
        val height: Int,
        val robots: List<Robot>,
    ) {

        fun afterSteps(steps: Int): BathroomGrid = copy(
            robots = robots.map { it.moveRobot(steps) }
        )

        private fun Robot.moveRobot(steps: Int) = copy(
            position = Robot.Position(
                x = (position.x + (velocity.x * steps)).mod(width),
                y = (position.y + (velocity.y * steps)).mod(height),
            )
        )

        val safetyFactor: Int
            get() = quadrantCounts().values
                .reduce { acc, next -> acc * next }

        fun quadrantCounts(): Map<Quadrant, Int> {
            val halfHeight = height / 2
            val halfWidth = width / 2
            val qMap = mutableMapOf<Quadrant, Int>()

            fun inc(q: Quadrant) {
                qMap[q] = (qMap[q] ?: 0) + 1
            }

            robots.forEach { robot ->
                val x = robot.position.x
                val y = robot.position.y
                when {
                    x in 0..<halfWidth && y in 0..<halfHeight -> inc(UpperLeft)
                    x in (halfWidth + 1)..<width && y in 0..<halfHeight -> inc(UpperRight)
                    x in 0..<halfWidth && y in (halfHeight + 1)..<height -> inc(LowerLeft)
                    x in (halfWidth + 1)..<width && y in (halfHeight + 1)..<height -> inc(LowerRight)
                }
            }
            return qMap
        }

        fun longestContinuousRow(): Int {
            val rowToPosition = robots.map { it.position }
                .groupBy { it.y }
            var longestRow = -1
            rowToPosition.entries.forEach { (row, positions) ->
                val firstToLast = positions.map { it.x }.sorted()
                var currentRow = 0
                for (i in 1..firstToLast.lastIndex) {
                    val previous = firstToLast[i - 1]
                    val current = firstToLast[i]
                    when {
                        previous == current - 1 -> {
                            currentRow++
                            if (currentRow > longestRow) {
                                longestRow = currentRow
                            }
                        }

                        else -> currentRow = 1
                    }
                }
            }
            return longestRow
        }

        fun print() {
            val positionMap = robots.map { it.position }
                .groupBy { it.x to it.y }
            for (col in 0..<width) {
                for (row in 0..<height) {
                    val robotPositions = positionMap[col to row]
                    if (robotPositions.isNullOrEmpty()) {
                        print('.')
                    } else print(robotPositions.size)
                }
                println()
            }
        }

        enum class Quadrant {
            UpperLeft,
            UpperRight,
            LowerLeft,
            LowerRight,
        }

        data class Robot(
            val position: Position,
            val velocity: Velocity,
        ) {

            data class Position(
                val x: Int,
                val y: Int,
            )

            data class Velocity(
                val x: Int,
                val y: Int,
            )
        }
    }
}