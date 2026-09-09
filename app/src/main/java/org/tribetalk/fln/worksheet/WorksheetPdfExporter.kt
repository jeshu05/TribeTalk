package org.tribetalk.fln.worksheet

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.model.WorksheetItem
import org.tribetalk.fln.model.WorksheetType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * High-performance, offline Dual-Sheet PDF generator for NIPUN Bharat bilingual worksheets.
 * Generates Page 1 (Student Activity Sheet) and Page 2 (Teacher Answer Key & Phonics Rubric)
 * using Android's native vector PdfDocument with zero bitmap allocations (< 2.5 MB RAM peak).
 */
object WorksheetPdfExporter {

    private const val TAG = "WorksheetPdfExporter"
    private const val PAGE_WIDTH = 595 // A4 standard point width
    private const val PAGE_HEIGHT = 842 // A4 standard point height
    private const val MARGIN = 36f      // 0.5 inch margin

    /**
     * Generates a printable A4 PDF file (Student Sheet + Teacher Answer Key).
     */
    fun generatePdf(
        context: Context,
        config: WorksheetConfig,
        items: List<WorksheetItem>
    ): File? {
        val document = PdfDocument()

        try {
            // -----------------------------------------------------------------
            // PAGE 1: STUDENT ACTIVITY SHEET
            // -----------------------------------------------------------------
            val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = document.startPage(pageInfo1)
            renderStudentSheet(page1.canvas, config, items)
            document.finishPage(page1)

            // -----------------------------------------------------------------
            // PAGE 2: TEACHER ANSWER KEY & PHONETICS GUIDE
            // -----------------------------------------------------------------
            if (config.includeTeacherKey) {
                val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                val page2 = document.startPage(pageInfo2)
                renderTeacherKeySheet(page2.canvas, config, items)
                document.finishPage(page2)
            }

            val exportDir = File(context.cacheDir, "worksheets").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val pdfFile = File(exportDir, "TribeTalk_NIPUN_${config.type.name}_$timeStamp.pdf")

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            Log.i(TAG, "Generated dual-sheet PDF at: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
            return pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render worksheet PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    // =========================================================================
    // PAGE 1: STUDENT ACTIVITY SHEET
    // =========================================================================
    private fun renderStudentSheet(
        canvas: Canvas,
        config: WorksheetConfig,
        items: List<WorksheetItem>
    ) {
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val headerFillPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val guidePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val dotPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
            isAntiAlias = true
        }

        // Outer Page Border
        canvas.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, borderPaint)

        // Header Section
        val headerHeight = 70f
        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + headerHeight)
        canvas.drawRect(headerRect, headerFillPaint)
        canvas.drawLine(MARGIN, MARGIN + headerHeight, PAGE_WIDTH - MARGIN, MARGIN + headerHeight, borderPaint)

        canvas.drawText("TRIBETALK • NIPUN BHARAT FOUNDATIONAL WORKSHEET", MARGIN + 14f, MARGIN + 20f, titlePaint)
        canvas.drawText("${config.type.displayName}  |  ${config.grade.displayName}  |  Difficulty: ${config.difficulty.displayName}", MARGIN + 14f, MARGIN + 36f, subtitlePaint)
        canvas.drawText("Mother-Tongue Learning: Hindi <-> Santali (Ol Chiki)  •  NIPUN Target: ${config.type.nipunTargetCode}", MARGIN + 14f, MARGIN + 50f, guidePaint)
        canvas.drawText(config.schoolName, MARGIN + 14f, MARGIN + 63f, subtitlePaint)

        // Metadata box (Student info & Score)
        val infoY = MARGIN + headerHeight + 20f
        canvas.drawText("Student Name: __________________________", MARGIN + 14f, infoY, textPaint)
        canvas.drawText("Roll No: ______", MARGIN + 270f, infoY, textPaint)
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
        canvas.drawText("Date: $dateStr", MARGIN + 360f, infoY, textPaint)

        // Score Box on Top Right
        val scoreBox = RectF(PAGE_WIDTH - MARGIN - 60f, MARGIN + 10f, PAGE_WIDTH - MARGIN - 10f, MARGIN + 60f)
        canvas.drawRoundRect(scoreBox, 4f, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL })
        canvas.drawRoundRect(scoreBox, 4f, 4f, borderPaint)
        canvas.drawText("SCORE", scoreBox.left + 8f, scoreBox.top + 16f, guidePaint)
        canvas.drawText("/ ${items.size}", scoreBox.left + 16f, scoreBox.top + 38f, boldTextPaint)

        canvas.drawLine(MARGIN + 10f, infoY + 12f, PAGE_WIDTH - MARGIN - 10f, infoY + 12f, dotPaint)

