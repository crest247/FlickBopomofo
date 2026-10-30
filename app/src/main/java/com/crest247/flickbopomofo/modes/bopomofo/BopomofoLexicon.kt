package com.crest247.flickbopomofo.modes.bopomofo

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BopomofoLexicon(bytes: ByteArray, private val rules: BopomofoRules) {
    val unitCount: Int
    val vocabSize: Int
    private val textOffsets: IntArray
    private val textBytes: ByteArray
    private val syllableOffsets: IntArray
    private val unitSyllableIds: ShortArray
    val unitToVocab: IntArray
    val unkLogProbs: FloatArray
    val unigramLogProbs: FloatArray
    private val childOffsets: IntArray
    private val childSyllableIds: ShortArray
    private val childNodes: IntArray
    private val nodeUnitOffsets: IntArray
    private val nodeUnits: IntArray
    private val texts: Array<String?>

    init {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        unitCount = buffer.int
        vocabSize = buffer.int
        textOffsets = buffer.readInts(unitCount + 1)
        textBytes = ByteArray(buffer.int).also { buffer.get(it) }
        syllableOffsets = buffer.readInts(unitCount + 1)
        unitSyllableIds = buffer.readShorts(syllableOffsets[unitCount])
        unitToVocab = buffer.readInts(unitCount)
        unkLogProbs = buffer.readFloats(unitCount)
        unigramLogProbs = buffer.readFloats(unitCount)
        val nodeCount = buffer.int
        childOffsets = buffer.readInts(nodeCount + 1)
        childSyllableIds = buffer.readShorts(childOffsets[nodeCount])
        childNodes = buffer.readInts(childOffsets[nodeCount])
        nodeUnitOffsets = buffer.readInts(nodeCount + 1)
        nodeUnits = buffer.readInts(nodeUnitOffsets[nodeCount])
        texts = arrayOfNulls(unitCount)
    }

    fun getText(unitId: Int): String = texts[unitId]
        ?: String(
            textBytes,
            textOffsets[unitId],
            textOffsets[unitId + 1] - textOffsets[unitId],
            Charsets.UTF_8
        )
            .also { texts[unitId] = it }

    fun getSyllableCount(unitId: Int): Int = syllableOffsets[unitId + 1] - syllableOffsets[unitId]

    fun getSyllableIds(unitId: Int): IntArray =
        IntArray(getSyllableCount(unitId)) { unitSyllableIds[syllableOffsets[unitId] + it].toInt() and 0xFFFF }

    class Graph(
        val edges: Array<IntArray>,
        val tableFillTokens: List<BopomofoToken>,
        val target: Int
    )

    private val syllableIds: Map<BopomofoSyllable, Int> =
        rules.syllables.withIndex().associate { (id, syllable) -> syllable to id }

    private val singleCharUnits: Map<String, Int> = HashMap<String, Int>().also { units ->
        for (unitId in 0 until unitCount) {
            if (getSyllableCount(unitId) == 1) units.putIfAbsent(
                getText(unitId) + "\u0000" + getSyllableIds(
                    unitId
                )[0], unitId
            )
        }
    }

    fun getCharUnitId(char: String, syllable: BopomofoSyllable): Int {
        val syllableId = syllableIds[syllable] ?: return -1
        return singleCharUnits[char + "\u0000" + syllableId] ?: -1
    }

    private val tableCharCache = HashMap<Triple<Char, String?, Int>, List<Pair<String, Int>>>()

    fun getTableChars(token: BopomofoToken): List<Pair<String, Int>> =
        tableCharCache.getOrPut(
            Triple(
                token.initial,
                token.remain,
                token.tone
            )
        ) { getChars(rules.getTableSyllables(token)) }

    private val readingCharCache = HashMap<Triple<Char, String?, Int>, List<Pair<String, Int>>>()

    fun getReadingChars(token: BopomofoToken): List<Pair<String, Int>> =
        readingCharCache.getOrPut(
            Triple(
                token.initial,
                token.remain,
                token.tone
            )
        ) { getChars(rules.getReadingSyllables(token)) }

    private fun getChars(syllables: List<BopomofoSyllable>): List<Pair<String, Int>> {
        val chars = LinkedHashMap<String, Int>()
        for (syllable in syllables) {
            for (char in rules.charsBySyllable.getValue(syllable)) {
                if (char !in chars) chars[char] = getCharUnitId(char, syllable)
            }
        }
        return chars.map { it.key to it.value }
    }

    fun buildGraph(input: String): Graph {
        val length = input.length
        val tokensByPosition = Array(length) { rules.getTokensAt(input, it) }
        val eaten = BooleanArray(length)
        for (position in 0 until length) {
            if (eaten[position]) {
                tokensByPosition[position] = emptyList()
                continue
            }
            for (token in tokensByPosition[position]) {
                for (covered in position + 1 until minOf(token.end, length)) eaten[covered] = true
            }
        }
        val edges = Array(length) { IntArray(0) }
        val tableFillTokens = ArrayList<BopomofoToken>()
        val reachable = BooleanArray(length + 1)
        if (length > 0) reachable[0] = true
        val edgeBuffer = IntList()
        val seenEdges = HashSet<Long>()
        val syllables = rules.syllables

        fun collectUnits(position: Int, node: Int, depth: Int) {
            if (depth > 0) {
                for (index in nodeUnitOffsets[node] until nodeUnitOffsets[node + 1]) {
                    val unitId = nodeUnits[index]
                    if (seenEdges.add(unitId.toLong() shl 20 or position.toLong())) {
                        edgeBuffer.add(unitId); edgeBuffer.add(position)
                    }
                }
            }
            if (position == length) return
            for (token in tokensByPosition[position]) {
                for (child in childOffsets[node] until childOffsets[node + 1]) {
                    if (token.matches(syllables[childSyllableIds[child].toInt() and 0xFFFF])) {
                        collectUnits(token.end, childNodes[child], depth + 1)
                    }
                }
            }
        }

        for (start in 0 until length) {
            if (!reachable[start]) continue
            edgeBuffer.clear(); seenEdges.clear()
            collectUnits(start, 0, 0)
            for (token in tokensByPosition[start]) {
                if (rules.getTableSyllables(token).isNotEmpty()) {
                    tableFillTokens.add(token)
                    edgeBuffer.add(-tableFillTokens.size); edgeBuffer.add(token.end)
                }
            }
            edges[start] = edgeBuffer.toArray()
            for (index in 1 until edges[start].size step 2) reachable[edges[start][index]] = true
        }

        val target =
            if (reachable[length]) length else (length downTo 1).firstOrNull { reachable[it] } ?: 0
        val unreachable = Int.MAX_VALUE
        val tableFillCost = IntArray(length + 1) { unreachable }
        tableFillCost[target] = 0
        for (start in target - 1 downTo 0) {
            val startEdges = edges[start]
            for (index in startEdges.indices step 2) {
                val end = startEdges[index + 1]
                if (end <= target && tableFillCost[end] != unreachable) {
                    val cost = (if (startEdges[index] < 0) 1 else 0) + tableFillCost[end]
                    tableFillCost[start] = minOf(tableFillCost[start], cost)
                }
            }
        }
        for (start in 0 until length) {
            val startEdges = edges[start]
            val keptEdges = IntList()
            if (start < target && tableFillCost[start] != unreachable) {
                for (index in startEdges.indices step 2) {
                    val end = startEdges[index + 1]
                    if (end <= target && tableFillCost[end] != unreachable &&
                        (if (startEdges[index] < 0) 1 else 0) + tableFillCost[end] == tableFillCost[start]
                    ) {
                        keptEdges.add(startEdges[index]); keptEdges.add(end)
                    }
                }
            }
            edges[start] = keptEdges.toArray()
        }
        return Graph(edges, tableFillTokens, target)
    }
}

internal class IntList(capacity: Int = 64) {
    private var values = IntArray(capacity)
    var size = 0
        private set

    fun add(value: Int) {
        if (size == values.size) values = values.copyOf(size * 2)
        values[size++] = value
    }

    fun clear() {
        size = 0
    }

    fun toArray(): IntArray = values.copyOf(size)
}

internal fun ByteBuffer.readInts(count: Int) =
    IntArray(count).also { asIntBuffer().get(it); position(position() + 4 * count) }

internal fun ByteBuffer.readShorts(count: Int) =
    ShortArray(count).also { asShortBuffer().get(it); position(position() + 2 * count) }

internal fun ByteBuffer.readFloats(count: Int) =
    FloatArray(count).also { asFloatBuffer().get(it); position(position() + 4 * count) }
