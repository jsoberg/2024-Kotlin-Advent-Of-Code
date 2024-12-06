import Day04.Part1
import Day04.Part2
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D

// https://adventofcode.com/2024/day/4
fun main() {
    val input = readInput(day = 4).toGrid2D { line ->
        line.toCharArray().asList()
    }
    println("Part 1: ${Part1.calculateNumberOfWords(input)}")
    println("Part 2: ${Part2.calculateNumberOfXmasCrosses(input)}")
}

private object Day04 {

    object Part1 {
        private const val Word = "XMAS"
        private const val ReverseWord = "SAMX"

        fun calculateNumberOfWords(grid: Grid2D<Char>): Int {
            var sum = 0
            grid.traverse { location ->
                sum += wordCountForGridLocation(grid, location)
            }
            return sum
        }

        private fun wordCountForGridLocation(grid: Grid2D<Char>, location: Location): Int =
            DirectionsToCheck.count { direction ->
                isWordOrReversePresent(grid, location, direction)
            }

        private val DirectionsToCheck = setOf(
            Direction.East,
            Direction.South,
            Direction.SouthEast,
            Direction.SouthWest,
        )

        private fun isWordOrReversePresent(
            grid: Grid2D<Char>,
            location: Location,
            direction: Direction
        ): Boolean {
            val word = grid.collectWord(location, direction, 4)
            return (word == Word || word == ReverseWord)
        }
    }

    object Part2 {
        private const val Word = "MAS"
        private const val ReverseWord = "SAM"

        fun calculateNumberOfXmasCrosses(grid: Grid2D<Char>): Int {
            var sum = 0
            grid.traverse { location ->
                if (xmasCrossCheckForGridStep(grid, location)) {
                    sum++
                }
            }
            return sum
        }

        private fun xmasCrossCheckForGridStep(grid: Grid2D<Char>, location: Location): Boolean =
            isWordOrReversePresent(grid, location, Direction.SouthEast) &&
                    leftDiagonalXmasCheck(grid, location)

        private fun leftDiagonalXmasCheck(grid: Grid2D<Char>, location: Location): Boolean {
            // For the X, we need to move 2 columns over from current start.
            val start = location.move(Direction.East, 2)
            return isWordOrReversePresent(grid, start, Direction.SouthWest)
        }

        private fun isWordOrReversePresent(
            grid: Grid2D<Char>,
            location: Location,
            direction: Direction
        ): Boolean {
            val word = grid.collectWord(location, direction, 3)
            return (word == Word || word == ReverseWord)
        }
    }

    private fun Grid2D<Char>.collectWord(from: Location, direction: Direction, distance: Int) =
        collect(from, direction, distance)
            ?.joinToString("") ?: ""
}