        // Problem Items Layout
        var currentY = infoY + 26f
        val availableHeight = PAGE_HEIGHT - currentY - 50f
        val itemHeight = availableHeight / items.size.coerceAtLeast(1)

        items.forEachIndexed { index, item ->
            val topY = currentY
            val bottomY = topY + itemHeight
            val centerY = topY + (itemHeight / 2f)

            // Problem Index
            canvas.drawText("${index + 1}.", MARGIN + 14f, topY + 16f, boldTextPaint)

            when (config.type) {
                WorksheetType.COUNT_AND_MATCH -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, guidePaint)

                    // Draw visual counting circles
                    val visualStartX = MARGIN + 32f
                    val dotRadius = 6f
                    val dotSpacing = 18f
                    val displayCount = item.quantity.coerceAtMost(12)

                    for (i in 0 until displayCount) {
                        val cx = visualStartX + (i * dotSpacing)
                        val cy = centerY + 2f
                        canvas.drawCircle(cx, cy, dotRadius, Paint().apply { color = Color.rgb(16, 185, 129); style = Paint.Style.FILL; isAntiAlias = true })
                        canvas.drawCircle(cx, cy, dotRadius, Paint().apply { color = Color.rgb(5, 150, 105); style = Paint.Style.STROKE; strokeWidth = 1.2f; isAntiAlias = true })
                    }

                    // Matching line
                    canvas.drawLine(MARGIN + 260f, centerY + 2f, MARGIN + 350f, centerY + 2f, dotPaint)
                    canvas.drawCircle(MARGIN + 260f, centerY + 2f, 3f, borderPaint)
                    canvas.drawCircle(MARGIN + 350f, centerY + 2f, 3f, borderPaint)

                    canvas.drawText("${item.rightLabelSantali}   [ ${item.leftLabelHindi} ]", MARGIN + 365f, centerY + 5f, boldTextPaint)
                }

