package com.alchemists.tribetalk.worksheet

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local Native PDF Exporter for TribeTalk Bilingual Worksheets.
 *
 * Generates standard A4 printable PDF documents containing curriculum-aligned
 * Hindi + Santali bilingual activities using android.graphics.pdf.PdfDocument.
 * No external third-party PDF dependencies are required.
 */
class WorksheetPdfExporter(private val context: Context) {

    companion object {
        const val PAGE_WIDTH = 595   // Standard A4 width in points (72 DPI)
        const val PAGE_HEIGHT = 842  // Standard A4 height in points
        const val MARGIN_LEFT = 40f
        const val MARGIN_RIGHT = 555f
        const val MARGIN_TOP = 36f
        const val MARGIN_BOTTOM = 806f
        const val CONTENT_WIDTH = 515f // MARGIN_RIGHT - MARGIN_LEFT
    }

    /**
     * Generates a printable A4 PDF file from a Worksheet data model.
     */
    fun exportPdf(worksheet: Worksheet): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dottedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            style = Paint.Style.STROKE
            strokeWidth = 1.0f
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // Draw First Page Header
        var currentY = drawHeader(canvas, paint, worksheet, isFirstPage = true)

        // Draw Questions
        for ((index, question) in worksheet.questions.withIndex()) {
            val estimatedHeight = estimateQuestionHeight(question)
            if (currentY + estimatedHeight > MARGIN_BOTTOM - 40f) {
                // Finish current page
                drawFooter(canvas, paint, pageNumber)
                pdfDocument.finishPage(page)

                // Start new page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = drawHeader(canvas, paint, worksheet, isFirstPage = false)
            }

            currentY = drawQuestionCard(
                canvas = canvas,
                paint = paint,
                dottedPaint = dottedPaint,
                borderPaint = borderPaint,
                questionNumber = index + 1,
                question = question,
                startY = currentY
            )
            currentY += 12f // gap between questions
        }

        // Finish last page
        drawFooter(canvas, paint, pageNumber)
        pdfDocument.finishPage(page)

        // Clean topic for filename
        val safeTopic = worksheet.topic
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
            .ifBlank { "Bilingual" }
            .take(30)
        val filename = "TribeTalk_${safeTopic}_Worksheet.pdf"

