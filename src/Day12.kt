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
    val input = readTestInput(day = 12)
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
            return regions.sumOf { region ->
                calculateRegionFencePrice(grid, region)
            }
        }

        private fun calculateRegionFencePrice(grid: Grid2D<Char>, region: Region): Int {
            val area = region.size
            val numSides = RegionSideCounter(grid, region).sideCount()
            return area * numSides
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

    class RegionSideCounter(
        private val grid: Grid2D<Char>,
        private val region: Region,
    ) {
        fun sideCount(): Int {
            /*            region.map { loc ->
                            val corners = collectCorners(loc)
                        }*/

            return region.map(::collectCorners)
                .flatten()
                .toSet()
                .count()
        }

        private fun collectCorners(location: Location): Set<Corner> = buildSet {
            if (hasUpperRightCorner(location)) {
                println("${grid[location]} $location has upper right")
                add(
                    Corner(
                        rowInbetween = location.row - 1 to location.row,
                        colInbetween = location.col to location.col + 1,
                    )
                )
            }

            if (hasUpperLeftCorner(location)) {
                println("${grid[location]} $location has upper left")
                add(
                    Corner(
                        rowInbetween = location.row - 1 to location.row,
                        colInbetween = location.col - 1 to location.col,
                    )
                )
            }

            if (hasLowerLeftCorner(location)) {
                println("${grid[location]} $location has lower left")
                add(
                    Corner(
                        rowInbetween = location.row to location.row + 1,
                        colInbetween = location.col - 1 to location.col,
                    )
                )
            }

            if (hasLowerRightCorner(location)) {
                println("${grid[location]} $location has lower right")
                add(
                    Corner(
                        rowInbetween = location.row to location.row + 1,
                        colInbetween = location.col to location.col + 1,
                    )
                )
            }

            if (size > 0) {
                println()
            }
        }

        private infix fun Location.canGo(dir: Direction) = move(dir) in region

        private infix fun Location.cannotGo(dir: Direction) = move(dir) !in region

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

        private data class Corner(
            val rowInbetween: Pair<Int, Int>,
            val colInbetween: Pair<Int, Int>,
        ) {
            override fun toString() = "{$rowInbetween, $colInbetween}"
        }
    }

    data class Region(
        private val plots: Set<Location>,
    ) : Set<Location> by plots {

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