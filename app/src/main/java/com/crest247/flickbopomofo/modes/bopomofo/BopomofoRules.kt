package com.crest247.flickbopomofo.modes.bopomofo

internal class BopomofoRules(charsCsv: String, allCharsCsv: String) {
    val syllables: Array<BopomofoSyllable> =
        readCsvRows(charsCsv).map { row -> BopomofoSyllable(row[1], row[2], row[3].toInt()) }
            .toList().toTypedArray()

    val charsBySyllable: Map<BopomofoSyllable, List<String>>

    private val tableSyllables: Set<BopomofoSyllable>
    private val initials: Set<Char>
    private val tonesByInitial: Map<Char, Set<Int>>
    private val tonesByFinal: Map<Pair<Char, String>, Set<Int>>
    private val remainsByInitial: Map<Char, List<String>>
    private val completeSyllables: Map<String, BopomofoSyllable>

    init {

        val charsMap = LinkedHashMap<BopomofoSyllable, MutableList<String>>()
        readCsvRows(allCharsCsv).forEach { row ->
            val syllable = BopomofoSyllable(row[1], row[2], row[3].toInt())
            val chars = charsMap.getOrPut(syllable) { ArrayList() }
            if (row[0] !in chars) chars.add(row[0])
        }
        charsBySyllable = charsMap
        tableSyllables = charsMap.keys

        val allSyllables = syllables.toHashSet() + tableSyllables
        initials = allSyllables.mapTo(HashSet()) { it.initial[0] }
        val tonesOfInitial = HashMap<Char, MutableSet<Int>>()
        val tonesOfFinal = HashMap<Pair<Char, String>, MutableSet<Int>>()
        for ((initial, remain, tone) in allSyllables) {
            tonesOfInitial.getOrPut(initial[0]) { HashSet() }.add(tone)
            tonesOfFinal.getOrPut(initial[0] to remain) { HashSet() }
                .add(tone)
        }
        tonesByInitial = tonesOfInitial
        tonesByFinal = tonesOfFinal
        val remainsOfInitial = HashMap<Char, MutableList<String>>()
        for ((initial, remain) in tonesOfFinal.keys) {
            if (remain.isNotEmpty()) remainsOfInitial.getOrPut(initial) { ArrayList() }.add(remain)
        }
        remainsByInitial = remainsOfInitial
        completeSyllables =
            tableSyllables.associateBy { it.initial + it.remain + TONE_MARK[it.tone] }
    }

    fun getCompleteSyllable(input: String): BopomofoSyllable? = completeSyllables[input]

    fun classify(input: String, continuation: Boolean = false): BopomofoInputKind = when {
        input.isEmpty() -> BopomofoInputKind.EMPTY
        !continuation && input in completeSyllables -> BopomofoInputKind.SINGLE_SYLLABLE
        getTokensAt(input, 0).isNotEmpty() -> BopomofoInputKind.MIXED
        else -> BopomofoInputKind.INVALID
    }

    private val tableSyllableCache = HashMap<Triple<Char, String?, Int>, List<BopomofoSyllable>>()

    fun getTableSyllables(token: BopomofoToken): List<BopomofoSyllable> =
        tableSyllableCache.getOrPut(Triple(token.initial, token.remain, token.tone)) {
            val matched = charsBySyllable.keys.filter {
                it.initial[0] == token.initial && (token.remain == null || it.remain == token.remain)
            }
            if (token.tone != 0) matched.filter { it.tone == token.tone } else matched
        }

    private val readingSyllableCache = HashMap<Triple<Char, String?, Int>, List<BopomofoSyllable>>()

    fun getReadingSyllables(token: BopomofoToken): List<BopomofoSyllable> =
        readingSyllableCache.getOrPut(Triple(token.initial, token.remain, token.tone)) {
            val remain = token.remain ?: ""
            charsBySyllable.keys.filter {
                it.initial[0] == token.initial && it.remain == remain && (token.tone == 0 || it.tone == token.tone)
            }
        }

    fun getTokensAt(input: String, position: Int): List<BopomofoToken> {
        val tokens = ArrayList<BopomofoToken>(4)
        val initial = input[position]
        if (initial !in initials) return tokens
        val forms = ArrayList<Pair<String?, Int>>(4)
        forms.add(null to position + 1)
        remainsByInitial[initial]?.forEach { remain ->
            if (input.startsWith(
                    remain,
                    position + 1
                )
            ) forms.add(remain to position + 1 + remain.length)
        }
        for ((remain, end) in forms) {
            val tones =
                if (remain == null) tonesByInitial[initial]!! else tonesByFinal[initial to remain]!!
            if (end < input.length && input[end] in MARK_TONE) {
                val tone = MARK_TONE.getValue(input[end])
                if (remain == null && initial in STANDALONE && BopomofoSyllable(
                        initial.toString(),
                        "",
                        tone
                    ) in tableSyllables
                ) {
                    tokens.add(BopomofoToken(end + 1, initial, "", tone))
                } else if (tone in tones) {
                    tokens.add(BopomofoToken(end + 1, initial, remain, tone))
                }
            } else {
                tokens.add(BopomofoToken(end, initial, remain, 0))
            }
        }
        return tokens
    }

    companion object {
        val TONE_MARK = arrayOf("", "ˉ", "ˊ", "ˇ", "ˋ", "˙")
        val MARK_TONE = mapOf('ˉ' to 1, 'ˊ' to 2, 'ˇ' to 3, 'ˋ' to 4, '˙' to 5)
        val STANDALONE: Set<Char> = "ㄓㄔㄕㄖㄗㄘㄙㄧㄨㄩㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦ".toSet()

        fun readCsvRows(text: String): Sequence<List<String>> =
            text.lineSequence().drop(1).map { it.trimEnd('\r') }.filter { it.isNotEmpty() }
                .map { it.split(',') }
    }
}

internal data class BopomofoSyllable(val initial: String, val remain: String, val tone: Int)

internal class BopomofoToken(
    val end: Int,
    val initial: Char,
    val remain: String?,
    val tone: Int
) {
    fun matches(syllable: BopomofoSyllable): Boolean {
        if (syllable.initial.length != 1 || syllable.initial[0] != initial) return false
        if (remain != null && syllable.remain != remain) return false
        return tone == 0 || syllable.tone == tone
    }
}
