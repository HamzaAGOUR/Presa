package com.presa.app.data

import com.presa.app.domain.model.KeyChord

/**
 * Stable, dependency-free text format for per-button remaps:
 * `action.id=modifier:usage;action.id=modifier:usage`.
 */
object KeyRemapCodec {
    fun encode(overrides: Map<String, KeyChord>): String =
        overrides.entries
            .sortedBy { it.key }
            .joinToString(separator = ";") { (id, chord) ->
                "$id=${chord.modifier}:${chord.usage}"
            }

    fun decode(raw: String?): Map<String, KeyChord> {
        if (raw.isNullOrBlank()) return emptyMap()

        val result = LinkedHashMap<String, KeyChord>()
        raw.split(';').forEach { entry ->
            val id = entry.substringBefore('=', missingDelimiterValue = "").trim()
            val value = entry.substringAfter('=', missingDelimiterValue = "").trim()
            if (id.isEmpty() || value.isEmpty() || '=' !in entry) return@forEach

            val parts = value.split(':')
            if (parts.size != 2) return@forEach

            val modifier = parts[0].trim().toIntOrNull() ?: return@forEach
            val usage = parts[1].trim().toIntOrNull() ?: return@forEach
            if (modifier !in 0..0xFF || usage !in 1..0xFF) return@forEach

            result[id] = KeyChord(modifier, usage)
        }
        return result
    }
}
