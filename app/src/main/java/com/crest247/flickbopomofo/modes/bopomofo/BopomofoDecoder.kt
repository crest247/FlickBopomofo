package com.crest247.flickbopomofo.modes.bopomofo

import android.content.Context
import com.crest247.flickbopomofo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

enum class BopomofoInputKind {
    EMPTY,
    SINGLE_SYLLABLE,
    MIXED,
    INVALID,
}

data class BopomofoCandidate(
    val text: String,
    val unitId: Int,
    val consumed: Int,
    val score: Double = Double.NaN
)

data class BopomofoSentence(val text: String, val units: List<BopomofoCandidate>, val score: Double)

data class BopomofoResult(
    val input: String,
    val kind: BopomofoInputKind,
    val sentences: List<BopomofoSentence>,
    val candidates: List<BopomofoCandidate>,
) {
    companion object {
        val EMPTY = BopomofoResult("", BopomofoInputKind.EMPTY, emptyList(), emptyList())
    }
}

object BopomofoDecoder {
    private class LoadedData(
        val rules: BopomofoRules,
        val lexicon: BopomofoLexicon,
        val model: BopomofoModel
    ) {
        val beamSearch = BopomofoBeamSearch(lexicon, model)

        val contextStateCache =
            object : LinkedHashMap<List<Int>, BopomofoModel.State>(16, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<List<Int>, BopomofoModel.State>?) =
                    size > 32
            }
    }

    @Volatile
    private var loaded: LoadedData? = null

    var isLoaded = false
        private set

    var beamWidth = 8
    var maxSentences = 3
    var maxCandidates = 60

    suspend fun loadData(context: Context) {
        if (isLoaded) return
        withContext(Dispatchers.IO) {
            val resources = context.resources
            loadFrom { name ->
                resources.openRawResource(
                    when (name) {
                        "chars" -> R.raw.bopomofo_chars
                        "all_chars" -> R.raw.bopomofo_all_chars
                        "units" -> R.raw.bopomofo_units
                        else -> R.raw.bopomofo_model
                    }
                )
            }
        }
    }

    internal fun loadFrom(open: (String) -> InputStream) {
        fun readBytes(name: String) = open(name).use { it.readBytes() }
        val rules = BopomofoRules(
            String(readBytes("chars"), Charsets.UTF_8),
            String(readBytes("all_chars"), Charsets.UTF_8)
        )
        val lexicon = BopomofoLexicon(readBytes("units"), rules)
        val model = BopomofoModel(readBytes("model"))
        loaded = LoadedData(rules, lexicon, model)
        isLoaded = true
    }

    @Synchronized
    fun decode(
        input: String,
        selectedUnitIds: IntArray = IntArray(0),
        isCancelled: () -> Boolean = { false }
    ): BopomofoResult? {
        val data = loaded ?: return BopomofoResult.EMPTY
        return when (val kind = data.rules.classify(input, selectedUnitIds.isNotEmpty())) {
            BopomofoInputKind.SINGLE_SYLLABLE -> {
                val syllable = data.rules.getCompleteSyllable(input)!!
                BopomofoResult(
                    input,
                    kind,
                    emptyList(),
                    getTableChars(data, syllable, input.length)
                )
            }

            BopomofoInputKind.MIXED -> decodeMixed(data, input, selectedUnitIds, isCancelled)
            else -> BopomofoResult(input, kind, emptyList(), emptyList())
        }
    }

    private fun decodeMixed(
        data: LoadedData, input: String, selectedUnitIds: IntArray, isCancelled: () -> Boolean
    ): BopomofoResult? {
        val graph = data.lexicon.buildGraph(input)
        if (graph.target == 0) return BopomofoResult(
            input,
            BopomofoInputKind.INVALID,
            emptyList(),
            emptyList()
        )
        val searchResult = data.beamSearch.decode(
            graph,
            input.length,
            getContextState(data, selectedUnitIds),
            beamWidth,
            maxSentences,
            isCancelled
        ) ?: return null
        val sentences = searchResult.sentences.map { (score, path) ->
            val units = path.toList().map { step ->
                if (step.unitId >= 0) BopomofoCandidate(
                    data.lexicon.getText(step.unitId),
                    step.unitId,
                    step.end
                )
                else data.lexicon.getTableChars(graph.tableFillTokens[-step.unitId - 1])[0]
                    .let { (char, charUnitId) -> BopomofoCandidate(char, charUnitId, step.end) }
            }
            BopomofoSentence(units.joinToString("") { it.text }, units, score)
        }
        val candidates = searchResult.firstCandidates.take(maxCandidates)
            .map { BopomofoCandidate(it.text, it.unitId, it.end, it.score) }
        return BopomofoResult(
            input,
            BopomofoInputKind.MIXED,
            sentences,
            candidates + getHomophones(data, input, candidates)
        )
    }

    private fun getHomophones(
        data: LoadedData,
        input: String,
        candidates: List<BopomofoCandidate>
    ): List<BopomofoCandidate> {
        val shown = candidates.mapTo(HashSet()) { it.text to it.consumed }
        val token = data.rules.getTokensAt(input, 0)
            .filter { data.rules.getReadingSyllables(it).isNotEmpty() }
            .maxByOrNull { it.end } ?: return emptyList()
        return data.lexicon.getReadingChars(token)
            .map { (char, charUnitId) -> BopomofoCandidate(char, charUnitId, token.end) }
            .filter { shown.add(it.text to it.consumed) }
    }

    private fun getTableChars(
        data: LoadedData,
        syllable: BopomofoSyllable,
        consumed: Int
    ): List<BopomofoCandidate> =
        data.rules.charsBySyllable[syllable].orEmpty().distinct()
            .map { char ->
                BopomofoCandidate(
                    char,
                    data.lexicon.getCharUnitId(char, syllable),
                    consumed
                )
            }

    private fun getContextState(data: LoadedData, unitIds: IntArray): BopomofoModel.State {
        val validUnitIds = unitIds.filter { it >= 0 }
        data.contextStateCache[validUnitIds]?.let { return it }
        val state = if (validUnitIds.isEmpty()) data.model.getInitialState()
        else data.model.advance(
            getContextState(data, validUnitIds.dropLast(1).toIntArray()),
            data.lexicon.unitToVocab[validUnitIds.last()]
        )
        data.contextStateCache[validUnitIds] = state
        return state
    }
}
