package com.crest247.flickbopomofo.modes.bopomofo

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.tanh

internal class BopomofoModel(bytes: ByteArray) {
    class QuantizedMatrix(
        val rows: Int,
        val cols: Int,
        val scales: FloatArray,
        val values: ByteArray
    ) {
        fun dotRow(row: Int, vector: FloatArray): Float {
            var sum = 0f
            var offset = row * cols
            for (col in 0 until cols) sum += values[offset++] * vector[col]
            return sum * scales[row]
        }
    }

    class FloatMatrix(val rows: Int, val cols: Int, val weights: FloatArray) {
        constructor(matrix: QuantizedMatrix) : this(
            matrix.rows, matrix.cols,
            FloatArray(matrix.rows * matrix.cols) { matrix.values[it] * matrix.scales[it / matrix.cols] }
        )

        fun multiplyBatch(
            inputs: FloatArray,
            batchSize: Int,
            bias: FloatArray,
            outputs: FloatArray
        ) {
            for (row in 0 until rows) {
                val rowOffset = row * cols
                val rowBias = bias[row]
                for (batch in 0 until batchSize) {
                    var sum = 0f
                    val inputOffset = batch * cols
                    for (col in 0 until cols) sum += weights[rowOffset + col] * inputs[inputOffset + col]
                    outputs[batch * rows + row] = sum + rowBias
                }
            }
        }
    }

    class Layer(
        val inputWeights: FloatMatrix,
        val hiddenWeights: FloatMatrix,
        val inputBias: FloatArray,
        val hiddenBias: FloatArray
    )

    class State(val hidden: Array<FloatArray>, val output: FloatArray)

    val layerCount: Int
    val hiddenSize: Int
    val embeddingSize: Int
    private val embedding: QuantizedMatrix
    private val layers: Array<Layer>
    private val projection: FloatMatrix
    private val projectionBias: FloatArray
    private val outputBias: FloatArray
    val outputSize: Int
    val bosIndex: Int

    init {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        layerCount = buffer.int
        hiddenSize = buffer.int
        embeddingSize = buffer.int
        fun readMatrix(): QuantizedMatrix {
            val rows = buffer.int
            val cols = buffer.int
            val scales = buffer.readFloats(rows)
            val values = ByteArray(rows * cols).also { buffer.get(it) }
            return QuantizedMatrix(rows, cols, scales, values)
        }

        fun readVector(): FloatArray = buffer.readFloats(buffer.int)
        embedding = readMatrix()
        layers = Array(layerCount) {
            Layer(
                FloatMatrix(readMatrix()),
                FloatMatrix(readMatrix()),
                readVector(),
                readVector()
            )
        }
        projection = FloatMatrix(readMatrix())
        projectionBias = readVector()
        outputBias = readVector()
        outputSize = outputBias.size
        bosIndex = embedding.rows - 1
    }

    fun getInitialState(): State =
        stepBatch(listOf(Array(layerCount) { FloatArray(hiddenSize) }), intArrayOf(bosIndex))[0]

    fun advance(state: State, vocabIndex: Int): State =
        stepBatch(listOf(state.hidden), intArrayOf(vocabIndex))[0]

    fun stepBatch(hiddenStates: List<Array<FloatArray>>, vocabIndices: IntArray): List<State> {
        val batchSize = vocabIndices.size
        var inputs = FloatArray(batchSize * embeddingSize)
        for (batch in 0 until batchSize) {
            val vocabIndex = vocabIndices[batch]
            val scale = embedding.scales[vocabIndex]
            for (col in 0 until embeddingSize) {
                inputs[batch * embeddingSize + col] =
                    embedding.values[vocabIndex * embeddingSize + col] * scale
            }
        }
        val newHidden = Array(batchSize) { arrayOfNulls<FloatArray>(layerCount) }
        val inputGates = FloatArray(batchSize * 3 * hiddenSize)
        val hiddenGates = FloatArray(batchSize * 3 * hiddenSize)
        val previousHidden = FloatArray(batchSize * hiddenSize)
        for (layerIndex in 0 until layerCount) {
            val layer = layers[layerIndex]
            for (batch in 0 until batchSize) {
                System.arraycopy(
                    hiddenStates[batch][layerIndex],
                    0,
                    previousHidden,
                    batch * hiddenSize,
                    hiddenSize
                )
            }
            layer.inputWeights.multiplyBatch(inputs, batchSize, layer.inputBias, inputGates)
            layer.hiddenWeights.multiplyBatch(
                previousHidden,
                batchSize,
                layer.hiddenBias,
                hiddenGates
            )
            val nextInputs = FloatArray(batchSize * hiddenSize)
            for (batch in 0 until batchSize) {
                val gateOffset = batch * 3 * hiddenSize
                val hidden = FloatArray(hiddenSize)
                for (unit in 0 until hiddenSize) {
                    val resetGate =
                        sigmoid(inputGates[gateOffset + unit] + hiddenGates[gateOffset + unit])
                    val updateGate = sigmoid(
                        inputGates[gateOffset + hiddenSize + unit] + hiddenGates[gateOffset + hiddenSize + unit]
                    )
                    val candidate = tanh(
                        inputGates[gateOffset + 2 * hiddenSize + unit] + resetGate * hiddenGates[gateOffset + 2 * hiddenSize + unit]
                    )
                    hidden[unit] =
                        (1 - updateGate) * candidate + updateGate * previousHidden[batch * hiddenSize + unit]
                }
                newHidden[batch][layerIndex] = hidden
                System.arraycopy(hidden, 0, nextInputs, batch * hiddenSize, hiddenSize)
            }
            inputs = nextInputs
        }
        val outputs = FloatArray(batchSize * embeddingSize)
        projection.multiplyBatch(inputs, batchSize, projectionBias, outputs)
        return List(batchSize) { batch ->
            State(
                Array(layerCount) { newHidden[batch][it]!! },
                outputs.copyOfRange(batch * embeddingSize, (batch + 1) * embeddingSize)
            )
        }
    }

    fun getLogit(state: State, vocabIndex: Int): Float =
        embedding.dotRow(vocabIndex, state.output) + outputBias[vocabIndex]

    private fun sigmoid(value: Float): Float = 1f / (1f + exp(-value))
}
