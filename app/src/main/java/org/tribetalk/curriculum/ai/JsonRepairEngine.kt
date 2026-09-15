package org.tribetalk.curriculum.ai

import org.json.JSONArray
import org.json.JSONObject

/**
 * Resilient JSON repair engine for small language models (e.g. Qwen 2.5 0.5B).
 *
 * Small models frequently encounter:
 * 1. Markdown code fences (```json ... ```) or conversational preambles
 * 2. Premature truncation before closing braces/quotes
 * 3. Trailing commas before closing brackets
 * 4. Key aliasing (e.g. "activity_type" instead of "activityType")
 * 5. Python-style booleans (True/False/None)
 *
 * This engine deterministically repairs malformed output offline in < 1ms.
 */
object JsonRepairEngine {

    /**
     * Extracts, repairs, and parses a JSONObject from raw LLM output.
     */
    fun repairAndParse(rawOutput: String): JSONObject? {
        if (rawOutput.isBlank()) return null

        val cleaned = cleanMarkdownAndPreamble(rawOutput)
        val extractedJson = extractBalancedJsonString(cleaned) ?: cleaned

        // Strategy 1: Direct parse
        try {
            val direct = JSONObject(extractedJson)
            return normalizeAliases(direct)
        } catch (_: Exception) {
            // Proceed to sanitization
        }

        // Strategy 2: Sanitize syntax (trailing commas, python literals, unquoted keys)
        val sanitized = sanitizeJsonSyntax(extractedJson)
        try {
            val obj = JSONObject(sanitized)
            return normalizeAliases(obj)
        } catch (_: Exception) {
            // Proceed to bracket completion
        }

        // Strategy 3: Truncated JSON repair (balance unclosed quotes and braces)
        val balanced = balanceTruncatedJson(sanitized)
        try {
            val obj = JSONObject(balanced)
            return normalizeAliases(obj)
        } catch (_: Exception) {
            // Strategy 4: Fallback heuristic extraction
            return extractKeyValuesHeuristic(rawOutput)
        }
    }

    private fun cleanMarkdownAndPreamble(input: String): String {
        var text = input.trim()

        // Strip ```json ... ``` or ``` ... ```
        if (text.contains("```")) {
            val startCode = text.indexOf("```")
            val endCode = text.lastIndexOf("```")
            if (startCode != -1 && endCode > startCode) {
                var inner = text.substring(startCode + 3, endCode).trim()
                if (inner.startsWith("json", ignoreCase = true)) {
                    inner = inner.substring(4).trim()
                }
                text = inner
            }
        }

        return text
    }

