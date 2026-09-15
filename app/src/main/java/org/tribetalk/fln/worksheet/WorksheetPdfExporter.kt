package org.tribetalk.fln.worksheet

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import org.tribetalk.fln.model.FlnGrade
import org.tribetalk.fln.model.WorksheetConfig
import org.tribetalk.fln.model.WorksheetItem
import org.tribetalk.fln.model.WorksheetType
import org.tribetalk.fln.pipeline.*
import org.tribetalk.fln.repository.FlnCurriculumRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Authentic NCERT CNCL (National Centre for Children's Literature / National Literacy Centre)
 * bilingual vector PDF generator for Foundational Literacy & Numeracy (FLN).
 *
 * Implements:
 * 1. Official NCERT CNCL bilingual header & domain/chapter context
 * 2. 4-field student details box (नाम, दिनांक, कक्षा, क्रमांक)
 * 3. Prominent imperative bilingual instruction banner (निर्देश / ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ)
 * 4. Authentic 2-column matching format (स्तंभ क / स्तंभ ख) with connecting dots and answer boxes
 * 5. Signature NCERT learning outcome footer ("हमने सीखा / ᱟᱵᱚ ᱵᱚ ᱪᱮᱫ ᱠᱮᱫᱟ")
 * 6. Teacher evaluation rubric with competency checkboxes and signature
 * 7. Page 2 Teacher Reference & Phonics Answer Key
 *
 * 100% offline, native vector PdfDocument rendering, zero emojis.
 */
object WorksheetPdfExporter {

    private const val TAG = "WorksheetPdfExporter"
    private const val PAGE_WIDTH = 595 // Standard A4 points width (72 DPI)
    private const val PAGE_HEIGHT = 842 // Standard A4 points height (72 DPI)
    private const val MARGIN = 32f      // Clean margin

    /**
     * Official NCERT CNCL color scheme with high contrast and photocopy-safe tones.
     */
    private object NcertPalette {
        val NavyPrimary = Color.rgb(18, 52, 102)       // NCERT Dark Navy
        val NavyLight = Color.rgb(238, 244, 255)       // Soft Navy Tint
        val BorderPrimary = Color.rgb(18, 52, 102)     // Outer framing
        val BorderSubtle = Color.rgb(205, 218, 235)    // Grid lines
        val TextDark = Color.rgb(25, 35, 50)           // High readability text
        val TextSecondary = Color.rgb(75, 88, 105)     // Subtitles
        val InstructionBg = Color.rgb(246, 249, 255)   // Directive banner fill
        val InstructionBorder = Color.rgb(180, 205, 240) // Directive banner stroke
        val OutcomeBg = Color.rgb(240, 253, 244)       // Green tint for learning outcome
        val OutcomeBorder = Color.rgb(167, 243, 208)   // Emerald stroke
        val OutcomeText = Color.rgb(22, 101, 52)       // Forest green heading
        val RubricBg = Color.rgb(255, 251, 235)        // Soft amber for teacher evaluation
        val RubricBorder = Color.rgb(253, 230, 138)    // Amber stroke
        val RubricText = Color.rgb(146, 64, 14)        // Warm amber heading
        val MatchingDot = Color.rgb(18, 52, 102)       // Connecting anchor dot
        val CardBg = Color.rgb(255, 255, 255)          // White item card
        val CardBorder = Color.rgb(220, 230, 242)      // Soft item card border

        // Teacher Page Palette
        val TeacherHeaderBg = Color.rgb(255, 248, 235)
        val TeacherBorder = Color.rgb(180, 110, 30)
    }

    /**
     * Generates an authentic 2-page A4 NCERT CNCL bilingual PDF.
     */
    fun generatePdf(
        context: Context,
        config: WorksheetConfig,
        items: List<WorksheetItem>
    ): File? {
        val document = PdfDocument()

        try {
            // PAGE 1: STUDENT ACTIVITY SHEET (NCERT CNCL LAYOUT)
            val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page1 = document.startPage(pageInfo1)
            renderNcertStudentSheet(context, page1.canvas, config, items)
            document.finishPage(page1)

            // PAGE 2: TEACHER ANSWER KEY & PHONICS KEY
            if (config.includeTeacherKey) {
                val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                val page2 = document.startPage(pageInfo2)
                renderTeacherKeySheet(page2.canvas, config, items)
                document.finishPage(page2)
            }

            val exportDir = File(context.cacheDir, "worksheets").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val pdfFile = File(exportDir, "NCERT_CNCL_${config.type.name}_$timeStamp.pdf")

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            Log.i(TAG, "Generated NCERT CNCL bilingual PDF at: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
            return pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render worksheet PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    fun generatePdfFromActivityIRs(
        context: Context,
        config: WorksheetConfig,
        activityIRs: List<ActivityIR>
    ): File? {
        val items = activityIRs.map { WorksheetGenerator.activityIRToWorksheetItem(it) }
        return generatePdf(context, config, items)
    }

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "NCERT CNCL Bilingual Worksheet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Print or Share NCERT Worksheet PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch PDF share intent", e)
        }
    }

    // -------------------------------------------------------------------------
    // Page 1: Student Sheet (NCERT CNCL Bilingual Vector Format)
    // -------------------------------------------------------------------------

    private fun renderNcertStudentSheet(
        context: Context,
        canvas: Canvas,
        config: WorksheetConfig,
        items: List<WorksheetItem>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Decorative NCERT Double Border
        paint.color = NcertPalette.BorderPrimary
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, paint)

        paint.strokeWidth = 0.8f
        paint.color = NcertPalette.BorderSubtle
        canvas.drawRect(MARGIN + 3f, MARGIN + 3f, PAGE_WIDTH - MARGIN - 3f, PAGE_HEIGHT - MARGIN - 3f, paint)

        // 1. Official NCERT CNCL Header
        var currentY = MARGIN + 4f
        currentY = renderNcertHeader(canvas, paint, config, currentY)

        // 2. Student Details Box (4 fields in clean 2x2 grid)
        currentY = renderStudentDetailsBox(canvas, paint, currentY)

        // 3. Bilingual Instruction Banner (निर्देश / ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ)
        currentY = renderInstructionBanner(canvas, paint, config.type, currentY)

        // Calculate layout geometry for content and bottom footers
        val outcomeHeight = 44f
        val rubricHeight = 42f
        val footerAreaHeight = outcomeHeight + rubricHeight + 14f
        val bottomY = PAGE_HEIGHT - MARGIN - footerAreaHeight
        val availableContentHeight = bottomY - currentY

        // 4. Problem Content Area
        when (config.type) {
            WorksheetType.COUNT_AND_MATCH, WorksheetType.PICTURE_WORD_MATCH -> {
                renderTwoColumnMatching(context, canvas, paint, config, items, currentY, availableContentHeight)
            }
            else -> {
                renderSingleColumnProblems(context, canvas, paint, config, items, currentY, availableContentHeight)
            }
        }

        // 5. Signature NCERT Learning Outcome Banner ("हमने सीखा / ᱟᱵᱚ ᱵᱚ ᱪᱮᱫ ᱠᱮᱫᱟ")
        val outcomeY = PAGE_HEIGHT - MARGIN - rubricHeight - outcomeHeight - 8f
        renderLearningOutcomeBanner(canvas, paint, config.type, config.grade, outcomeY, outcomeHeight)

        // 6. Teacher Evaluation Rubric & Signature
        val rubricY = PAGE_HEIGHT - MARGIN - rubricHeight - 4f
        renderTeacherRubric(canvas, paint, rubricY, rubricHeight)

        // Bottom NCERT Copyright / Attribution
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.TextSecondary
        paint.textSize = 7f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("NCERT CNCL • Foundational Learning & Numeracy (FLN) • Bilingual Jharkhand MTB-MLE Edition", MARGIN + 12, PAGE_HEIGHT - MARGIN + 10, paint)
    }

    private fun renderNcertHeader(
        canvas: Canvas,
        paint: Paint,
        config: WorksheetConfig,
        startY: Float
    ): Float {
        val bannerHeight = 54f
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.NavyLight
        canvas.drawRect(MARGIN + 4f, startY, PAGE_WIDTH - MARGIN - 4f, startY + bannerHeight, paint)

        paint.color = NcertPalette.BorderPrimary
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN + 4f, startY + bannerHeight, PAGE_WIDTH - MARGIN - 4f, startY + bannerHeight, paint)

        // Primary Header: NCERT • राष्ट्रीय साक्षरता केंद्र (CNCL) • ᱡᱟᱹᱛᱤᱭᱟᱹᱨᱤ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱛᱟᱞᱢᱟ
        paint.color = NcertPalette.NavyPrimary
        paint.textSize = 12.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NCERT • राष्ट्रीय साक्षरता केंद्र (CNCL) • ᱡᱟᱹᱛᱤᱭᱟᱹᱨᱤ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱛᱟᱞᱢᱟ", MARGIN + 12f, startY + 18f, paint)

        // Subtitle: बुनियादी साक्षरता एवं संख्या-ज्ञान (FLN) • ᱮᱛᱚᱦᱚᱵ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱟᱨ ᱮᱞᱠᱷᱟ-ᱜᱮᱭᱟᱱ
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        paint.color = NcertPalette.TextSecondary
        canvas.drawText("बुनियादी साक्षरता एवं संख्या-ज्ञान (FLN) • ᱮᱛᱚᱦᱚᱵ ᱥᱟᱠᱷᱚᱨᱛᱟ ᱟᱨ ᱮᱞᱠᱷᱟ-ᱜᱮᱭᱟᱱ", MARGIN + 12f, startY + 32f, paint)

        // Domain & Worksheet Number pill on right
        paint.color = NcertPalette.NavyPrimary
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val wsText = "कार्य-पत्रक (Worksheet) 1 • ᱠᱟᱹᱢᱤ-ᱥᱟᱠᱟᱢ ᱑"
        val gradeText = "कक्षा: ${config.grade.hindiName} (${config.grade.displayName})"
        val wsWidth = paint.measureText(wsText)
        canvas.drawText(wsText, PAGE_WIDTH - MARGIN - wsWidth - 12f, startY + 20f, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = NcertPalette.TextSecondary
        val gradeWidth = paint.measureText(gradeText)
        canvas.drawText(gradeText, PAGE_WIDTH - MARGIN - gradeWidth - 12f, startY + 34f, paint)

        return startY + bannerHeight + 6f
    }

    private fun renderStudentDetailsBox(
        canvas: Canvas,
        paint: Paint,
        startY: Float
    ): Float {
        val boxHeight = 36f
        val boxRect = RectF(MARGIN + 6f, startY, PAGE_WIDTH - MARGIN - 6f, startY + boxHeight)

        // Background
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.CardBg
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.color = NcertPalette.BorderSubtle
        paint.strokeWidth = 1f
        canvas.drawRoundRect(boxRect, 6f, 6f, paint)

        // Internal dividing grid lines
        val midX = MARGIN + 6f + (PAGE_WIDTH - (MARGIN * 2) - 12f) * 0.62f
        val midY = startY + (boxHeight / 2f)
        canvas.drawLine(MARGIN + 6f, midY, PAGE_WIDTH - MARGIN - 6f, midY, paint)
        canvas.drawLine(midX, startY, midX, startY + boxHeight, paint)

        // Field texts
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.TextDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT

        // Row 1: Name and Date
        canvas.drawText("शिक्षार्थी का नाम / ᱧᱩᱛᱩᱢ: ________________________", MARGIN + 12f, startY + 12.5f, paint)
        canvas.drawText("दिनांक / ᱢᱟᱹᱦᱤᱛ: _________", midX + 8f, startY + 12.5f, paint)

        // Row 2: Class and Roll No
        canvas.drawText("कक्षा / ᱪᱟᱱᱟᱪ: _______________________________", MARGIN + 12f, midY + 12.5f, paint)
        canvas.drawText("क्रमांक / ᱮᱞ: _________", midX + 8f, midY + 12.5f, paint)

        return startY + boxHeight + 6f
    }

    private fun renderInstructionBanner(
        canvas: Canvas,
        paint: Paint,
        type: WorksheetType,
        startY: Float
    ): Float {
        val bannerHeight = 34f
        val bannerRect = RectF(MARGIN + 6f, startY, PAGE_WIDTH - MARGIN - 6f, startY + bannerHeight)

        // Soft directive fill
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.InstructionBg
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)

        // Directive border
        paint.style = Paint.Style.STROKE
        paint.color = NcertPalette.InstructionBorder
        paint.strokeWidth = 1f
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)

        val (hiInstr, satInstr) = getBilingualInstruction(type)

        // Hindi instruction
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.NavyPrimary
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(hiInstr, MARGIN + 14f, startY + 13f, paint)

        // Santhali instruction
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = NcertPalette.TextSecondary
        canvas.drawText(satInstr, MARGIN + 14f, startY + 27f, paint)

        return startY + bannerHeight + 8f
    }

    // -------------------------------------------------------------------------
    // NCERT Signature Two-Column Matching Layout
    // -------------------------------------------------------------------------

    private fun renderTwoColumnMatching(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        config: WorksheetConfig,
        items: List<WorksheetItem>,
        startY: Float,
        availableHeight: Float
    ) {
        val totalWidth = PAGE_WIDTH - (MARGIN * 2) - 12f
        val gap = 36f
        val colWidth = (totalWidth - gap) / 2f
        val colAX = MARGIN + 6f
        val colBX = colAX + colWidth + gap

        // Column Headers
        val headerY = startY + 12f
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.NavyPrimary
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("स्तंभ 'क' (Column A) • ᱠᱷᱟᱸᱫᱷᱟ 'A'", colAX + 6f, headerY, paint)
        canvas.drawText("स्तंभ 'ख' (Column B) • ᱠᱷᱟᱸᱫᱷᱟ 'B'", colBX + 6f, headerY, paint)

        paint.color = NcertPalette.BorderSubtle
        paint.strokeWidth = 1f
        canvas.drawLine(colAX + 4f, headerY + 4f, colAX + colWidth - 4f, headerY + 4f, paint)
        canvas.drawLine(colBX + 4f, headerY + 4f, colBX + colWidth - 4f, headerY + 4f, paint)

        val rowsStartY = headerY + 10f
        val count = items.size.coerceIn(3, 6)
        val rowHeight = ((availableHeight - 24f) / count).coerceIn(48f, 72f)

        // Generate deterministically shuffled targets for Column B
        val random = Random(config.seed + 99)
        val shuffledIndices = (0 until count).shuffled(random)

        for (i in 0 until count) {
            val item = items[i]
            val rowY = rowsStartY + (i * rowHeight)
            val rowRectA = RectF(colAX, rowY, colAX + colWidth, rowY + rowHeight - 6f)

            // Column A Card Box
            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.CardBg
            canvas.drawRoundRect(rowRectA, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = NcertPalette.CardBorder
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rowRectA, 6f, 6f, paint)

            // Index badge
            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.NavyPrimary
            canvas.drawCircle(colAX + 14f, rowY + (rowHeight - 6f) / 2f, 8f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${i + 1}", colAX + 14f, rowY + (rowHeight - 6f) / 2f + 3f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Column A Concrete Visual Asset / Illustration
            val assetStartX = colAX + 28f
            val assetWidth = colWidth - 52f
            val centerY = rowY + (rowHeight - 6f) / 2f
            val qty = item.quantity.coerceIn(1, 10)
            val bitmap = loadAssetBitmap(context, item.imageAssetPath)

            if (bitmap != null) {
                val iconSize = (rowHeight - 16f).coerceAtMost(22f)
                val perRow = if (qty > 5) 5 else qty
                for (q in 0 until qty) {
                    val r = q / perRow
                    val c = q % perRow
                    val ix = assetStartX + (c * (iconSize + 3f))
                    val iy = if (qty <= 5) centerY - (iconSize / 2f) else (rowY + 6f + (r * (iconSize + 2f)))
                    if (ix + iconSize <= colAX + colWidth - 14f) {
                        canvas.drawBitmap(bitmap, null, RectF(ix, iy, ix + iconSize, iy + iconSize), paint)
                    }
                }
            } else {
                paint.color = NcertPalette.NavyPrimary
                paint.style = Paint.Style.FILL
                for (q in 0 until qty) {
                    val dotX = assetStartX + (q * 16f) + 6f
                    if (dotX <= colAX + colWidth - 14f) {
                        canvas.drawCircle(dotX, centerY, 5f, paint)
                    }
                }
            }

            // Connection Anchor Dot on right edge of Column A
            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.MatchingDot
            val anchorDotAX = colAX + colWidth - 8f
            canvas.drawCircle(anchorDotAX, centerY, 4.5f, paint)

            // -------------------------------------------------------------
            // Column B Target (Shuffled)
            // -------------------------------------------------------------
            val targetIdx = shuffledIndices[i]
            val targetItem = items[targetIdx]
            val rowRectB = RectF(colBX, rowY, colBX + colWidth, rowY + rowHeight - 6f)

            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.CardBg
            canvas.drawRoundRect(rowRectB, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = NcertPalette.CardBorder
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rowRectB, 6f, 6f, paint)

            // Connection Anchor Dot on left edge of Column B
            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.MatchingDot
            val anchorDotBX = colBX + 8f
            canvas.drawCircle(anchorDotBX, centerY, 4.5f, paint)

            // Column B Text Content
            val (hiNumWord, satNumWord) = numberToWordBilingual(targetItem.quantity)
            val labelText = if (config.type == WorksheetType.PICTURE_WORD_MATCH) {
                "${targetItem.rightLabelSantali} • ${targetItem.leftLabelHindi}"
            } else {
                "$hiNumWord • $satNumWord"
            }

            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.TextDark
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(labelText, colBX + 22f, centerY + 3.5f, paint)

            // Right Answer Box [   ] in Column B
            val ansBoxRect = RectF(colBX + colWidth - 28f, centerY - 10f, colBX + colWidth - 6f, centerY + 10f)
            paint.style = Paint.Style.STROKE
            paint.color = NcertPalette.BorderSubtle
            paint.strokeWidth = 1f
            canvas.drawRoundRect(ansBoxRect, 3f, 3f, paint)
        }
    }

    // -------------------------------------------------------------------------
    // Single Column Problems (Addition, Subtraction, Train, Tracing)
    // -------------------------------------------------------------------------

    private fun renderSingleColumnProblems(
        context: Context,
        canvas: Canvas,
        paint: Paint,
        config: WorksheetConfig,
        items: List<WorksheetItem>,
        startY: Float,
        availableHeight: Float
    ) {
        val count = items.size.coerceIn(3, 6)
        val rowHeight = (availableHeight / count).coerceIn(48f, 76f)
        val w = PAGE_WIDTH - (MARGIN * 2) - 12f
        val x = MARGIN + 6f

        items.take(count).forEachIndexed { index, item ->
            val rowY = startY + (index * rowHeight)
            val boxRect = RectF(x, rowY, x + w, rowY + rowHeight - 6f)

            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.CardBg
            canvas.drawRoundRect(boxRect, 6f, 6f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = NcertPalette.CardBorder
            paint.strokeWidth = 1f
            canvas.drawRoundRect(boxRect, 6f, 6f, paint)

            // Problem Index Badge
            paint.style = Paint.Style.FILL
            paint.color = NcertPalette.NavyPrimary
            canvas.drawCircle(x + 14f, rowY + 15f, 8f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${index + 1}", x + 14f, rowY + 18f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Bilingual Sub-prompt
            paint.color = NcertPalette.NavyPrimary
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(item.promptHindi, x + 28f, rowY + 13f, paint)

            paint.color = NcertPalette.TextSecondary
            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            if (item.promptSantali.isNotBlank()) {
                canvas.drawText(item.promptSantali, x + 28f, rowY + 23f, paint)
            }

            // Visual elements & answer box
            val contentY = rowY + 28f
            when (config.type) {
                WorksheetType.ADDITION_WORD_PROBLEM -> {
                    val bitmap = loadAssetBitmap(context, item.imageAssetPath)
                    val iconSize = 16f
                    for (i in 0 until item.quantity) {
                        val ix = x + 30f + (i * 18f)
                        if (bitmap != null) canvas.drawBitmap(bitmap, null, RectF(ix, contentY, ix + iconSize, contentY + iconSize), paint)
                        else canvas.drawCircle(ix + 8f, contentY + 8f, 5f, paint)
                    }
                    val plusX = x + 30f + (item.quantity * 18f) + 8f
                    paint.color = NcertPalette.NavyPrimary
                    paint.textSize = 12f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("+", plusX, contentY + 12f, paint)

                    for (i in 0 until item.secondaryQuantity) {
                        val ix = plusX + 16f + (i * 18f)
                        if (bitmap != null) canvas.drawBitmap(bitmap, null, RectF(ix, contentY, ix + iconSize, contentY + iconSize), paint)
                        else canvas.drawCircle(ix + 8f, contentY + 8f, 5f, paint)
                    }
                    val eqX = plusX + 16f + (item.secondaryQuantity * 18f) + 12f
                    canvas.drawText("=  [ _____ ]", eqX, contentY + 12f, paint)
                }
                WorksheetType.SUBTRACTION_PROBLEM -> {
                    val bitmap = loadAssetBitmap(context, item.imageAssetPath)
                    val iconSize = 16f
                    for (i in 0 until item.quantity) {
                        val ix = x + 30f + (i * 18f)
                        if (bitmap != null) canvas.drawBitmap(bitmap, null, RectF(ix, contentY, ix + iconSize, contentY + iconSize), paint)
                        else canvas.drawCircle(ix + 8f, contentY + 8f, 5f, paint)

                        if (i >= item.quantity - item.secondaryQuantity) {
                            paint.style = Paint.Style.STROKE
                            paint.color = Color.rgb(220, 38, 38)
                            paint.strokeWidth = 1.6f
                            canvas.drawLine(ix, contentY + iconSize, ix + iconSize, contentY, paint)
                            paint.style = Paint.Style.FILL
                        }
                    }
                    val eqX = x + 30f + (item.quantity * 18f) + 12f
                    paint.color = NcertPalette.NavyPrimary
                    paint.textSize = 11f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("- ${item.secondaryQuantity} =  [ _____ ]", eqX, contentY + 12f, paint)
                }
                WorksheetType.NUMBER_SEQUENCE_TRAIN -> {
                    val boxW = (w - 60f) / item.sequenceItems.size.coerceAtLeast(1)
                    item.sequenceItems.forEachIndexed { seqIdx, seqVal ->
                        val bx = x + 30f + (seqIdx * boxW)
                        val isMissing = seqIdx == item.missingSequenceIndex
                        val sRect = RectF(bx, contentY - 2f, bx + boxW - 6f, contentY + 18f)

                        paint.style = Paint.Style.FILL
                        paint.color = if (isMissing) Color.rgb(238, 244, 255) else Color.rgb(250, 252, 255)
                        canvas.drawRoundRect(sRect, 4f, 4f, paint)

                        paint.style = Paint.Style.STROKE
                        paint.color = if (isMissing) NcertPalette.NavyPrimary else NcertPalette.BorderSubtle
                        canvas.drawRoundRect(sRect, 4f, 4f, paint)

                        paint.style = Paint.Style.FILL
                        paint.color = if (isMissing) NcertPalette.NavyPrimary else NcertPalette.TextDark
                        paint.textSize = 9.5f
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText(if (isMissing) "?" else seqVal, bx + (boxW - 6f) / 2f, contentY + 11f, paint)
                        paint.textAlign = Paint.Align.LEFT
                    }
                }
                WorksheetType.AKSHAR_TRACING -> {
                    paint.color = NcertPalette.NavyPrimary
                    paint.textSize = 18f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(item.leftLabelHindi, x + 30f, contentY + 14f, paint)

                    paint.color = NcertPalette.TextSecondary
                    paint.textSize = 10f
                    paint.typeface = Typeface.DEFAULT
                    canvas.drawText("Practice: . . .   . . .   . . .", x + 70f, contentY + 12f, paint)
                    canvas.drawText("[ ${item.rightLabelSantali} ]", x + w - 140f, contentY + 12f, paint)
                }
                else -> {
                    paint.color = NcertPalette.NavyPrimary
                    paint.textSize = 11f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("Answer: [ _____ ]", x + w - 120f, contentY + 10f, paint)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Signature NCERT Learning Outcome Banner ("हमने सीखा / ᱟᱵᱚ ᱵᱚ ᱪᱮᱫ ᱠᱮᱫᱟ")
    // -------------------------------------------------------------------------

    private fun renderLearningOutcomeBanner(
        canvas: Canvas,
        paint: Paint,
        type: WorksheetType,
        grade: FlnGrade,
        startY: Float,
        height: Float
    ) {
        val rect = RectF(MARGIN + 6f, startY, PAGE_WIDTH - MARGIN - 6f, startY + height)

        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.OutcomeBg
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = NcertPalette.OutcomeBorder
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        val (hiOutcome, satOutcome) = getBilingualLearningOutcome(type, grade)

        // Title
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.OutcomeText
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("★ हमने सीखा (Learning Outcome) • ᱟᱵᱚ ᱵᱚ ᱪᱮᱫ ᱠᱮᱫᱟ:", MARGIN + 14f, startY + 13f, paint)

        // Details
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        paint.color = NcertPalette.TextDark
        canvas.drawText(hiOutcome, MARGIN + 14f, startY + 25f, paint)

        paint.textSize = 7.5f
        paint.color = NcertPalette.TextSecondary
        canvas.drawText(satOutcome, MARGIN + 14f, startY + 36f, paint)
    }

    // -------------------------------------------------------------------------
    // Teacher Rubric & Signature
    // -------------------------------------------------------------------------

    private fun renderTeacherRubric(
        canvas: Canvas,
        paint: Paint,
        startY: Float,
        height: Float
    ) {
        val rect = RectF(MARGIN + 6f, startY, PAGE_WIDTH - MARGIN - 6f, startY + height)

        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.RubricBg
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = NcertPalette.RubricBorder
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.RubricText
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("शिक्षक मूल्यांकन (Teacher Evaluation): ★ ★ ★ ★ ★", MARGIN + 14f, startY + 15f, paint)

        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        paint.color = NcertPalette.TextDark
        canvas.drawText("[  ] आरम्भिक (Emerging)    [  ] प्रगतिशील (Developing)    [  ] दक्ष (Proficient)", MARGIN + 14f, startY + 29f, paint)

        val sigText = "हस्ताक्षर (Teacher Signature): ____________________"
        paint.textSize = 8f
        val sigWidth = paint.measureText(sigText)
        canvas.drawText(sigText, PAGE_WIDTH - MARGIN - sigWidth - 14f, startY + 22f, paint)
    }

    // -------------------------------------------------------------------------
    // Page 2: Teacher Answer Key & Phonics Solutions
    // -------------------------------------------------------------------------

    private fun renderTeacherKeySheet(canvas: Canvas, config: WorksheetConfig, items: List<WorksheetItem>) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Border
        paint.color = NcertPalette.TeacherBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, paint)

        // Header Banner
        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.TeacherHeaderBg
        canvas.drawRect(MARGIN + 2, MARGIN + 2, PAGE_WIDTH - MARGIN - 2, MARGIN + 60, paint)

        // Header Title
        paint.color = NcertPalette.TeacherBorder
        paint.textSize = 13.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NCERT CNCL • TEACHER ASSESSMENT & PHONICS KEY", MARGIN + 14, MARGIN + 26, paint)

        paint.color = NcertPalette.TextSecondary
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("NIPUN Bharat Foundational Literacy & Numeracy • Verified Step-by-Step Solutions", MARGIN + 14, MARGIN + 44, paint)

        // Solutions Grid
        val startY = MARGIN + 74
        items.forEachIndexed { idx, item ->
            val rowY = startY + (idx * 52)
            if (rowY + 48 < PAGE_HEIGHT - 170) {
                renderTeacherSolutionRow(canvas, paint, idx + 1, item, MARGIN + 10, rowY, PAGE_WIDTH - (MARGIN * 2) - 20)
            }
        }

        // Bottom: NIPUN Rubric Box
        val rubricY = PAGE_HEIGHT - MARGIN - 140
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(250, 250, 252)
        val rRect = RectF(MARGIN + 10, rubricY, PAGE_WIDTH - MARGIN - 10, PAGE_HEIGHT - MARGIN - 20)
        canvas.drawRoundRect(rRect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(210, 215, 225)
        canvas.drawRoundRect(rRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.NavyPrimary
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NIPUN Assessment Competency Levels:", MARGIN + 20, rubricY + 18, paint)

        paint.color = Color.rgb(60, 70, 90)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• Emerging (आरंभिक): Needs assistance to identify Ol Chiki letter sounds or count.", MARGIN + 20, rubricY + 38, paint)
        canvas.drawText("• Developing (प्रगतिशील): Identifies with minor teacher prompting in home language.", MARGIN + 20, rubricY + 56, paint)
        canvas.drawText("• Proficient (दक्ष): Independently reads and counts in Santali L1 and explains in Hindi L2.", MARGIN + 20, rubricY + 74, paint)

        // Footer
        paint.color = Color.rgb(140, 130, 110)
        paint.textSize = 8f
        canvas.drawText("Page 2: Teacher Reference & Scoring Guide • TribeTalk NCERT CNCL Suite", MARGIN + 14, PAGE_HEIGHT - MARGIN - 6, paint)
    }

    private fun renderTeacherSolutionRow(
        canvas: Canvas,
        paint: Paint,
        num: Int,
        item: WorksheetItem,
        x: Float,
        y: Float,
        w: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 253, 250)
        val rect = RectF(x, y, x + w, y + 46)
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(240, 225, 200)
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.color = NcertPalette.TeacherBorder
        canvas.drawCircle(x + 16, y + 16, 9f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$num", x + 16, y + 19, paint)
        paint.textAlign = Paint.Align.LEFT

        paint.color = Color.rgb(40, 45, 55)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Answer: ${item.teacherSolutionNote}", x + 34, y + 18, paint)

        paint.color = NcertPalette.TeacherBorder
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Phonics Guide: ${item.teacherPhoneticAnswer}", x + 34, y + 34, paint)
    }

    // -------------------------------------------------------------------------
    // Helper Text & Asset Resolvers
    // -------------------------------------------------------------------------

    private fun numberToWordBilingual(num: Int): Pair<String, String> {
        return when (num) {
            1 -> "एक (1)" to "ᱢᱤᱫ (᱑)"
            2 -> "दो (2)" to "ᱵᱟᱨ (᱒)"
            3 -> "तीन (3)" to "ᱯᱮ (᱓)"
            4 -> "चार (4)" to "ᱯᱩᱱ (᱔)"
            5 -> "पाँच (5)" to "ᱢᱚᱬᱮ (᱕)"
            6 -> "छह (6)" to "ᱛᱩᱨᱩᱭ (᱖)"
            7 -> "सात (7)" to "ᱮᱭᱟᱭ (᱗)"
            8 -> "आठ (8)" to "ᱤᱨᱟᱹᱞ (᱘)"
            9 -> "नौ (9)" to "ᱟᱨᱮ (᱙)"
            10 -> "दस (10)" to "ᱜᱮᱞ (᱑᱐)"
            11 -> "ग्यारह (11)" to "ᱜᱮᱞ ᱢᱤᱫ (᱑᱑)"
            12 -> "बारह (12)" to "ᱜᱮᱞ ᱵᱟᱨ (᱑᱒)"
            else -> "$num" to FlnCurriculumRepository.toOlChikiDigits(num)
        }
    }

    fun getBilingualInstruction(type: WorksheetType): Pair<String, String> {
        return when (type) {
            WorksheetType.COUNT_AND_MATCH ->
                "निर्देश: स्तंभ (क) के चित्रों को गिनें और स्तंभ (ख) की सही संख्या से रेखा खींचकर मिलाएँ।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱠᱷᱟᱸᱫᱷᱟ (A) ᱨᱮᱱᱟᱜ ᱪᱤᱛᱟᱹᱨ ᱠᱚ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱠᱷᱟᱸᱫᱷᱟ (B) ᱨᱮ ᱥᱟᱹᱦᱤ ᱮᱞ ᱥᱟᱞᱟᱜ ᱜᱟᱨ ᱴᱟᱱᱟᱣ ᱠᱟᱛᱮ ᱡᱚᱲᱟᱣ ᱢᱮ ᱾"
            WorksheetType.PICTURE_WORD_MATCH ->
                "निर्देश: स्तंभ (क) के चित्रों को पहचानें और स्तंभ (ख) के संताली (ओल चिकी) शब्दों से मिलाएँ।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱠᱷᱟᱸᱫᱷᱟ (A) ᱨᱮᱱᱟᱜ ᱪᱤᱛᱟᱹᱨ ᱧᱮᱞ ᱠᱟᱛᱮ ᱠᱷᱟᱸᱫᱷᱟ (B) ᱨᱮᱱᱟᱜ ᱥᱟᱱᱛᱟᱲᱤ (ᱚᱞ ᱪᱤᱠᱤ) ᱟᱹᱲᱟᱹ ᱥᱟᱞᱟᱜ ᱡᱚᱲᱟᱣ ᱢᱮ ᱾"
            WorksheetType.ADDITION_WORD_PROBLEM ->
                "निर्देश: चित्रों को गिनकर जोड़ें और दिए गए कोष्ठक [   ] में सही कुल संख्या लिखें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱪᱤᱛᱟᱹᱨ ᱠᱚ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱡᱚᱲᱟᱣ ᱢᱮ ᱟᱨ ᱠᱷᱟᱸᱪᱟ [   ] ᱨᱮ ᱢᱩᱴᱷ ᱮᱞ ᱚᱞ ᱢᱮ ᱾"
            WorksheetType.SUBTRACTION_PROBLEM ->
                "निर्देश: कटे हुए चित्रों को घटाएँ और बचे हुए गिनकर दिए गए कोष्ठक [   ] में लिखें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ゲᱫ ᱟᱠᱟᱱ ᱪᱤᱛᱟᱹᱨ ᱠᱚ ᱵᱷᱮᱜᱟᱨ ᱢᱮ ᱟᱨ ᱥᱟᱨᱮᱡ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱠᱷᱟᱸᱪᱟ [   ] ᱨᱮ ᱚᱞ ᱢᱮ ᱾"
            WorksheetType.MULTIPLICATION_GROUPS ->
                "निर्देश: प्रत्येक समूह की वस्तुओं को गिनें और गुणा करके कुल संख्या लिखें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱢᱤᱫ ᱢᱤᱫ ᱫᱚᱞ ᱨᱮᱱᱟᱜ ᱡᱤᱱᱤᱥ ᱞᱮᱠᱷᱟᱭ ᱢᱮ ᱟᱨ ᱜᱩᱬᱟᱹ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱮᱞ ᱚᱞ ᱢᱮ ᱾"
            WorksheetType.NUMBER_SEQUENCE_TRAIN ->
                "निर्देश: रेलगाड़ी के डिब्बों में क्रम से छूटी हुई संख्याएँ पहचानकर लिखें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱨᱮᱞᱜᱟᱹᱰᱤ ᱨᱮ ᱞᱮᱛᱟᱲ ᱞᱮᱠᱷᱟ ᱨᱮ ᱟᱫ ᱟᱠᱟᱱ ᱮᱞ ᱠᱚ ᱚᱞ ᱢᱮ ᱾"
            WorksheetType.AKSHAR_TRACING ->
                "निर्देश: दिए गए ओल चिकी वर्णों को देखें, बोलें और बिन्दुओं पर सुंदर हस्तलेखन करें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱮᱢ ᱟᱠᱟᱱ ᱚᱞ ᱪᱤᱠᱤ ᱪᱤᱠᱤ ᱠᱚ ᱧᱮᱞ ᱢᱮ, ᱨᱚᱲ ᱢᱮ ᱟᱨ ᱴᱩᱰᱟᱹᱜ ᱪᱮᱛᱟᱱ ᱨᱮ ᱚᱞ ᱢᱮ ᱾"
            WorksheetType.MISSING_AKSHAR_SPELLING ->
                "निर्देश: दिए गए विकल्पों में से सही वर्ण चुनकर शब्द को पूरा करें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱮᱢ ᱟᱠᱟᱱ ᱵᱟᱪᱷᱟᱣ ᱠᱷᱚᱱ ᱴᱷᱤᱠ ᱪᱤᱠᱤ ᱵᱟᱪᱷᱟᱣ ᱠᱟᱛᱮ ᱟᱹᱲᱟᱹ ᱯᱮᱨᱮᱡ ᱢᱮ ᱾"
            WorksheetType.MONEY_COUNTING ->
                "निर्देश: सिक्कों के मूल्यों को जोड़ें और कुल रुपये [ ₹ _____ ] में लिखें।" to
                        "ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ: ᱥᱤᱠᱠᱟ ᱨᱮᱱᱟᱜ ᱫᱟᱢ ᱡᱚᱲ ᱠᱟᱛᱮ ᱢᱩᱴᱷ ᱴᱟᱠᱟ [ ₹ _____ ] ᱨᱮ ᱚᱞ ᱢᱮ ᱾"
        }
    }

    fun getBilingualLearningOutcome(type: WorksheetType, grade: FlnGrade): Pair<String, String> {
        return when (type) {
            WorksheetType.COUNT_AND_MATCH ->
                "वस्तुओं को गिनना, उनके संख्या-प्रतीकों की पहचान करना और संताली (ओल चिकी) व हिन्दी में मिलान करना।" to
                        "ᱡᱤᱱᱤᱥ ᱠᱚ ᱞᱮᱠᱷᱟ, ᱩᱱᱠᱩᱣᱟᱜ ᱮᱞ-ᱪᱤᱱᱦᱟᱹ ᱪᱤᱱᱦᱟᱹᱣ ᱟᱨ ᱥᱟᱱᱛᱟᱲᱤ (ᱚᱞ ᱪᱤᱠᱤ) ᱟᱨ ᱦᱤᱱᱫᱤ ᱛᱮ ᱡᱚᱲᱟᱣ ᱾"
            WorksheetType.PICTURE_WORD_MATCH ->
                "चित्रों की पहचान, ध्वनियों का उच्चारण और संताली (ओल चिकी) दैनिक शब्दावली का ज्ञान प्राप्त करना।" to
                        "ᱪᱤᱛᱟᱹᱨ ᱪᱤᱱᱦᱟᱹᱣ, ᱟᱲᱟᱝ ᱨᱚᱲ ᱟᱨ ᱥᱟᱱᱛᱟᱲᱤ (ᱚᱞ ᱪᱤᱠᱤ) ᱫᱤᱱᱟᱹᱢ ᱟᱹᱲᱟᱹ ᱵᱟᱰᱟᱭ ᱧᱟᱢ ᱾"
            WorksheetType.ADDITION_WORD_PROBLEM ->
                "मूर्त वस्तुओं के माध्यम से एक-अंकीय जोड़ की संक्रिया समझना और दैनिक जीवन में उसका अनुप्रयोग।" to
                        "ᱡᱤᱱᱤᱥ ᱠᱚ ᱥᱟᱶ ᱢᱤᱫ-ᱮᱞ ᱡᱚᱲ ᱵᱩঝᱟᱹᱣ ᱟᱨ ᱫᱤᱱᱟᱹᱢ ᱡᱤᱭᱚᱱ ᱨᱮ ᱠᱟᱹᱢᱤ ᱨᱮ ᱞᱟᱜᱟᱣ ᱾"
            WorksheetType.SUBTRACTION_PROBLEM ->
                "मूर्त वस्तुओं से घटाव की अवधारणा (कम होना/शेष बचना) को समझना और परिणाम व्यक्त करना।" to
                        "ᱡᱤᱱᱤᱥ ᱠᱚ ᱠᱷᱚᱱ ᱵᱷᱮᱜᱟᱨ (ᱠᱚᱢᱚᱜ/ᱥᱟᱨᱮᱡᱚᱜ) ᱨᱮᱱᱟᱜ ᱵᱩঝᱟᱹᱣ ᱟᱨ ᱚᱨᱡᱚ ᱚᱞ ᱾"
            WorksheetType.MULTIPLICATION_GROUPS ->
                "समान समूहों के माध्यम से बार-बार जोड़ने और गुणा की प्रारम्भिक समझ विकसित करना।" to
                        "ᱥᱚᱢᱟᱱ ᱫᱚᱞ ᱥᱟᱶ ᱞᱮᱛᱟᱲ ᱡᱚᱲ ᱟᱨ ᱜᱩᱬᱟᱹ ᱨᱮᱱᱟᱜ ᱮᱛᱚᱦᱚᱵ ᱵᱩঝᱟᱹᱣ ᱾"
            WorksheetType.NUMBER_SEQUENCE_TRAIN ->
                "संख्याओं के आरोही क्रम की समझ और छूटे हुए अंकों को सही स्थान पर पहचानने की क्षमता।" to
                        "ᱮᱞ ᱨᱮᱱᱟᱜ ᱞᱟᱦᱟᱱᱛᱤ ᱠᱨᱚᱢ ᱵᱩঝᱟᱹᱣ ᱟᱨ ᱟᱫ ᱟᱠᱟᱱ ᱮᱞ ᱴᱷᱤᱠ ᱴᱷᱟᱶ ᱨᱮ ᱪᱤᱱᱦᱟᱹᱣ ᱾"
            WorksheetType.AKSHAR_TRACING ->
                "ओल चिकी वर्णों की सही बनावट, दिशा-बोध तथा उनके ध्वनि-प्रतीकों का शुद्ध उच्चारण।" to
                        "ᱚᱞ ᱪᱤᱠᱤ ᱪᱤᱠᱤ ᱨᱮᱱᱟᱜ ᱥᱟᱹᱦᱤ ᱜᱚᱲᱦᱚᱱ, ᱫᱤᱥᱟᱹ-ᱩᱫᱩᱜ ᱟᱨ ᱥᱟᱹᱦᱤ ᱟᱲᱟᱝ ᱨᱚᱲ ᱾"
            WorksheetType.MISSING_AKSHAR_SPELLING ->
                "शब्दों में सही वर्ण की पहचान कर रिक्त स्थान भरना और ओल चिकी वर्तनी दक्षता प्राप्त करना।" to
                        "ᱟᱹᱲᱟᱹ ᱨᱮ ᱴᱷᱤᱠ ᱪᱤᱠᱤ ᱪᱤᱱᱦᱟᱹᱣ ᱠᱟᱛᱮ ᱯᱮᱨᱮᱡ ᱟᱨ ᱚᱞ ᱪᱤᱠᱤ ᱵᱟᱱᱟᱱ ᱪᱮᱫᱚᱜ ᱾"
            WorksheetType.MONEY_COUNTING ->
                "भारतीय मुद्रा (रुपये और पैसे) के सिक्कों की पहचान और साधारण दैनिक क्रय-विक्रय की गणना।" to
                        "ᱥᱤᱧᱚᱛ ᱴᱟᱠᱟ-ᱯᱩᱭᱥᱟᱹ ᱪᱤᱱᱦᱟᱹᱣ ᱟᱨ ᱫᱤᱱᱟᱹᱢ ᱦᱟᱴ-ᱵᱟᱡᱟᱨ ᱨᱮ ᱞᱮᱠᱷᱟ-ᱡᱚᱠᱷᱟ ᱾"
        }
    }

    private fun loadAssetBitmap(context: Context, assetPath: String?): Bitmap? {
        if (assetPath.isNullOrBlank()) return null
        val clean = assetPath.removePrefix("assets/").trim()

        val resolvedPath = if (clean.endsWith(".svg", ignoreCase = true)) {
            val key = clean.substringAfterLast("/").substringBefore(".svg")
            when (key) {
                "mango" -> "flashcards/fr_01.webp"
                "banana" -> "flashcards/fr_03.webp"
                "apple" -> "flashcards/fr_04.webp"
                "sal_leaf" -> "flashcards/na_01.webp"
                "clay_pot" -> "flashcards/sf_06.webp"
                "tumdak_drum" -> "flashcards/sf_03.webp"
                "straw_hut" -> "flashcards/sf_01.webp"
                "sickle" -> "flashcards/sf_12.webp"
                "fish" -> "flashcards/an_14.webp"
                "peacock" -> "flashcards/an_16.webp"
                "cow" -> "flashcards/an_02.webp"
                "elephant" -> "flashcards/an_05.webp"
                "dog" -> "flashcards/an_01.webp"
                else -> clean
            }
        } else clean

        if (resolvedPath.endsWith(".webp", ignoreCase = true)) {
            val webp = org.tribetalk.fln.image.FlnImageLoader.loadAndroidBitmap(context, resolvedPath)
            if (webp != null) return webp
        }
        return org.tribetalk.fln.image.FlnImageLoader.loadAndroidBitmap(context, clean)
    }
}
