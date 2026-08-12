package com.crest247.flickbopomofo.modes.shared.array

import android.content.Context
import com.crest247.flickbopomofo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.floor

object ArrayDecoder {
    private var keySumTable: List<String> = emptyList()
    private var valueSumTable: List<String> = emptyList()

    var isLoaded = false
        private set

    suspend fun loadData(context: Context) {
        withContext(Dispatchers.IO) {
            val keys = context.resources.openRawResource(R.raw.array30_key_sum).bufferedReader()
                .readLines()
            val values = context.resources.openRawResource(R.raw.array30_value_sum).bufferedReader()
                .readLines()
            withContext(Dispatchers.Main) {
                keySumTable = keys
                valueSumTable = values
                isLoaded = true
            }
        }
    }

    fun decode(lookupString: String): List<String> {
        if (!isLoaded || keySumTable.isEmpty() || valueSumTable.isEmpty() || lookupString.isEmpty()) return emptyList()

        if (lookupString.length > 5 || (lookupString.length == 5 && lookupString[4] != 'i'))
            return emptyList()
        if (lookupString.length == 2 && lookupString[0] == 'w' && lookupString[1] in '0'..'9')
            return getValueList(150 + lookupString[1].code - '0'.code)
        val sum = calcRadicalsSum(lookupString)
        val hashValue = calcHashValue(sum)
        return getKeyValue(hashValue, sum)
    }

    private fun getRadicalCode(radical: Char): Int {
        return when (radical) {
            in 'A'..'Z' -> radical.code - 60
            in 'a'..'z' -> radical.code - 92
            ',', '.', '/' -> radical.code - 43
            ';', ':' -> 2
            '<', '>', '?' -> radical.code - 59
            else -> 0
        }
    }

    private fun calcRadicalsSum(radicals: String): Int {
        var factor = 1
        var sum = 0
        for (radical in radicals) {
            val code = getRadicalCode(radical)
            if (code == 0) return 0
            sum += code * factor
            factor *= 30
        }
        if (radicals.length == 5) sum -= 9720000
        return sum
    }

    private fun calcHashValue(radicalsSum: Int): Int {
        return floor((radicalsSum * 0.6180339887) % 1 * 32767).toInt()
    }

    private fun getKeyValue(lineNumber: Int, radicalsSum: Int): List<String> {
        if (lineNumber !in keySumTable.indices) return emptyList()
        val lineData = keySumTable[lineNumber].split('\t')
        if (lineData.size == 1) {
            return if (lineData[0] == "") emptyList() else lineData[0].split(' ')
        }
        for ((index, data) in lineData.withIndex()) {
            if (data.toIntOrNull() == radicalsSum) {
                return getValueList(lineNumber, index)
            }
        }
        return emptyList()
    }

    private fun getValueList(lineNumber: Int, index: Int = 0): List<String> {
        val data = valueSumTable[lineNumber].split('\t').getOrNull(index) ?: return emptyList()
        return data.split(' ')
    }

    private val arrayAlphabet = listOf("pqwertyuio", ";asdfghjkl", "/zxcvbnm,.")
}