        val outputDir = File(context.filesDir, "worksheets").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, filename)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawHeader(
        canvas: Canvas,
        paint: Paint,
        worksheet: Worksheet,
        isFirstPage: Boolean
    ): Float {
        var currentY = MARGIN_TOP

        // Top Brand Banner
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#9B3F4A") // TribeTalk Terracotta Primary
        canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 54f, 6f, 6f, paint)

        // Banner Text
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("TribeTalk • ${worksheet.title}", MARGIN_LEFT + 14f, currentY + 24f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        val gradeText = worksheet.grade?.let { " • Grade: $it" } ?: ""
        canvas.drawText("Topic: ${worksheet.topic}$gradeText • NIPUN Bharat FLN Bilingual Pedagogy", MARGIN_LEFT + 14f, currentY + 42f, paint)

        currentY += 66f

        if (isFirstPage) {
            // Student Info Header Box
            paint.color = Color.parseColor("#F8FAFC")
            canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 44f, 4f, 4f, paint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 44f, 4f, 4f, borderPaint)

            paint.color = Color.parseColor("#334155")
            paint.textSize = 10f
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(worksheet.createdAt))
            canvas.drawText("Student Name (विद्यार्थी का नाम): __________________________", MARGIN_LEFT + 12f, currentY + 18f, paint)
            canvas.drawText("Date: $dateStr", MARGIN_RIGHT - 130f, currentY + 18f, paint)
            canvas.drawText("Class / Section (कक्षा): ________________", MARGIN_LEFT + 12f, currentY + 34f, paint)
            canvas.drawText("Roll No: ___________", MARGIN_RIGHT - 130f, currentY + 34f, paint)

            currentY += 54f

            // Instructions line
            if (worksheet.instructions.isNotBlank()) {
                paint.color = Color.parseColor("#64748B")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("निर्देश: ${worksheet.instructions}", MARGIN_LEFT + 4f, currentY, paint)
                currentY += 16f
            }
        }

        return currentY
    }

    private fun drawQuestionCard(
        canvas: Canvas,
        paint: Paint,
        dottedPaint: Paint,
        borderPaint: Paint,
        questionNumber: Int,
        question: WorksheetQuestion,
        startY: Float
    ): Float {
        var currentY = startY

        // Background Box for question
        val height = estimateQuestionHeight(question)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#FAFAFA")
        canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + height, 6f, 6f, paint)
        canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + height, 6f, 6f, borderPaint)

        // Question Type & Number Badge
        paint.color = Color.parseColor("#C95C5C") // Coral Secondary
        canvas.drawRoundRect(MARGIN_LEFT + 10f, currentY + 8f, MARGIN_LEFT + 140f, currentY + 24f, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("Q$questionNumber • ${question.type.name.replace('_', ' ')}", MARGIN_LEFT + 16f, currentY + 19f, paint)

        currentY += 34f

        // Hindi Text Block
        paint.color = Color.parseColor("#1E293B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11.5f
        val hindiLines = wrapText(question.hindiText, paint, CONTENT_WIDTH - 28f)
        for (line in hindiLines) {
            canvas.drawText("हिन्दी: $line", MARGIN_LEFT + 14f, currentY, paint)
            currentY += 15f
        }

        // Santali Text Block
        paint.color = Color.parseColor("#2F8F83") // Success / Educational Teal
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        val santaliLines = wrapText(question.santaliText, paint, CONTENT_WIDTH - 28f)
        for (line in santaliLines) {
            canvas.drawText("ᱥᱟᱱᱛᱟᱲᱤ: $line", MARGIN_LEFT + 14f, currentY, paint)
            currentY += 15f
        }

        // Options (for Multiple Choice)
        if (question.type == QuestionType.MULTIPLE_CHOICE && question.options.isNotEmpty()) {
            currentY += 2f
            paint.color = Color.parseColor("#334155")
            paint.textSize = 10f
            for (opt in question.options) {
                canvas.drawText("   [  ] $opt", MARGIN_LEFT + 18f, currentY, paint)
                currentY += 14f
            }
        }

        // Answer Line / Blank Space
        currentY += 4f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("उत्तर / Answer:", MARGIN_LEFT + 14f, currentY + 8f, paint)
        canvas.drawLine(MARGIN_LEFT + 80f, currentY + 8f, MARGIN_RIGHT - 14f, currentY + 8f, dottedPaint)

        return startY + height
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageNumber: Int) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#94A3B8")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_LEFT, MARGIN_BOTTOM, MARGIN_RIGHT, MARGIN_BOTTOM, dividerPaint)

        canvas.drawText("TribeTalk Offline Bilingual Worksheet • NIPUN Bharat Foundational Learning", MARGIN_LEFT, MARGIN_BOTTOM + 16f, paint)
        canvas.drawText("Page $pageNumber", MARGIN_RIGHT - 45f, MARGIN_BOTTOM + 16f, paint)
    }

    private fun estimateQuestionHeight(question: WorksheetQuestion): Float {
        var base = 65f
        if (question.hindiText.length > 50) base += 16f
        if (question.santaliText.length > 50) base += 16f
        if (question.type == QuestionType.MULTIPLE_CHOICE) {
            base += (question.options.size * 14f) + 8f
        }
        if (question.type == QuestionType.MATCHING) {
            base += 20f
        }
        return base
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = StringBuilder(candidate)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    /**
     * Opens the generated PDF in the device's default PDF viewer.
     */
    fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer app found on device.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares the generated PDF via Android Share Sheet.
     */
    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                putExtra(Intent.EXTRA_TEXT, "Here is the auto-generated TribeTalk bilingual worksheet: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Bilingual Worksheet").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sends the PDF to Android Print Framework for printing or saving.
     */
    fun printPdf(file: File) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = object : android.print.PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = android.print.PrintDocumentInfo.Builder(file.name)
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val input = file.inputStream()
                            val output = FileOutputStream(destination?.fileDescriptor)
                            input.copyTo(output)
                            input.close()
                            output.close()
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print("TribeTalk_${file.name}", printAdapter, PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "Print service not available.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error printing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
