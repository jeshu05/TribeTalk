package com.alchemists.tribetalk.translation

import java.io.File

class TranslationMemory(private val file: File) {
    private val entries = mutableListOf<TranslationEntry>()

    init {
        loadMemory()
    }

    private fun loadMemory() {
        if (!file.exists()) return
        try {
            file.forEachLine { line ->
                val parts = line.split("|")
                if (parts.size >= 4) {
                    val srcText = parts[0]
                    val tgtText = parts[1]
                    val srcLang = Language.valueOf(parts[2])
                    val tgtLang = Language.valueOf(parts[3])
                    val category = if (parts.size >= 5) parts[4] else "Teacher Correction"
                    
                    entries.add(
                        TranslationEntry(
                            sourceText = srcText,
                            targetText = tgtText,
                            sourceLang = srcLang,
                            targetLang = tgtLang,
                            category = category,
                            confidence = "High (Teacher Validated)"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addCorrection(entry: TranslationEntry) {
        entries.removeAll { 
            it.sourceText.equals(entry.sourceText, ignoreCase = true) && 
            it.sourceLang == entry.sourceLang && 
            it.targetLang == entry.targetLang 
        }
        entries.add(entry)
        saveMemory()
    }

    fun findCorrection(text: String, source: Language, target: Language): TranslationEntry? {
        val normalizedInput = text.lowercase().replace(Regex("[?.!,]"), "").trim()
        return entries.firstOrNull { entry ->
            val normalizedSource = entry.sourceText.lowercase().replace(Regex("[?.!,]"), "").trim()
            normalizedSource == normalizedInput && entry.sourceLang == source && entry.targetLang == target
        }
    }

    private fun saveMemory() {
        try {
            file.parentFile?.mkdirs()
            file.printWriter().use { out ->
                entries.forEach { entry ->
                    out.println("${entry.sourceText}|${entry.targetText}|${entry.sourceLang.name}|${entry.targetLang.name}|${entry.category}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getEntries(): List<TranslationEntry> {
        return entries.toList()
    }

    fun clearMemory() {
        entries.clear()
        if (file.exists()) {
            file.delete()
        }
    }
}
