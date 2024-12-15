import Day15.Part1
import Day15.WarehouseGrid.Element
import Day15.parseInput
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D

// https://adventofcode.com/2024/day/15
fun main() {
    val input = readInput(day = 15)
    val (warehouse, moves) = parseInput(input)
    println("Part 1: ${Part1.calculateBoxGpsSum(warehouse, moves)}")
}

private object Day15 {

    object Part1 {

        fun calculateBoxGpsSum(warehouse: WarehouseGrid, moves: List<Direction>): Long {
            val warehouseAfterMoves = runMoves(warehouse, moves)
            return calculateGpsSum(warehouseAfterMoves)
        }

        private fun calculateGpsSum(warehouse: WarehouseGrid): Long =
            warehouse.grid.sumOf { at, element ->
                if (element == Element.Box) {
                    ((100L * at.row) + at.col)
                } else 0L
            }
    }

    fun runMoves(original: WarehouseGrid, moves: List<Direction>): WarehouseGrid {
        var warehouse = original
        for (move in moves) {
            warehouse = warehouse.move(move)
        }
        return warehouse
    }

    fun parseInput(input: List<String>): Pair<WarehouseGrid, List<Direction>> {
        var robotLocation: Location? = null
        val gridRows = mutableListOf<List<Element>>()
        val moves = mutableListOf<Direction>()
        for (line in input) {
            when {
                (line.startsWith('#')) -> {
                    val gridRow = parseGridRow(line)
                    val robotCol = gridRow.indexOf(Element.Robot)
                    if (robotCol != -1) {
                        robotLocation = Location(row = gridRows.size, col = robotCol)
                    }
                    gridRows.add(gridRow)
                }

                line.isEmpty() -> continue

                else -> {
                    moves.addAll(parseMoves(line))
                }
            }
        }

        return WarehouseGrid(
            grid = gridRows.toGrid2D(),
            robotLocation = robotLocation ?: error("No robot found in warhouse"),
        ) to moves
    }

    private fun parseGridRow(line: String): List<Element> = line.toCharArray().map { char ->
        when (char) {
            'O' -> Element.Box
            '#' -> Element.Wall
            '@' -> Element.Robot
            else -> Element.Empty
        }
    }

    private fun parseMoves(line: String) = line.toCharArray().map { char ->
        when (char) {
            'v' -> Direction.South
            '^' -> Direction.North
            '>' -> Direction.East
            '<' -> Direction.West
            else -> error("Unknown char $char found when parsing moves")
        }

    }

    data class WarehouseGrid(
        val grid: Grid2D<Element>,
        val robotLocation: Location,
    ) {

        /** Moves robot in [robotLocation] in [direction] and pushes out any boxes in the way, if possible. */
        fun move(direction: Direction): WarehouseGrid {
            val moved = robotLocation.move(direction)
            val (modifiedGrid, newRobotLocation) = when (grid[moved]) {
                Element.Wall -> grid to robotLocation
                Element.Empty -> moveToEmpty(moved) to moved
                Element.Box -> attemptToMoveBoxes(direction)
                Element.Robot -> error("Attempted move in $direction from $robotLocation has robot in new location")
            }

            return WarehouseGrid(
                grid = modifiedGrid,
                robotLocation = newRobotLocation,
            )
        }

        private fun moveToEmpty(newLocation: Location): Grid2D<Element> =
            grid.modify { gridList ->
                gridList[newLocation.row][newLocation.col] = Element.Robot
                gridList[robotLocation.row][robotLocation.col] = Element.Empty
            }

        private fun attemptToMoveBoxes(direction: Direction): Pair<Grid2D<Element>, Location> =
            if (hasEmptySpaceInDirection(robotLocation, direction)) {
                pushBoxes(direction) to robotLocation.move(direction)
            } else {
                // Can't move.
                grid to robotLocation
            }

        private tailrec fun hasEmptySpaceInDirection(
            start: Location,
            direction: Direction
        ): Boolean {
            val moved = start.move(direction)
            if (moved !in grid) return false

            return when (grid[moved]) {
                Element.Empty -> true
                Element.Wall -> false
                Element.Box -> hasEmptySpaceInDirection(moved, direction)
                Element.Robot -> error("Starting from robot location, shouldn't be hitting it again")
            }
        }

        private fun pushBoxes(direction: Direction): Grid2D<Element> =
            grid.modify { gridList ->
                val endLocation = robotLocation.move(direction)
                gridList[endLocation.row][endLocation.col] = Element.Robot
                gridList[robotLocation.row][robotLocation.col] = Element.Empty

                var boxLocation = endLocation.move(direction)
                while (grid[boxLocation] != Element.Empty) {
                    boxLocation = boxLocation.move(direction)
                }
                gridList[boxLocation.row][boxLocation.col] = Element.Box
            }

        fun print() = grid.print()

        enum class Element(val char: Char) {
            Wall('#'),
            Box('O'),
            Robot('@'),
            Empty('.');

            override fun toString(): String = char.toString()
        }
    }
}