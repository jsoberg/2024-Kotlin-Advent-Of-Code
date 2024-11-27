import com.soberg.kotlin.aoc.api.AdventOfCodeInputApi
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

private const val Year = 2024

/**
 * Pulls and caches input lines for specified [day].
 */
fun readInput(day: Int) = AdventOfCodeInputApi(
    cachingStrategy = AdventOfCodeInputApi.CachingStrategy.LocalTextFile("input")
).blockingReadInput(
    year = Year,
    day = day,
    sessionToken = readSessionToken(),
).getOrThrow()

private fun readSessionToken(): String {
    val secretTokenFile = Path("session-token.secret")
    require(secretTokenFile.exists()) {
        "session-token.secret file must exist and contain the sessionToken for Advent of Code"
    }
    return secretTokenFile.readText().trim()
}

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)
