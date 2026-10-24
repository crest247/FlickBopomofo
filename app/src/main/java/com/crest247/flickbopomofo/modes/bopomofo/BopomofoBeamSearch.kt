package com.crest247.flickbopomofo.modes.bopomofo

internal class BopomofoBeamSearch(
    private val lexicon: BopomofoLexicon,
    private val model: BopomofoModel
) {
    class Path(val unitId: Int, val end: Int, val previous: Path?) {
        val first: Path get() = previous?.first ?: this
        fun toList(): List<Path> = generateSequence(this) { it.previous }.toList().asReversed()
    }

    private class Hypothesis(
        val score: Double,
        val parentState: BopomofoModel.State,
        val unitId: Int,
        val path: Path?
    ) {
        var state: BopomofoModel.State? = null
    }

    class FirstCandidate(val score: Double, val text: String, val unitId: Int, val end: Int)

    class Result(
        val sentences: List<Pair<Double, Path>>,
        val firstCandidates: List<FirstCandidate>,
    )

    private class StateCacheKey(val parentState: BopomofoModel.State, val vocabIndex: Int) {
        override fun equals(other: Any?) =
            other is StateCacheKey && other.parentState === parentState && other.vocabIndex == vocabIndex

        override fun hashCode() = System.identityHashCode(parentState) * 31 + vocabIndex
    }

    private val stateCache =
        object : LinkedHashMap<StateCacheKey, BopomofoModel.State>(1024, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<StateCacheKey, BopomofoModel.State>?) =
                size > 4096
        }

    private val unkIndex = model.outputSize - 1

    private fun getVocabIndex(unitId: Int) =
        if (unitId >= 0) lexicon.unitToVocab[unitId] else unkIndex

    private fun getLogProb(state: BopomofoModel.State, unitId: Int): Float =
        if (unitId >= 0) model.getLogit(
            state,
            lexicon.unitToVocab[unitId]
        ) + lexicon.unkLogProbs[unitId]
        else model.getLogit(state, unkIndex) + TABLE_FILL_PENALTY

    private fun getUnigramLogProb(unitId: Int): Double =
        if (unitId >= 0) lexicon.unigramLogProbs[unitId].toDouble() else TABLE_FILL_UNIGRAM_LOG_PROB

    private fun getState(hypothesis: Hypothesis): BopomofoModel.State =
        hypothesis.state ?: (
                if (hypothesis.unitId == ROOT) hypothesis.parentState
                else model.advance(hypothesis.parentState, getVocabIndex(hypothesis.unitId))
                ).also { hypothesis.state = it }

    fun decode(
        graph: BopomofoLexicon.Graph,
        length: Int,
        contextState: BopomofoModel.State,
        beamWidth: Int = 8,
        maxSentences: Int = 5,
        isCancelled: () -> Boolean = { false }
    ): Result? {
        val edges = graph.edges
        val target = graph.target
        val futureScores = DoubleArray(target + 1) { Double.NEGATIVE_INFINITY }
        futureScores[target] = 0.0
        for (start in target - 1 downTo 0) {
            val startEdges = edges[start]
            for (index in startEdges.indices step 2) {
                val score =
                    getUnigramLogProb(startEdges[index]) + futureScores[startEdges[index + 1]]
                if (score > futureScores[start]) futureScores[start] = score
            }
        }

        val hypotheses = Array(target + 1) { ArrayList<Hypothesis>() }
        hypotheses[0].add(Hypothesis(0.0, contextState, ROOT, null))
        var firstLogProbs: FloatArray? = null
        for (start in 0 until target) {
            if (isCancelled()) return null
            val startEdges = edges[start]
            if (hypotheses[start].isEmpty() || startEdges.isEmpty()) continue
            val kept = hypotheses[start].sortedByDescending { it.score }.take(beamWidth)

            val pending = ArrayList<Hypothesis>()
            for (hypothesis in kept) {
                if (hypothesis.state != null || hypothesis.unitId == ROOT) continue
                val cached = stateCache[StateCacheKey(
                    hypothesis.parentState,
                    getVocabIndex(hypothesis.unitId)
                )]
                if (cached != null) hypothesis.state = cached else pending.add(hypothesis)
            }
            if (pending.isNotEmpty()) {
                val vocabIndices = IntArray(pending.size) { getVocabIndex(pending[it].unitId) }
                val states = model.stepBatch(pending.map { it.parentState.hidden }, vocabIndices)
                for (index in pending.indices) {
                    pending[index].state = states[index]
                    stateCache[StateCacheKey(pending[index].parentState, vocabIndices[index])] =
                        states[index]
                }
            }
            for (hypothesis in kept) {
                val state = getState(hypothesis)
                val logProbs =
                    FloatArray(startEdges.size / 2) { getLogProb(state, startEdges[2 * it]) }
                if (start == 0) firstLogProbs = logProbs
                for (index in logProbs.indices) {
                    val unitId = startEdges[2 * index]
                    val end = startEdges[2 * index + 1]
                    hypotheses[end].add(
                        Hypothesis(
                            hypothesis.score + logProbs[index],
                            state,
                            unitId,
                            Path(unitId, end, hypothesis.path)
                        )
                    )
                }
            }
            hypotheses[start].clear()
            hypotheses[start].addAll(kept)
        }

        val finals =
            hypotheses[target].sortedByDescending { it.score }.take(maxOf(beamWidth, maxSentences))
        val sentences = if (target == length) finals.take(maxSentences)
            .map { it.score to it.path!! } else emptyList()

        val exactScores = HashMap<Long, Double>()
        for (hypothesis in finals) {
            val firstStep = hypothesis.path!!.first
            val pathKey = getPathKey(firstStep.unitId, firstStep.end)
            if ((exactScores[pathKey]
                    ?: Double.NEGATIVE_INFINITY) < hypothesis.score
            ) exactScores[pathKey] = hypothesis.score
        }
        val bestByText = LinkedHashMap<Pair<String, Int>, FirstCandidate>()
        fun addCandidate(score: Double, text: String, unitId: Int, end: Int) {
            val textKey = text to end
            val existing = bestByText[textKey]
            if (existing == null || existing.score < score) bestByText[textKey] =
                FirstCandidate(score, text, unitId, end)
        }

        val startEdges = edges[0]
        for (index in 0 until startEdges.size / 2) {
            val unitId = startEdges[2 * index]
            val end = startEdges[2 * index + 1]
            val score = exactScores[getPathKey(unitId, end)]
                ?: (firstLogProbs!![index].toDouble() + futureScores[end])
            if (unitId >= 0) addCandidate(score, lexicon.getText(unitId), unitId, end)
            else lexicon.getTableChars(graph.tableFillTokens[-unitId - 1])
                .forEachIndexed { order, (char, charUnitId) ->
                    addCandidate(score - 1e-6 * order, char, charUnitId, end)
                }
        }

        val firstCandidates = bestByText.values.sortedWith(
            compareByDescending<FirstCandidate> { it.score }.thenByDescending { getUnigramLogProb(it.unitId) }
                .thenBy { it.unitId }
        )
        return Result(sentences, firstCandidates)
    }

    private fun getPathKey(unitId: Int, end: Int) = unitId.toLong() shl 20 or end.toLong()

    companion object {
        const val TABLE_FILL_PENALTY = -12.0f
        const val TABLE_FILL_UNIGRAM_LOG_PROB = -20.0
        private const val ROOT = Int.MIN_VALUE
    }
}
