import Day06.Part1
import Day06.Part2
import Day06.PatrolGrid.LocationType
import Day06.parseInput
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTimedValue

// https://adventofcode.com/2024/day/6
fun main() {
    val input = readInput(day = 6)
    val patrolGrid = parseInput(input)
    println("Part 1: ${Part1.calculateDistinctPositionCount(patrolGrid)}")

    val part2SolutionSync =
        measureTimedValue { Part2.calculateDistinctObstacleLoopsSync(patrolGrid) }
    println("Part 2 (Sync): ${part2SolutionSync.value}, time: ${part2SolutionSync.duration}")

    val part2SolutionAsync =
        measureTimedValue { Part2.calculateDistinctObstacleLoopsAsync(patrolGrid) }
    println("Part 2 (Async): ${part2SolutionAsync.value}, time: ${part2SolutionAsync.duration}")
}

private object Day06 {

    object Part1 {

        fun calculateDistinctPositionCount(patrolGrid: PatrolGrid): Int {
            // Counting the starting position.
            var positionCount = 1
            // Track what locations the guard has moved in.
            val hitLocations = hashSetOf<Location>().apply {
                add(patrolGrid.guardLocation)
            }
            walkThroughGrid(
                grid = patrolGrid.grid,
                start = patrolGrid.guardLocation
            )
            { location, _ ->
                if (!hitLocations.contains(location)) {
                    hitLocations.add(location)
                    positionCount++
                }
                true
            }
            return positionCount
        }
    }

    object Part2 {
        fun calculateDistinctObstacleLoopsSync(patrolGrid: PatrolGrid): Int {
            var numLoops = 0
            val originalGrid = patrolGrid.grid
            for (row in 0..<originalGrid.rowSize) {
                for (col in 0..<originalGrid.colSize) {
                    val location = Location(row, col)
                    if (originalGrid[location] == LocationType.Open
                        && createsLoopForObstacleAt(patrolGrid, location)
                    ) {
                        numLoops++
                    }
                }
            }
            return numLoops
        }

        fun calculateDistinctObstacleLoopsAsync(patrolGrid: PatrolGrid): Int {
            val numLoops = AtomicInteger(0)
            val originalGrid = patrolGrid.grid
            runBlocking(Dispatchers.Default) {
                for (row in 0..<originalGrid.rowSize) {
                    for (col in 0..<originalGrid.colSize) {
                        launch {
                            val location = Location(row, col)
                            if (originalGrid[location] == LocationType.Open
                                && createsLoopForObstacleAt(patrolGrid, location)
                            ) {
                                numLoops.incrementAndGet()
                            }
                        }
                    }
                }
            }
            return numLoops.get()
        }

        private fun createsLoopForObstacleAt(
            patrolGrid: PatrolGrid,
            newObstacleLocation: Location,
        ): Boolean {
            // Track what vectors the guard has moved in.
            val movedVectors = hashSetOf<Pair<Location, Direction>>().apply {
                add(patrolGrid.guardLocation to Direction.North)
            }
            walkThroughGrid(
                grid = patrolGrid.grid.replace(newObstacleLocation, LocationType.Obstacle),
                start = patrolGrid.guardLocation
            )
            { location, direction ->
                if (!movedVectors.contains(location to direction)) {
                    movedVectors.add(location to direction)
                    true
                } else {
                    return@createsLoopForObstacleAt true
                }
            }
            return false
        }
    }

    fun parseInput(input: List<String>): PatrolGrid {
        var guardLocation: Location? = null
        val grid = input.mapIndexed { row, line ->
            line.mapIndexed { col, char ->
                val type = toLocationType(char)
                if (type == LocationType.Guard) {
                    guardLocation = Location(row = row, col = col)
                }
                type
            }
        }.toGrid2D()
        require(guardLocation != null) {
            "No starting guard location found"
        }
        return PatrolGrid(guardLocation!!, grid)
    }

    private fun toLocationType(char: Char): LocationType = when (char) {
        '^' -> LocationType.Guard
        '.' -> LocationType.Open
        else -> LocationType.Obstacle
    }

    inline fun walkThroughGrid(
        grid: Grid2D<LocationType>,
        start: Location,
        continueForMove: (Location, Direction) -> Boolean,
    ) {
        var location = start
        // Start is always North.
        var direction = Direction.North

        do {
            val moved = location.move(direction)
            // We've moved out of bounds, so just return.
            if (!grid.isInBounds(moved)) {
                return
            }

            when (grid[moved]) {
                LocationType.Open, LocationType.Guard -> {
                    // Increment location.
                    location = moved
                }

                LocationType.Obstacle -> {
                    // We've hit an obstacle and need to turn.
                    direction = direction.turnRight()
                }
            }
        } while (continueForMove(location, direction))
    }

    private fun Direction.turnRight() = when (this) {
        Direction.North -> Direction.East
        Direction.East -> Direction.South
        Direction.South -> Direction.West
        Direction.West -> Direction.North
        else -> error("Can't use diagonals for this use case")
    }

    data class PatrolGrid(
        val guardLocation: Location,
        val grid: Grid2D<LocationType>,
    ) {
        enum class LocationType {
            Guard,
            Obstacle,
            Open,
        }
    }
}