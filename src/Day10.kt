import Day10.Part1
import Day10.Part2
import Day10.parseInput
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D
import com.soberg.aoc.utlities.extensions.asyncSumOfBlocking

// https://adventofcode.com/2024/day/10
fun main() {
    val input = readInput(day = 10)
    val map = parseInput(input)
    println("Part 1: ${Part1.calculateTrailheadScoreSum(map)}")
    println("Part 2: ${Part2.calculateTrailheadScoreSum(map)}")
}

private object Day10 {

    object Part1 {
        fun calculateTrailheadScoreSum(map: TopographicMap): Int =
            map.trailheadLocations.asyncSumOfBlocking { trailhead ->
                findValidTrailEndLocations(trailhead, map.grid).toSet().count()
            }
    }

    object Part2 {
        fun calculateTrailheadScoreSum(map: TopographicMap): Int =
            map.trailheadLocations.asyncSumOfBlocking { trailhead ->
                findValidTrailEndLocations(trailhead, map.grid).count()
            }
    }

    private val TrailDirections = listOf(
        Direction.North,
        Direction.South,
        Direction.East,
        Direction.West,
    )

    /** @return List of end locations for valid trails (start from 0 and end at 9 iteratively). */
    fun findValidTrailEndLocations(trailhead: Location, grid: Grid2D<Int>) =
        TrailDirections.map { directionToMove ->
            traverseTrail(grid, trailhead, directionToMove, 0)
        }.flatten()

    fun traverseTrail(
        grid: Grid2D<Int>,
        startLocation: Location,
        direction: Direction,
        depth: Int,
    ): List<Location> {
        val startHeight = grid[startLocation]
        val currentLocation = startLocation.move(direction)
        if (currentLocation !in grid) {
            return emptyList()
        }

        val currentHeight = grid[currentLocation]
        return when {
            isEndOfTrail(startHeight, currentHeight) -> listOf(currentLocation)

            (currentHeight == startHeight + 1) -> {
                (TrailDirections - direction.opposite).map { directionToMove ->
                    traverseTrail(grid, currentLocation, directionToMove, depth + 1)
                }.flatten()
            }

            else -> emptyList()
        }
    }

    fun isEndOfTrail(previousHeight: Int, currentHeight: Int) =
        previousHeight == 8 && currentHeight == 9

    fun parseInput(input: List<String>): TopographicMap {
        val grid = input.toGrid2D { line ->
            line.toCharArray().map {
                when {
                    it.isDigit() -> it.digitToInt()
                    else -> -1
                }
            }
        }
        val trailheadLocations = buildList {
            grid.traverse { at, height ->
                if (height == 0) {
                    add(at)
                }
            }
        }
        return TopographicMap(
            grid = grid,
            trailheadLocations = trailheadLocations,
        )
    }

    data class TopographicMap(
        val grid: Grid2D<Int>,
        val trailheadLocations: List<Location>,
    )
}