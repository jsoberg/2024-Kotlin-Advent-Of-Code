import Day15.Part1
import Day15.Part2
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D

// https://adventofcode.com/2024/day/15
fun main() {
    val input = readInput(day = 15)
    println("Part 1: ${Part1.calculateSmallWarehouseBoxGpsSum(input)}")
    println("Part 2: ${Part2.calculateLargeWarehouseBoxGpsSum(input)}")
}

private object Day15 {

    object Part1 {
        fun calculateSmallWarehouseBoxGpsSum(input: List<String>): Long {
            val (smallWarehouse, moves) = WarehouseGrid.Small.parseInput(input)
            return smallWarehouse.runMoves(moves)
                .calculateGpsSum()
        }
    }

    object Part2 {
        fun calculateLargeWarehouseBoxGpsSum(input: List<String>): Long {
            val (largeWarehouse, moves) = WarehouseGrid.Large.parseInput(input)
            return largeWarehouse.runMoves(moves)
                .calculateGpsSum()
        }
    }
}

private sealed interface WarehouseGrid<T> {
    fun calculateGpsSum(): Long
    fun runMoves(moves: List<Direction>): T

    // region Small Warehouse

    data class Small(
        val grid: Grid2D<Element>,
        val robotLocation: Location,
    ) : WarehouseGrid<Small> {

        override fun calculateGpsSum(): Long =
            grid.sumOf { at, element ->
                if (element == Element.Box) {
                    ((100L * at.row) + at.col)
                } else 0L
            }

        override fun runMoves(moves: List<Direction>): Small {
            var warehouse = this
            for (move in moves) {
                warehouse = warehouse.move(move)
            }
            return warehouse
        }

        /** Moves robot in [robotLocation] in [direction] and pushes out any boxes in the way, if possible. */
        fun move(direction: Direction): Small {
            val moved = robotLocation.move(direction)
            val (modifiedGrid, newRobotLocation) = when (grid[moved]) {
                Element.Wall -> grid to robotLocation
                Element.Empty -> moveToEmpty(moved) to moved
                Element.Box -> attemptToMoveBoxes(direction)
                Element.Robot -> error("Attempted move in $direction from $robotLocation has robot in new location")
            }

            return Small(
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

        companion object {
            fun parseInput(input: List<String>): Pair<Small, List<Direction>> {
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

                return Small(
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
        }
    }

    // endregion

    // region Large Warehouse

    data class Large(
        val grid: Grid2D<Element>,
        val robotLocation: Location,
    ) : WarehouseGrid<Large> {

        override fun calculateGpsSum(): Long =
            grid.sumOf { at, element ->
                if (element == Element.BoxLeft) {
                    ((100L * at.row) + at.col)
                } else 0L
            }

        override fun runMoves(moves: List<Direction>): Large {
            var warehouse = this
            /*            println("INITIAL")
                        warehouse.print()
                        println()
                        var counter = 1*/
            for (move in moves) {
                warehouse = warehouse.move(move)
                /*                println("MOVE $move: STEP $counter")
                                warehouse.print()
                                println()
                                counter++*/
            }
            return warehouse
        }

        /** Moves robot in [robotLocation] in [direction] and pushes out any boxes in the way, if possible. */
        fun move(direction: Direction): Large {
            val moved = robotLocation.move(direction)
            val (modifiedGrid, newRobotLocation) = when (grid[moved]) {
                Element.Wall -> grid to robotLocation
                Element.Empty -> moveToEmpty(moved) to moved
                Element.BoxLeft, Element.BoxRight -> attemptToMoveBoxes(direction)
                Element.Robot -> error("Attempted move in $direction from $robotLocation has robot in new location")
            }

            return Large(
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
            if (canPushBoxes(robotLocation, direction)) {
                pushBoxes(direction) to robotLocation.move(direction)
            } else {
                // Can't move.
                grid to robotLocation
            }

        private fun canPushBoxes(
            start: Location,
            direction: Direction
        ): Boolean {
            val moved = start.move(direction)
            if (moved !in grid) return false

            return when (grid[moved]) {
                Element.Empty -> true
                Element.Wall -> false
                Element.BoxLeft, Element.BoxRight -> {
                    when (direction) {
                        Direction.East, Direction.West ->
                            hasEmptySpaceHorizontally(start, direction)

                        Direction.North, Direction.South ->
                            boxesVerticallyPushableFromOrigin(origin = start, direction = direction)
                                .isNotEmpty()

                        else -> error("$direction not supported for warehouse")
                    }
                }

                Element.Robot -> error("Starting from robot location, shouldn't be hitting it again")
            }
        }

        private tailrec fun hasEmptySpaceHorizontally(
            start: Location,
            direction: Direction
        ): Boolean {
            val moved = start.move(direction)
            if (moved !in grid) return false

            return when (grid[moved]) {
                Element.Empty -> true
                Element.Wall -> false
                Element.BoxLeft, Element.BoxRight -> hasEmptySpaceHorizontally(moved, direction)
                Element.Robot -> error("Starting from robot location, shouldn't be hitting it again")
            }
        }

        // Pre-condition: We've already checked that [origin.move(direction)] contains a box
        private fun boxesVerticallyPushableFromOrigin(
            origin: Location,
            direction: Direction
        ): Set<Pair<Location, Element>> {
            val pushBoxSet = mutableSetOf<Pair<Location, Element>>()
            val startBoxLocation = origin.move(direction)
            val otherStartBoxSide = otherBoxLocation(startBoxLocation)
            pushBoxSet.add(startBoxLocation to grid[startBoxLocation])
            pushBoxSet.add(otherStartBoxSide to grid[otherStartBoxSide])

            var movedUp = startBoxLocation.move(direction)
            while (true) {
                val previousRow = movedUp.move(direction.opposite).row
                val boxesInRow = pushBoxSet.filter { (location, _) ->
                    location.row == previousRow
                }
                var emptyCount = 0
                for ((boxSide, _) in boxesInRow) {
                    val movedNextFromBoxSide = boxSide.move(direction)
                    when (val next = grid[movedNextFromBoxSide]) {
                        Element.BoxLeft, Element.BoxRight -> {
                            val movedNextOtherBoxSide = otherBoxLocation(movedNextFromBoxSide)
                            pushBoxSet.add(movedNextFromBoxSide to next)
                            pushBoxSet.add(movedNextOtherBoxSide to grid[movedNextOtherBoxSide])
                        }

                        Element.Empty -> emptyCount++
                        Element.Wall -> {
                            // If we hit a wall, back out immediately.
                            return emptySet()
                        }

                        Element.Robot -> error("Should not hit robot when moving boxes")
                    }
                }

                // All boxes in last row are pushing against empty - continue.
                if (emptyCount == boxesInRow.count()) {
                    return pushBoxSet
                }
                movedUp = movedUp.move(direction)
            }
        }

        private fun otherBoxLocation(boxSide: Location): Location {
            val element = grid[boxSide]
            val otherBoxSideDirection = if (element == Element.BoxLeft) {
                Direction.East
            } else Direction.West
            return boxSide.move(otherBoxSideDirection)
        }

        private fun pushBoxes(direction: Direction): Grid2D<Element> =
            when (direction) {
                Direction.East, Direction.West -> horizontalPush(direction)
                Direction.North, Direction.South -> verticalPush(direction)
                else -> error("$direction not supported for warehouse")
            }

        private fun horizontalPush(direction: Direction) = grid.modify { gridList ->
            val endLocation = robotLocation.move(direction)
            var currentBoxSide = gridList[endLocation.row][endLocation.col]
            gridList[endLocation.row][endLocation.col] = Element.Robot
            gridList[robotLocation.row][robotLocation.col] = Element.Empty

            var boxLocation = endLocation.move(direction)
            while (grid[boxLocation] != Element.Empty) {
                val boxSideToSet = currentBoxSide
                currentBoxSide = grid[boxLocation]
                gridList[boxLocation.row][boxLocation.col] = boxSideToSet
                boxLocation = boxLocation.move(direction)
            }
            gridList[boxLocation.row][boxLocation.col] = currentBoxSide
        }

        private fun verticalPush(direction: Direction) = grid.modify { gridList ->
            val boxLocationsToPush =
                boxesVerticallyPushableFromOrigin(origin = robotLocation, direction = direction)
            // Push all boxes up one.
            for ((location, boxSide) in boxLocationsToPush.reversed()) {
                val moved = location.move(direction)
                gridList[location.row][location.col] = Element.Empty
                gridList[moved.row][moved.col] = boxSide
            }
            // Now move the robot.
            val moved = robotLocation.move(direction)
            gridList[robotLocation.row][robotLocation.col] = Element.Empty
            gridList[moved.row][moved.col] = Element.Robot
        }

        fun print() = grid.print()

        enum class Element(val char: Char) {
            Wall('#'),
            BoxLeft('['),
            BoxRight(']'),
            Robot('@'),
            Empty('.');

            override fun toString(): String = char.toString()
        }

        companion object {
            fun parseInput(input: List<String>): Pair<Large, List<Direction>> {
                var robotLocation: Location? = null
                val gridRows = mutableListOf<List<Element>>()
                val moves = mutableListOf<Direction>()
                for (line in input) {
                    when {
                        (line.startsWith('#')) -> {
                            val gridRow = parseGridRow(expandLine(line))
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

                return Large(
                    grid = gridRows.toGrid2D(),
                    robotLocation = robotLocation ?: error("No robot found in warhouse"),
                ) to moves
            }

            private fun expandLine(line: String): String = buildString {
                for (char in line) {
                    when (char) {
                        '@' -> append("@.")
                        'O' -> append("[]")
                        '#' -> append("##")
                        '.' -> append("..")
                    }
                }
            }

            private fun parseGridRow(line: String): List<Element> = line.toCharArray().map { char ->
                when (char) {
                    '[' -> Element.BoxLeft
                    ']' -> Element.BoxRight
                    '#' -> Element.Wall
                    '@' -> Element.Robot
                    else -> Element.Empty
                }
            }
        }
    }

    // endregion

    companion object {
        fun parseMoves(line: String) = line.toCharArray().map { char ->
            when (char) {
                'v' -> Direction.South
                '^' -> Direction.North
                '>' -> Direction.East
                '<' -> Direction.West
                else -> error("Unknown char $char found when parsing moves")
            }
        }
    }
}