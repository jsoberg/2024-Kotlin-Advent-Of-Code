import Day08.Part1
import Day08.Part2
import Day08.parseNodeGrid
import com.soberg.aoc.utlities.datastructures.Grid2D
import com.soberg.aoc.utlities.datastructures.Grid2D.Location
import com.soberg.aoc.utlities.datastructures.toGrid2D

// https://adventofcode.com/2024/day/8
fun main() {
    val input = readInput(day = 8)
    val nodeGrid = parseNodeGrid(input)
    println("Part 1: ${Part1.calculateUniqueAntiNodeLocationCount(nodeGrid)}")
    println("Part 2: ${Part2.calculateUniqueAntiNodeLocationCountWithResonance(nodeGrid)}")
}

private object Day08 {

    object Part1 {

        fun calculateUniqueAntiNodeLocationCount(nodeGrid: NodeGrid): Int = buildSet {
            val antennaToLocMap = nodeGrid.antennaToLocationsMap
            for ((_, locations) in antennaToLocMap.entries) {
                for (i in locations.indices) {
                    for (j in locations.indices) {
                        if (i == j) continue
                        val first = locations[i]
                        val second = locations[j]
                        val distance = second distanceTo first
                        val antiNode = second + distance
                        if (antiNode in nodeGrid.grid) {
                            add(antiNode)
                        }
                    }
                }
            }
            //nodeGrid.printWithAntiNodes(this)
        }.size
    }

    object Part2 {
        fun calculateUniqueAntiNodeLocationCountWithResonance(nodeGrid: NodeGrid): Int = buildSet {
            val antennaToLocMap = nodeGrid.antennaToLocationsMap
            for ((_, locations) in antennaToLocMap.entries) {
                for (i in locations.indices) {
                    for (j in locations.indices) {
                        if (i == j) continue
                        val first = locations[i]
                        val second = locations[j]
                        val distance = second distanceTo first

                        addAll(
                            // Make sure we include second itself.
                            generateSequence(second) { nextLocation ->
                                nextLocation + distance
                            }.takeWhile { nextLocation -> nextLocation in nodeGrid.grid }
                        )
                    }
                }
            }
            //nodeGrid.printWithAntiNodes(this)
        }.size
    }

    fun parseNodeGrid(input: List<String>): NodeGrid {
        val grid = input.toGrid2D { line -> line.toCharArray().asList() }
        return NodeGrid(
            grid = grid,
            antennaToLocationsMap = grid.filterElementToLocationsMap { element, _ ->
                element != '.'
            },
        )
    }

    data class NodeGrid(
        val grid: Grid2D<Char>,
        val antennaToLocationsMap: Map<Char, List<Location>>
    ) {
        fun printWithAntiNodes(antiNodeLocations: Set<Location>) {
            grid.print { location ->
                when {
                    (location in antiNodeLocations) -> "#"
                    else -> grid[location].toString()
                }
            }
        }
    }

}