    private fun extractBalancedJsonString(text: String): String? {
        val start = text.indexOf('{')
        if (start == -1) return null

        var depth = 0
        var inString = false
        var isEscaped = false
        var endIndex = -1

        for (i in start until text.length) {
            val c = text[i]

            if (inString) {
                if (c == '\\' && !isEscaped) {
                    isEscaped = true
                } else if (c == '"' && !isEscaped) {
                    inString = false
                } else {
                    isEscaped = false
                }
                continue
            }

            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        endIndex = i
                        break
                    }
                }
            }
        }

        return if (endIndex != -1) {
            text.substring(start, endIndex + 1)
        } else {
            // Unclosed JSON object starting at 'start'
            text.substring(start)
        }
    }

    private fun sanitizeJsonSyntax(jsonStr: String): String {
        var result = jsonStr

        // Replace Python literals
        result = result.replace(Regex("(?<=[\\s,:\\[])True(?=[\\s,\\]\\}])"), "true")
        result = result.replace(Regex("(?<=[\\s,:\\[])False(?=[\\s,\\]\\}])"), "false")
        result = result.replace(Regex("(?<=[\\s,:\\[])None(?=[\\s,\\]\\}])"), "null")

        // Remove trailing commas before } or ]
        result = result.replace(Regex(",\\s*([}\\]])"), "$1")

        // Convert single quotes around keys/values to double quotes if standard double quotes are absent
        if (!result.contains("\"") && result.contains("'")) {
            result = result.replace("'", "\"")
        }

        return result
    }

    private fun balanceTruncatedJson(jsonStr: String): String {
        val trimmed = jsonStr.trim()
        val sb = StringBuilder(trimmed)

        var inString = false
        var isEscaped = false
        val braceStack = mutableListOf<Char>()

        for (i in trimmed.indices) {
            val c = trimmed[i]
            if (inString) {
                if (c == '\\' && !isEscaped) {
                    isEscaped = true
                } else if (c == '"' && !isEscaped) {
                    inString = false
                } else {
                    isEscaped = false
                }
                continue
            }

            when (c) {
                '"' -> inString = true
                '{' -> braceStack.add('}')
                '[' -> braceStack.add(']')
                '}', ']' -> {
                    if (braceStack.isNotEmpty() && braceStack.last() == c) {
                        braceStack.removeAt(braceStack.size - 1)
                    }
                }
            }
        }

        // If truncated inside a string literal, close the quote
        if (inString) {
            sb.append('"')
        }

        // Remove any dangling trailing commas or colons before closing braces
        var temp = sb.toString().trim()
        temp = temp.replace(Regex("[,:]\\s*$"), "")
        val repaired = StringBuilder(temp)

        // Close remaining unclosed brackets in reverse order
        while (braceStack.isNotEmpty()) {
            repaired.append(braceStack.removeAt(braceStack.size - 1))
        }

        return repaired.toString()
    }

    /**
     * Normalizes snake_case or legacy aliases to the canonical ActivitySpec keys.
     */
    fun normalizeAliases(source: JSONObject): JSONObject {
        val target = JSONObject()
        val keys = source.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val canonicalKey = when (key.lowercase()) {
                "activity_type", "activitytype", "type", "action" -> "activityType"
                "quantity", "count", "target_quantity", "targetquantity", "object_count", "objectcount", "num_objects" -> "quantity"
                "visual_asset", "visualasset", "asset", "primary_asset", "primaryasset", "primary_object", "primaryobject", "item" -> "visualAsset"
                "secondary_visual_asset", "secondaryvisualasset", "secondary_asset" -> "secondaryVisualAsset"
                "visual_theme", "visualtheme", "theme" -> "visualTheme"
                "visual_layout", "visuallayout", "layout" -> "visualLayout"
                "correct_answer", "correctanswer", "answer", "target_number", "targetnumber" -> "correctAnswer"
                "distractor_options", "distractoroptions", "distractors", "options", "choices" -> "distractorOptions"
                "difficulty", "diff_level" -> "difficulty"
                "items", "sub_items", "questions", "activities" -> "items"
                "instruction_hindi", "instructionhindi" -> "instructionHindi"
                "instruction_santali", "instructionsantali" -> "instructionSantali"
                else -> key
            }

            val value = source.get(key)
            if (value is JSONObject) {
                target.put(canonicalKey, normalizeAliases(value))
            } else if (value is JSONArray && canonicalKey == "items") {
                val normalizedArray = JSONArray()
                for (i in 0 until value.length()) {
                    val item = value.optJSONObject(i)
                    if (item != null) {
                        normalizedArray.put(normalizeAliases(item))
                    } else {
                        normalizedArray.put(value.get(i))
                    }
                }
                target.put(canonicalKey, normalizedArray)
            } else {
                target.put(canonicalKey, value)
            }
        }

        return target
    }

    private fun extractKeyValuesHeuristic(rawText: String): JSONObject? {
        try {
            val obj = JSONObject()

            // Extract activityType
            val typeMatch = Regex("""["']?(?:activity_?type|type)["']?\s*:\s*["']?([A-Za-z_]+)["']?""", RegexOption.IGNORE_CASE).find(rawText)
            if (typeMatch != null) {
                obj.put("activityType", typeMatch.groupValues[1].uppercase())
            }

            // Extract quantity
            val qtyMatch = Regex("""["']?(?:quantity|count|target_?quantity)["']?\s*:\s*(\d+)""", RegexOption.IGNORE_CASE).find(rawText)
            if (qtyMatch != null) {
                obj.put("quantity", qtyMatch.groupValues[1].toIntOrNull() ?: 5)
            }

            // Extract visualAsset
            val assetMatch = Regex("""["']?(?:visual_?asset|asset|primary_?object)["']?\s*:\s*["']?([A-Za-z0-9_]+)["']?""", RegexOption.IGNORE_CASE).find(rawText)
            if (assetMatch != null) {
                obj.put("visualAsset", assetMatch.groupValues[1].lowercase())
            }

            // Extract correctAnswer
            val ansMatch = Regex("""["']?(?:correct_?answer|answer)["']?\s*:\s*["']?([^"',}\]]+)["']?""", RegexOption.IGNORE_CASE).find(rawText)
            if (ansMatch != null) {
                obj.put("correctAnswer", ansMatch.groupValues[1].trim())
            }

            // Extract distractors/options array if possible
            val distMatch = Regex("""["']?(?:distractor_?options|distractors|options)["']?\s*:\s*\[(.*?)\]""", RegexOption.IGNORE_CASE).find(rawText)
            if (distMatch != null) {
                val optsArray = JSONArray()
                val items = distMatch.groupValues[1].split(",")
                for (it in items) {
                    val cleanOpt = it.trim().removeSurrounding("\"").removeSurrounding("'").trim()
                    if (cleanOpt.isNotBlank()) {
                        optsArray.put(cleanOpt)
                    }
                }
                if (optsArray.length() > 0) {
                    obj.put("distractorOptions", optsArray)
                }
            }

            return if (obj.length() >= 2) obj else null
        } catch (_: Exception) {
            return null
        }
    }
}
