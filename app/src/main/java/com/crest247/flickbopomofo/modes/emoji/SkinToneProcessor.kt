package com.crest247.flickbopomofo.modes.emoji

import com.crest247.flickbopomofo.modes.emoji.EmojiData.humanPrefixMap
import com.crest247.flickbopomofo.modes.emoji.EmojiData.humanTypeMap

object SkinToneProcessor {
    private const val SKIN_LIGHT = "\uD83C\uDFFB"
    private const val SKIN_MEDIUM_LIGHT = "\uD83C\uDFFC"
    private const val SKIN_MEDIUM = "\uD83C\uDFFD"
    private const val SKIN_MEDIUM_DARK = "\uD83C\uDFFE"
    private const val SKIN_DARK = "\uD83C\uDFFF"
    private const val VS16 = "\uFE0F"

    private val SKIN_VARIANTS = listOf(
        SKIN_LIGHT, SKIN_MEDIUM_LIGHT, SKIN_MEDIUM, SKIN_MEDIUM_DARK, SKIN_DARK
    )

    fun hasSkinToneVariants(emoji: String): Boolean = humanTypeMap.containsKey(emoji)

    private fun applySkinTone(baseEmoji: String, skinTone: String): String {
        if (baseEmoji.isEmpty()) return ""

        val firstLen = Character.charCount(baseEmoji.codePointAt(0))
        val head = baseEmoji.substring(0, firstLen)
        val tail = baseEmoji.substring(firstLen).removePrefix(VS16)

        return "$head$skinTone$tail"
    }

    private fun getCodePointCount(text: String): Int = text.codePointCount(0, text.length)

    private fun getSecondCodePointString(text: String): String? {
        if (text.isEmpty()) return null

        val firstLen = Character.charCount(text.codePointAt(0))
        if (firstLen >= text.length) return null

        val secondLen = Character.charCount(text.codePointAt(firstLen))
        return text.substring(firstLen, firstLen + secondLen)
    }

    fun generateVariants(emoji: String): List<String> {
        val type = humanTypeMap[emoji] ?: return emptyList()

        return SKIN_VARIANTS.map { skinTone ->
            when (type) {
                "One" -> applySkinTone(emoji, skinTone)

                "Same" -> {
                    val directKey = emoji + SKIN_LIGHT
                    if (humanPrefixMap.containsKey(directKey))
                        emoji + skinTone
                    else {
                        val stripped = emoji.removeSuffix(VS16)
                        val endsWithGender = if (stripped.isNotEmpty()) {
                            val lastCodePoint = stripped.codePointBefore(stripped.length)
                            lastCodePoint == 0x2640 || lastCodePoint == 0x2642
                        } else false

                        if (endsWithGender)
                            applySkinTone(emoji, skinTone)
                        else
                            applySkinTone(emoji, skinTone) + skinTone
                    }

                }

                else -> {
                    val emojiPrefix = humanPrefixMap[emoji] ?: return@map emoji
                    val countPrefix = getCodePointCount(emojiPrefix)
                    val countEmoji = getCodePointCount(emoji)

                    if (countPrefix + 1 == countEmoji)
                        emojiPrefix + skinTone
                    else {
                        val secondChar = getSecondCodePointString(emoji)
                        if (secondChar == skinTone) emoji else emojiPrefix + skinTone
                    }
                }
            }
        }
    }
}