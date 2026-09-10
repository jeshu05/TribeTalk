package org.tribetalk.worksheet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import org.tribetalk.flashcards.NIPUNLearningFramework
import java.io.File
import java.io.FileOutputStream

/**
 * Bilingual NIPUN Bharat Worksheet Generator (Pedagogy Tracing Renderer).
 *
 * Input: Dual-script strings (Hindi Devanagari + Santali Ol Chiki).
 * Workflow: Renders vector-based A4 bilingual NIPUN worksheets with tracing grids,
 *           saving generated PDFs directly to local cache storage.
 */
class BilingualWorksheetGenerator(private val context: Context) {

    companion object {
        const val PAGE_WIDTH = 595  // Standard A4 width in points (72 DPI)
        const val PAGE_HEIGHT = 842 // Standard A4 height in points
    }

    data class WorksheetEntry(
        val hindi: String,
        val santaliOlChiki: String,
        val phoneticGuide: String,
        val category: String
    )

    /**
     * Generates a printable A4 bilingual worksheet with tracing grids and saves it as a PDF.
     */
    fun generateWorksheet(
        title: String = "NIPUN Bharat Bilingual Worksheet",
        gradeLevel: String = "Grade 1-3 Foundational",
        entries: List<WorksheetEntry> = getDefaultEntries()
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dottedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F46E5")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        // 1. Header Banner
        paint.color = Color.parseColor("#4F46E5") // Indigo primary
        paint.style = Paint.Style.FILL
        canvas.drawRect(30f, 30f, PAGE_WIDTH - 30f, 95f, paint)

        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("TribeTalk • $title", 45f, 65f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("Ministry of Education NIPUN Bharat FLN • $gradeLevel", 45f, 85f, paint)

        // 2. Student Info Box
        paint.color = Color.DKGRAY
        paint.textSize = 11f
        canvas.drawText("Student Name: _______________________", 45f, 125f, paint)
        canvas.drawText("Date: ____________", PAGE_WIDTH - 180f, 125f, paint)
        canvas.drawText("School: _____________________________", 45f, 145f, paint)
        canvas.drawText("Class: ___________", PAGE_WIDTH - 180f, 145f, paint)

        // Divider
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, 160f, PAGE_WIDTH - 30f, 160f, paint)

        // 3. Tracing Grids for Dual-Script Vocabulary
        var currentY = 195f
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Section A: Handwriting & Tracing Grid (Hindi ➜ Ol Chiki)", 45f, currentY, paint)
        currentY += 25f

        val displayEntries = entries.take(5)
        for (item in displayEntries) {
            // Background card box
            paint.color = Color.parseColor("#F8FAFC")
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(40f, currentY, PAGE_WIDTH - 40f, currentY + 75f, 8f, 8f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(40f, currentY, PAGE_WIDTH - 40f, currentY + 75f, 8f, 8f, paint)

            // Hindi source word
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("${item.hindi} (${item.category})", 55f, currentY + 28f, paint)

            // Phonetic Pronunciation Guide
            paint.color = Color.parseColor("#0D9488")
            paint.textSize = 11f
            paint.isFakeBoldText = false
            canvas.drawText("Pronunciation: ${item.phoneticGuide}", 55f, currentY + 50f, paint)

            // Ol Chiki Model Text
            paint.color = Color.parseColor("#4F46E5")
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(item.santaliOlChiki, 270f, currentY + 38f, paint)

            // Handwriting Tracing Practice Boxes (4 empty practice slots)
            val boxStartX = 360f
            val boxWidth = 40f
            val boxHeight = 45f
            for (b in 0 until 4) {
                val bx = boxStartX + (b * (boxWidth + 8f))
                val by = currentY + 15f
                canvas.drawRect(bx, by, bx + boxWidth, by + boxHeight, gridPaint)
                // Midline guide
                canvas.drawLine(bx, by + (boxHeight / 2f), bx + boxWidth, by + (boxHeight / 2f), dottedPaint)
            }

            currentY += 88f
        }

        // 4. Section B: Classroom Conversation Activity
        currentY += 10f
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Section B: Classroom Practice (Speak out loud with teacher)", 45f, currentY, paint)
        currentY += 25f

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("1. Teacher says: किताब खोलो ➜ Student responds: ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ (पुथी झीज मे)", 55f, currentY, paint)
        currentY += 18f
        canvas.drawText("2. Teacher says: पानी पीना है ➜ Student responds: ᱫᱟᱜ ᱧᱩ ᱥᱟᱱᱟᱹᱧ ᱠᱟᱱᱟ (दाग ञु सानाञ काना)", 55f, currentY, paint)
        currentY += 18f
        canvas.drawText("3. Count in Ol Chiki: 1 (ᱢᱤᱫ)  2 (ᱵᱟᱨ)  3 (ᱯᱮ)  4 (ᱯᱩᱱ)  5 (ᱢᱚᱬᱮ)", 55f, currentY, paint)

        // 5. Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        canvas.drawText("Generated offline by TribeTalk Engine • NIPUN Bharat MTB-MLE Initiative", 45f, PAGE_HEIGHT - 35f, paint)

        pdfDocument.finishPage(page)

        // 6. Write PDF to local cache storage
        val outputDir = File(context.cacheDir, "worksheets").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "NIPUN_Worksheet_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun getDefaultEntries(): List<WorksheetEntry> {
        val entries = mutableListOf<WorksheetEntry>()
        for ((category, templates) in NIPUNLearningFramework.presetTopicTemplates) {
            val catName = category.substringBefore(" (")
            for (t in templates) {
                entries.add(
                    WorksheetEntry(
                        hindi = t.hindi,
                        santaliOlChiki = t.santaliOlChiki,
                        phoneticGuide = t.phoneticDevanagari,
                        category = catName
                    )
                )
            }
        }
        return entries
    }
}
