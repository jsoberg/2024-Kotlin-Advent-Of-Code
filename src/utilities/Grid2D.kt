package utilities

data class Grid2D<T>(
    private val grid: List<List<T>>,
) {
    val rowSize: Int
        get() = grid.size

    val colSize: Int
        get() = grid[0].size

    infix operator fun get(location: Location): T = grid[location.row][location.col]

    fun isInBounds(location: Location): Boolean = isInBounds(location.row, location.col)

    fun isInBounds(row: Int, col: Int): Boolean =
        (row <= grid.lastIndex && row >= 0) &&
                (col <= grid[0].lastIndex && col >= 0)

    /** @return The element for the specified element starting at [from] and moving in [direction] for
     *  [distance], null if this element doesn't exist or location would be out of bounds of the grid.*/
    fun elementAt(
        from: Location,
        direction: Direction,
        distance: Int,
    ): T? {
        val endLocation = from.move(direction, distance)
        return if (isInBounds(endLocation)) {
            get(endLocation)
        } else null
    }

    /** @return A new copy of this [Grid2D] with the specified [element] replaced at [location]*/
    fun replace(location: Location, element: T): Grid2D<T> {
        val replacedRow: MutableList<T> = grid[location.row].toMutableList()
        replacedRow[location.col] = element
        return copy(
            grid = grid.toMutableList().apply {
                set(location.row, replacedRow)
            }
        )
    }

    data class Location(
        val row: Int,
        val col: Int,
    ) {
        fun move(direction: Direction, distance: Int = 1) = Location(
            row = row + (direction.row.integerMovement * distance),
            col = col + (direction.col.integerMovement * distance),
        )
    }

    enum class Direction(
        internal val row: Movement = Movement.None,
        internal val col: Movement = Movement.None,
    ) {
        North(row = Movement.Negative),
        NorthEast(row = Movement.Negative, col = Movement.Positive),
        East(col = Movement.Positive),
        SouthEast(row = Movement.Positive, col = Movement.Positive),
        South(row = Movement.Positive),
        SouthWest(row = Movement.Positive, col = Movement.Negative),
        West(col = Movement.Negative),
        NorthWest(row = Movement.Negative, col = Movement.Negative);

        internal enum class Movement(
            val integerMovement: Int,
        ) {
            Positive(1),
            Negative(-1),
            None(0),
        }
    }
}

fun <T> List<List<T>>.toGrid2D() = Grid2D(this)