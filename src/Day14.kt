import Day14.BathroomGrid.Quadrant.LowerLeft
import Day14.BathroomGrid.Quadrant.LowerRight
import Day14.BathroomGrid.Quadrant.UpperLeft
import Day14.BathroomGrid.Quadrant.UpperRight
import Day14.BathroomGrid.Robot
import Day14.Part1
import Day14.parseInput

// https://adventofcode.com/2024/day/14
fun main() {
    val input = readInput(day = 14)
    val grid = parseInput(w = 101, h = 103, input)
    println("Part 1: ${Part1.calculateSafetyFactor(grid, steps = 100)}")
}

private object Day14 {

    object Part1 {

        fun calculateSafetyFactor(grid: BathroomGrid, steps: Int): Int {
            val newGrid = grid.afterSteps(steps)
            val qCounts = newGrid.quadrantCounts()
            return qCounts.values.reduce { acc, next ->
                acc * next
            }
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