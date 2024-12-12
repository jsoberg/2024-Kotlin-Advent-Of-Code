import Day12.Part1
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toCharGrid2D

// https://adventofcode.com/2024/day/12
fun main() {
    val input = readInput(day = 12)
    val grid = input.toCharGrid2D()
    println("Part 1: ${Part1.calculateTotalFencePrice(grid)}")
    //println("Part 2: ${Part2.calculateTotalFencePrice(grid)}")
}

private object Day12 {

    object Part1 {

        fun calculateTotalFencePrice(grid: Grid2D<Char>): Int {
            val finder = RegionFinder(grid)
            val regions = finder.findRegions()
            return regions.sumOf(::calculateRegionFencePrice)
        }

        private fun calculateRegionFencePrice(region: Set<Location>): Int {
            val area = region.size
            val perimeter = region.sumOf { location ->
                val neighbors = location.neighbors(RegionFinder.RegionDirections)
                4 - numNeighborsInRegion(neighbors, region)
            }
            return area * perimeter
        }

        private fun numNeighborsInRegion(neighbors: Set<Location>, region: Set<Location>): Int =
            (region.size - (region - neighbors).size)
    }

    object Part2 {
        fun calculateTotalFencePrice(grid: Grid2D<Char>): Int {
            val finder = RegionFinder(grid)
            val regions = finder.findRegions()
            return regions.sumOf(::calculateRegionFencePrice)
        }

        private fun calculateRegionFencePrice(region: Set<Location>): Int {
            val area = region.size
            val numSides = calculateNumSides(region)
            return area * numSides
        }

        private fun calculateNumSides(region: Set<Location>): Int {
            TODO()
        }
    }

    class RegionFinder(
        private val grid: Grid2D<Char>,
    ) {
        private val hitLocations: HashSet<Location> = hashSetOf()

        fun findRegions(): List<Set<Location>> = buildList {
            grid.traverse { at ->
                if (at !in hitLocations) {
                    add(traverseRegion(at, RegionDirections))
                }
            }
        }

        private fun traverseRegion(start: Location, directions: List<Direction>): Set<Location> =
            buildSet {
                add(start)
                hitLocations.add(start)
                directions.forEach { direction ->
                    addAll(traverseRegion(start, direction))
                }
            }

        private fun traverseRegion(
            start: Location,
            direction: Direction,
        ): Set<Location> {
            val regionElement = grid[start]
            val checkLocation = start.move(direction)

            return when {
                (checkLocation !in grid) -> emptySet()
                (checkLocation in hitLocations) -> emptySet()
                grid[checkLocation] != regionElement -> emptySet()
                else -> {
                    traverseRegion(checkLocation, (RegionDirections - direction.opposite))
                }
            }
        }

        companion object {
            val RegionDirections = listOf(
                Direction.North,
                Direction.South,
                Direction.East,
                Direction.West,
            )
        }
    }

}

/** Returns all neighboring Locations for this Location, in specified [directions].
 * Note that these neighboring Locations are **NOT** guaranteed to be in bounds for any given Grid. */
fun Location.neighbors(directions: Collection<Direction>): Set<Location> = buildSet {
    directions.forEach { direction ->
        add(move(direction))
    }
}