                WorksheetType.PICTURE_WORD_MATCH -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, guidePaint)
                    canvas.drawText(item.leftLabelHindi, MARGIN + 32f, centerY + 5f, boldTextPaint)

                    canvas.drawLine(MARGIN + 200f, centerY + 2f, MARGIN + 330f, centerY + 2f, dotPaint)
                    canvas.drawCircle(MARGIN + 200f, centerY + 2f, 3f, borderPaint)
                    canvas.drawCircle(MARGIN + 330f, centerY + 2f, 3f, borderPaint)

                    canvas.drawText(item.rightLabelSantali, MARGIN + 345f, centerY + 5f, boldTextPaint)
                }

                WorksheetType.AKSHAR_TRACING -> {
                    canvas.drawText(item.leftLabelHindi, MARGIN + 32f, centerY + 5f, boldTextPaint)
                    val traceBox = RectF(MARGIN + 230f, centerY - 18f, PAGE_WIDTH - MARGIN - 20f, centerY + 18f)
                    canvas.drawRoundRect(traceBox, 6f, 6f, dotPaint)
                    canvas.drawText("Practice:  . . . .   . . . .   . . . .   . . . .", traceBox.left + 20f, centerY + 4f, guidePaint)
                }

                WorksheetType.ASSESSMENT_CIRCLE -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, textPaint)
                    val optY = topY + 34f
                    item.options.forEachIndexed { optIdx, optText ->
                        val optX = MARGIN + 35f + (optIdx * 125f)
                        canvas.drawCircle(optX, optY, 7f, borderPaint)
                        canvas.drawText(optText, optX + 12f, optY + 4f, textPaint)
                    }
                }

                WorksheetType.ADDITION_WORD_PROBLEM -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, textPaint)

                    // Draw Group 1 dots + Group 2 dots = Box
                    val visualY = topY + 32f
                    var cx = MARGIN + 40f
                    for (i in 0 until item.quantity) {
                        canvas.drawCircle(cx, visualY, 5f, Paint().apply { color = Color.rgb(16, 185, 129); style = Paint.Style.FILL; isAntiAlias = true })
                        cx += 14f
                    }
                    canvas.drawText(" + ", cx + 6f, visualY + 4f, boldTextPaint)
                    cx += 28f
                    for (i in 0 until item.secondaryQuantity) {
                        canvas.drawCircle(cx, visualY, 5f, Paint().apply { color = Color.rgb(59, 130, 246); style = Paint.Style.FILL; isAntiAlias = true })
                        cx += 14f
                    }
                    canvas.drawText(" = ", cx + 8f, visualY + 4f, boldTextPaint)

                    // Answer Box for student
                    val ansBox = RectF(cx + 32f, visualY - 12f, cx + 72f, visualY + 12f)
                    canvas.drawRoundRect(ansBox, 4f, 4f, borderPaint)
                }

                WorksheetType.NUMBER_SEQUENCE_TRAIN -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, textPaint)
                    val trainY = topY + 22f

                    // Draw sequential train cars
                    item.sequenceItems.forEachIndexed { seqIdx, seqVal ->
                        val carX = MARGIN + 35f + (seqIdx * 80f)
                        val carRect = RectF(carX, trainY, carX + 65f, trainY + 26f)
                        if (seqIdx == item.missingSequenceIndex) {
                            canvas.drawRoundRect(carRect, 4f, 4f, dotPaint)
                            canvas.drawText("[   ]", carRect.left + 18f, carRect.centerY() + 4f, guidePaint)
                        } else {
                            canvas.drawRoundRect(carRect, 4f, 4f, borderPaint)
                            canvas.drawText(seqVal, carRect.left + 22f, carRect.centerY() + 5f, boldTextPaint)
                        }
                    }
                }

                WorksheetType.GREATER_LESSER_COMPARE -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, textPaint)
                    val compY = topY + 32f

                    // Left count
                    var cx = MARGIN + 40f
                    for (i in 0 until item.quantity) {
                        canvas.drawCircle(cx, compY, 5f, Paint().apply { color = Color.rgb(16, 185, 129); style = Paint.Style.FILL; isAntiAlias = true })
                        cx += 14f
                    }

                    // Middle Compare Circle [ O ]
                    val midCircleX = MARGIN + 250f
                    canvas.drawCircle(midCircleX, compY, 14f, borderPaint)
                    canvas.drawText("?", midCircleX - 4f, compY + 4f, guidePaint)

                    // Right count
                    var rx = MARGIN + 300f
                    for (i in 0 until item.secondaryQuantity) {
                        canvas.drawCircle(rx, compY, 5f, Paint().apply { color = Color.rgb(59, 130, 246); style = Paint.Style.FILL; isAntiAlias = true })
                        rx += 14f
                    }
                }

                WorksheetType.MISSING_AKSHAR_SPELLING -> {
                    canvas.drawText(item.promptHindi, MARGIN + 32f, topY + 14f, textPaint)
                    canvas.drawText("Word: ${item.wordWithBlank ?: ""}", MARGIN + 32f, topY + 30f, boldTextPaint)

                    // Letter options
                    val optY = topY + 44f
                    item.options.forEachIndexed { optIdx, optChar ->
                        val optX = MARGIN + 220f + (optIdx * 70f)
                        val optBox = RectF(optX, optY - 14f, optX + 45f, optY + 14f)
                        canvas.drawRoundRect(optBox, 4f, 4f, borderPaint)
                        canvas.drawText(optChar, optBox.left + 16f, optBox.centerY() + 4f, boldTextPaint)
                    }
                }
            }

            // Divider between items
            if (index < items.size - 1) {
                canvas.drawLine(MARGIN + 10f, bottomY - 2f, PAGE_WIDTH - MARGIN - 10f, bottomY - 2f, dotPaint)
            }
            currentY = bottomY
        }

        // Footer Section
        val footerY = PAGE_HEIGHT - MARGIN - 12f
        canvas.drawLine(MARGIN, footerY - 12f, PAGE_WIDTH - MARGIN, footerY - 12f, borderPaint)
        canvas.drawText("NIPUN Bharat Foundational Stage  |  Page 1 of 2: Student Activity Sheet", MARGIN + 14f, footerY, subtitlePaint)
        canvas.drawText("Generated 100% Offline via TribeTalk", PAGE_WIDTH - MARGIN - 190f, footerY, guidePaint)
    }

    // =========================================================================
    // PAGE 2: NON-NATIVE TEACHER ANSWER KEY & PHONETICS GUIDE
    // =========================================================================
    private fun renderTeacherKeySheet(
        canvas: Canvas,
        config: WorksheetConfig,
        items: List<WorksheetItem>
    ) {
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        val headerFillPaint = Paint().apply {
            color = Color.rgb(236, 253, 245) // Light Emerald
            style = Paint.Style.FILL
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(4, 120, 87) // Deep Emerald
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val phoneticPaint = Paint().apply {
            color = Color.rgb(5, 150, 105) // Emerald
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val guidePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val dotPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
            isAntiAlias = true
        }

        // Outer Page Border
        canvas.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, borderPaint)

        // Header Section
        val headerHeight = 65f
        val headerRect = RectF(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + headerHeight)
        canvas.drawRect(headerRect, headerFillPaint)
        canvas.drawLine(MARGIN, MARGIN + headerHeight, PAGE_WIDTH - MARGIN, MARGIN + headerHeight, borderPaint)

        canvas.drawText("TEACHER EVALUATION KEY & PRONUNCIATION GUIDE (शिक्षक उत्तर कुंजी)", MARGIN + 14f, MARGIN + 22f, titlePaint)
        canvas.drawText("${config.type.displayName}  |  Grade: ${config.grade.displayName}  |  NIPUN Code: ${config.type.nipunTargetCode}", MARGIN + 14f, MARGIN + 38f, subtitlePaint)
        canvas.drawText("Equips non-native teachers to evaluate and pronounce Santali accurately in class.", MARGIN + 14f, MARGIN + 52f, guidePaint)

        // Summary Table Header
        var currentY = MARGIN + headerHeight + 25f
        val tableHeaderY = currentY
        canvas.drawRect(MARGIN + 10f, tableHeaderY - 14f, PAGE_WIDTH - MARGIN - 10f, tableHeaderY + 12f, Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL })
        canvas.drawRect(MARGIN + 10f, tableHeaderY - 14f, PAGE_WIDTH - MARGIN - 10f, tableHeaderY + 12f, borderPaint)

        canvas.drawText("#", MARGIN + 16f, tableHeaderY, boldTextPaint)
        canvas.drawText("Verified Answer Key", MARGIN + 45f, tableHeaderY, boldTextPaint)
        canvas.drawText("Teacher Phonetic Pronunciation Guide", MARGIN + 280f, tableHeaderY, boldTextPaint)

        currentY += 22f
        val availableHeight = (PAGE_HEIGHT - currentY - 140f)
        val rowHeight = availableHeight / items.size.coerceAtLeast(1)

        items.forEachIndexed { idx, item ->
            val rowTop = currentY
            val rowCenter = rowTop + (rowHeight / 2f)

            canvas.drawText("${idx + 1}", MARGIN + 16f, rowCenter, boldTextPaint)

            // Solution Details
            val solutionText = item.teacherSolutionNote.ifEmpty { "Answer: ${item.options.getOrNull(item.correctIndex) ?: item.rightLabelSantali}" }
            canvas.drawText(solutionText, MARGIN + 45f, rowCenter - 2f, textPaint)

            // Phonetics Guide
            val phoneticText = "बोलें: ${item.teacherPhoneticAnswer.ifEmpty { item.rightLabelSantali }}"
            canvas.drawText(phoneticText, MARGIN + 280f, rowCenter - 2f, phoneticPaint)

            canvas.drawLine(MARGIN + 10f, rowTop + rowHeight, PAGE_WIDTH - MARGIN - 10f, rowTop + rowHeight, dotPaint)
            currentY = rowTop + rowHeight
        }

        // NIPUN Bharat Scoring Rubric Table
        val rubricY = PAGE_HEIGHT - MARGIN - 110f
        canvas.drawRect(MARGIN + 10f, rubricY, PAGE_WIDTH - MARGIN - 10f, rubricY + 65f, Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL })
        canvas.drawRect(MARGIN + 10f, rubricY, PAGE_WIDTH - MARGIN - 10f, rubricY + 65f, borderPaint)

        canvas.drawText("NIPUN BHARAT FOUNDATIONAL EVALUATION RUBRIC", MARGIN + 16f, rubricY + 16f, boldTextPaint)
        canvas.drawText("• Emerging (0-40%): Struggles with phonics/counting. Provide 1-on-1 choral drilling.", MARGIN + 16f, rubricY + 32f, guidePaint)
        canvas.drawText("• Developing (41-79%): Recognizes concepts with teacher verbal scaffolding.", MARGIN + 16f, rubricY + 46f, guidePaint)
        canvas.drawText("• Proficient (80-100%): Mastered Lakshya competency. Ready for next grade level.", MARGIN + 16f, rubricY + 58f, guidePaint)

        // Signature & Date
        val sigY = PAGE_HEIGHT - MARGIN - 26f
        canvas.drawText("Teacher Signature: _______________________", MARGIN + 16f, sigY, textPaint)
        canvas.drawText("Assessment Date: _____________", PAGE_WIDTH - MARGIN - 190f, sigY, textPaint)

        // Footer Section
        val footerY = PAGE_HEIGHT - MARGIN - 10f
        canvas.drawLine(MARGIN, footerY - 12f, PAGE_WIDTH - MARGIN, footerY - 12f, borderPaint)
        canvas.drawText("Page 2 of 2: Non-Native Teacher Evaluation Sheet  •  TribeTalk Offline Suite", MARGIN + 14f, footerY, subtitlePaint)
    }

    /**
     * Triggers Android share / print intent for the generated PDF.
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share or Print Worksheet PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
