import Day12.Part1
import Day12.Part2
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.East
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.North
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.NorthEast
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.NorthWest
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.South
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.SouthEast
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.SouthWest
import com.soberg.aoc.utlities.datastructures.Grid2D.Direction.West
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toCharGrid2D

// https://adventofcode.com/2024/day/12
fun main() {
    val input = readInput(day = 12)
    val grid = input.toCharGrid2D()
    println("Part 1: ${Part1.calculateTotalFencePrice(grid)}")
    println("Part 2: ${Part2.calculateTotalFencePrice(grid)}")
}

private object Day12 {

    object Part1 {

        fun calculateTotalFencePrice(grid: Grid2D<Char>): Int {
            val finder = RegionFinder(grid)
            val regions = finder.findRegions()
            return regions.sumOf(::calculateRegionFencePrice)
        }

        private fun calculateRegionFencePrice(region: Region): Int {
            val area = region.size
            val perimeter = region.sumOf { location ->
                val neighbors = location.neighbors(Region.Directions)
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

        private fun calculateRegionFencePrice(region: Region): Int {
            val area = region.size
            // # corners == # of sides
            return area * region.numCorners
        }
    }

    class RegionFinder(
        private val grid: Grid2D<Char>,
    ) {
        private val hitLocations: HashSet<Location> = hashSetOf()

        fun findRegions(): List<Region> = buildList {
            grid.traverse { at ->
                if (at !in hitLocations) {
                    add(Region(traverseRegion(at, Region.Directions)))
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
                    traverseRegion(checkLocation, (Region.Directions - direction.opposite))
                }
            }
        }
    }

    data class Region(
        private val plots: Set<Location>,
    ) : Set<Location> by plots {

        val numCorners: Int by lazy {
            plots.sumOf(::numCorners)
        }

        private fun numCorners(location: Location): Int {
            var corners = 0
            if (hasUpperRightCorner(location)) {
                corners++
            }
            if (hasUpperLeftCorner(location)) {
                corners++
            }
            if (hasLowerLeftCorner(location)) {
                corners++
            }
            if (hasLowerRightCorner(location)) {
                corners++
            }
            return corners
        }

        private infix fun Location.canGo(dir: Direction) = move(dir) in this@Region

        private infix fun Location.cannotGo(dir: Direction) = move(dir) !in this@Region

        private fun hasUpperRightCorner(loc: Location) =
            (loc cannotGo North && loc cannotGo East) ||
                    (loc canGo North && loc canGo East && loc cannotGo NorthEast)

        private fun hasUpperLeftCorner(loc: Location) =
            (loc cannotGo North && loc cannotGo West) ||
                    (loc canGo North && loc canGo West && loc cannotGo NorthWest)

        private fun hasLowerRightCorner(loc: Location) =
            (loc cannotGo South && loc cannotGo East) ||
                    (loc canGo South && loc canGo East && loc cannotGo SouthEast)

        private fun hasLowerLeftCorner(loc: Location) =
            (loc cannotGo South && loc cannotGo West) ||
                    (loc canGo South && loc canGo West && loc cannotGo SouthWest)

        companion object {
            val Directions = listOf(
                North,
                South,
                East,
                West,
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