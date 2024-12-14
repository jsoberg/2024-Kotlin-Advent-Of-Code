import Day13.Part1
import Day13.Part2
import Day13.parseInput

// https://adventofcode.com/2024/day/13
fun main() {
    val input = readInput(day = 13)
    val clawGames = parseInput(input, aButtonTokenCost = 3, bButtonTokenCost = 1)
    println("Part 1: ${Part1.calculateFewestTokens(clawGames)}")
    println("Part 1: ${Part2.calculateFewestTokens(clawGames)}")
}

private object Day13 {

    object Part1 {
        fun calculateFewestTokens(clawGames: List<ClawGame>): Long =
            clawGames.sumOf(ClawGame::calculateFewestTokens)
    }

    object Part2 {
        fun calculateFewestTokens(clawGames: List<ClawGame>): Long =
            clawGames.map { clawGame ->
                val prize = clawGame.prize
                clawGame.copy(
                    prize = ClawGame.Point(
                        x = prize.x + 10000000000000,
                        y = prize.y + 10000000000000,
                    )
                )
            }.sumOf(ClawGame::calculateFewestTokens)
    }

    fun parseInput(
        input: List<String>,
        aButtonTokenCost: Int,
        bButtonTokenCost: Int,
    ) = buildList {
        for (i in input.indices step 4) {
            add(
                ClawGame(
                    prize = parsePrize(input[i + 2]),
                    aButton = parseButton(input[i], aButtonTokenCost),
                    bButton = parseButton(input[i + 1], bButtonTokenCost),
                )
            )
        }
    }

    private fun parseButton(line: String, tokenCost: Int): ClawGame.Button {
        val xy = line.split(": ")[1]
            .split(", ")
        return ClawGame.Button(
            x = xy[0].substring(2).toLong(),
            y = xy[1].substring(2).toLong(),
            tokenCost = tokenCost,
        )
    }

    private fun parsePrize(line: String): ClawGame.Point {
        val xy = line.split(": ")[1]
            .split(", ")
        return ClawGame.Point(
            x = xy[0].substring(2).toLong(),
            y = xy[1].substring(2).toLong(),
        )
    }

    data class ClawGame(
        val prize: Point,
        val aButton: Button,
        val bButton: Button,
    ) {
        // System of linear equations
        // ((Number of A presses) * (A button's X delta)) + ((Number of B presses) * (B button's X delta)) = (Prize X)
        // ((Number of A presses) * (A button's Y delta)) + ((Number of B presses) * (B button's Y delta)) = (Prize Y)
        //
        // Example:
        // Button A: X+94, Y+34
        // Button B: X+22, Y+67
        // Prize: X=8400, Y=5400
        //
        // Where A is (Number of A presses), and B is (Number of B presses):
        // (A * 94) + (B * 22) = 8400
        // (A * 34) + (B * 67) = 5400
        fun calculateFewestTokens(): Long {
            val a = aButton
            val b = bButton
            val bPresses = ((a.x * prize.y) - (a.y * prize.x)) / ((a.x * b.y) - (a.y * b.x))
            val aPresses = (prize.y - (b.y * bPresses)) / a.y
            val result = Point(
                x = (aPresses * a.x) + (bPresses * b.x),
                y = (aPresses * a.y) + (bPresses * b.y),
            )
            return if (result == prize) {
                (aPresses * a.tokenCost) + (bPresses * b.tokenCost)
            } else 0
        }

        data class Point(
            val x: Long,
            val y: Long,
        )

        data class Button(
            val x: Long,
            val y: Long,
            val tokenCost: Int,
        )
    }
}