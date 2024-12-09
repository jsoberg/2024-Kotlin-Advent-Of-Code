import Day09.Part1
import Day09.Part2

// https://adventofcode.com/2024/day/9
fun main() {
    val input = readInput(day = 9).first()
    println("Part 1: ${Part1.calculateCompactedDiskMapChecksum(input)}")
    println("Part 2: ${Part2.calculateDefragmentedDiskMapChecksum(input)}")
}

private object Day09 {

    object Part1 {

        fun calculateCompactedDiskMapChecksum(input: String): Long {
            val blocks = parseToBlocks(input)
            //blocks.print()
            val compactedBlocks = compactBlocks(blocks)
            //compactedBlocks.print()
            return calculateChecksum(compactedBlocks)
        }

        private fun compactBlocks(blocks: List<MemoryBlock>): List<MemoryBlock> {
            val movedBlocks = blocks.toMutableList()
            var blockToMoveIndex = nextFileBlockIndex(movedBlocks, blocks.size)
                ?: error("No file blocks at start")
            var freeSpaceIndex = nextFreeSpaceIndex(movedBlocks, -1)
                ?: error("No free space at start")
            while (freeSpaceIndex < blockToMoveIndex) {
                movedBlocks[freeSpaceIndex] = movedBlocks[blockToMoveIndex]
                movedBlocks[blockToMoveIndex] = MemoryBlock.Free
                // If no free space or block to move can be found, break out of this loop and calculate checksum.
                freeSpaceIndex = nextFreeSpaceIndex(movedBlocks, freeSpaceIndex) ?: break
                blockToMoveIndex = nextFileBlockIndex(movedBlocks, blockToMoveIndex) ?: break
            }
            return movedBlocks
        }

        private fun nextFreeSpaceIndex(blocks: List<MemoryBlock>, startIndex: Int): Int? {
            for (i in (startIndex + 1)..blocks.lastIndex) {
                if (blocks[i] == MemoryBlock.Free) return i
            }
            return null
        }

        private fun nextFileBlockIndex(blocks: List<MemoryBlock>, startIndex: Int): Int? {
            for (i in (startIndex - 1) downTo 0) {
                if (blocks[i] is MemoryBlock.File) return i
            }
            return null
        }

        private fun parseToBlocks(input: String): List<MemoryBlock> = buildList {
            for (i in input.indices step 2) {
                val blockSize = input[i].digitToInt()
                val freeBlocks = if (i < input.length - 1) {
                    input[i + 1].digitToInt()
                } else 0
                val id = i / 2
                for (block in 0..<blockSize) {
                    add(MemoryBlock.File(id))
                }
                for (block in 0..<freeBlocks) {
                    add(MemoryBlock.Free)
                }
            }
        }
    }

    object Part2 {
        
        fun calculateDefragmentedDiskMapChecksum(input: String): Long {
            val chunkedBlocks = parseToChunkedBlocks(input)
            val defragmentedBlocks = defragmentBlocks(chunkedBlocks)
            return calculateChecksum(defragmentedBlocks)
        }

        private fun defragmentBlocks(chunkedBlocks: List<ChunkedMemoryBlock>): List<ChunkedMemoryBlock> {
            val movedBlocks = chunkedBlocks.toMutableList()
            val files = chunkedBlocks.filterIsInstance<ChunkedMemoryBlock.File>().reversed()

            for (file in files) {
                val freeChunkIndex = movedBlocks
                    .indexOfFirst { it is ChunkedMemoryBlock.Free && it.blockSize >= file.blockSize }
                val fileIndex = movedBlocks.indexOfLast { it == file }
                if (freeChunkIndex != -1 && freeChunkIndex < fileIndex) {
                    val freeChunk = movedBlocks[freeChunkIndex]
                    val newFreeSpace = freeChunk.blockSize - file.blockSize
                    movedBlocks[freeChunkIndex] = file
                    movedBlocks[fileIndex] = ChunkedMemoryBlock.Free(file.blockSize)
                    if (newFreeSpace > 0) {
                        movedBlocks.add(freeChunkIndex + 1, ChunkedMemoryBlock.Free(newFreeSpace))
                    }
                }
            }

            return movedBlocks
        }

        private fun calculateChecksum(chunkedBlocks: List<ChunkedMemoryBlock>): Long {
            val memoryBlocks = toMemoryBlocks(chunkedBlocks)
            //memoryBlocks.print()
            return calculateChecksum(memoryBlocks)
        }

        private fun toMemoryBlocks(chunkedBlocks: List<ChunkedMemoryBlock>) = buildList {
            for (chunk in chunkedBlocks) {
                when (chunk) {
                    is ChunkedMemoryBlock.File -> {
                        (0..<chunk.blockSize).forEach { _ ->
                            add(MemoryBlock.File(chunk.id))
                        }
                    }

                    is ChunkedMemoryBlock.Free -> {
                        (0..<chunk.blockSize).forEach { _ ->
                            add(MemoryBlock.Free)
                        }
                    }
                }
            }
        }

        private fun parseToChunkedBlocks(input: String): List<ChunkedMemoryBlock> = buildList {
            for (i in input.indices step 2) {
                val blockSize = input[i].digitToInt()
                val freeBlocks = if (i < input.length - 1) {
                    input[i + 1].digitToInt()
                } else 0
                val id = i / 2
                add(ChunkedMemoryBlock.File(id, blockSize))
                if (freeBlocks > 0) {
                    add(ChunkedMemoryBlock.Free(freeBlocks))
                }
            }
        }
    }

    fun calculateChecksum(blocks: List<MemoryBlock>): Long {
        var checksum = 0L
        for (i in blocks.indices) {
            when (val block = blocks[i]) {
                MemoryBlock.Free -> continue
                is MemoryBlock.File -> {
                    checksum += block.id * i
                }
            }
        }
        return checksum
    }

    fun printableString(blocks: List<MemoryBlock>): String = buildString {
        blocks.forEach { block ->
            when (block) {
                MemoryBlock.Free -> append('.')
                is MemoryBlock.File -> append(block.id)
            }
        }
    }

    fun List<MemoryBlock>.print() {
        print(printableString(this))
        println()
    }

    sealed interface MemoryBlock {
        @JvmInline
        value class File(val id: Int) : MemoryBlock
        data object Free : MemoryBlock
    }

    sealed interface ChunkedMemoryBlock {
        val blockSize: Int

        data class File(val id: Int, override val blockSize: Int) : ChunkedMemoryBlock
        data class Free(override val blockSize: Int) : ChunkedMemoryBlock
    }
}
