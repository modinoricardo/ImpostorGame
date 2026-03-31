package com.ricardomodino.impostorgame.extensions

import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.TextPaint
import android.util.TypedValue
import android.widget.TextView
import androidx.core.widget.TextViewCompat

private const val MIN_TEXT_SIZE_HARD_SP = 6f
private const val TEXT_SIZE_STEP_SP = 1f

private data class WrappedTextResult(
    val text: String,
    val textSp: Float,
    val lineCount: Int
)

fun TextView.applyWordSafeText(
    rawText: String,
    preferSingleLine: Boolean,
    maxTextSp: Float,
    minTextSp: Float,
    preferredSingleLineMinSp: Float = minTextSp,
    preferredWrappedMaxLines: Int = Int.MAX_VALUE,
    absoluteMinTextSp: Float = MIN_TEXT_SIZE_HARD_SP
) {
    val source = rawText.replace("\r\n", "\n").trim()
    if (source.isEmpty()) {
        text = ""
        return
    }

    val paragraphs = normalizeParagraphs(source)

    includeFontPadding = false
    TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
    }

    val resolveText = {
        resolveWordSafeText(
            paragraphs = paragraphs,
            preferSingleLine = preferSingleLine,
            maxTextSp = maxTextSp,
            minTextSp = minTextSp,
            preferredSingleLineMinSp = preferredSingleLineMinSp,
            preferredWrappedMaxLines = preferredWrappedMaxLines,
            absoluteMinTextSp = absoluteMinTextSp
        )
    }

    if (width - paddingLeft - paddingRight > 0) resolveText()
    else post { resolveText() }
}

private fun TextView.applyWrappedResult(result: WrappedTextResult) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, result.textSp)
    setSingleLine(false)
    maxLines = Int.MAX_VALUE
    text = result.text
}

private fun TextView.resolveWordSafeText(
    paragraphs: List<String>,
    preferSingleLine: Boolean,
    maxTextSp: Float,
    minTextSp: Float,
    preferredSingleLineMinSp: Float,
    preferredWrappedMaxLines: Int,
    absoluteMinTextSp: Float
) {
    val availableWidth = width - paddingLeft - paddingRight
    if (availableWidth <= 0) {
        text = paragraphs.joinToString("\n\n")
        return
    }

    val hardMin = absoluteMinTextSp.coerceAtMost(maxTextSp)
    val softMin = minTextSp.coerceIn(hardMin, maxTextSp)
    val singleLineSoftMin = preferredSingleLineMinSp.coerceIn(hardMin, maxTextSp)
    val hasSpaces = paragraphs.size == 1 && paragraphs.first().contains(' ')

    if (preferSingleLine && paragraphs.size == 1) {
        val singleLineMin = if (hasSpaces) singleLineSoftMin else hardMin
        findSingleLineSize(
            text = paragraphs.first(),
            availableWidth = availableWidth.toFloat(),
            maxTextSp = maxTextSp,
            minTextSp = singleLineMin
        )?.let { fittedSize ->
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fittedSize)
            setSingleLine(true)
            maxLines = 1
            text = paragraphs.first()
            return
        }
    }

    val preferredLines = preferredWrappedMaxLines.coerceAtLeast(1)

    findWrappedText(
        paragraphs = paragraphs,
        availableWidth = availableWidth.toFloat(),
        maxTextSp = maxTextSp,
        minTextSp = softMin,
        preferredMaxLines = preferredLines
    )?.let { wrapped ->
        applyWrappedResult(wrapped)
        return
    }

    findWrappedText(
        paragraphs = paragraphs,
        availableWidth = availableWidth.toFloat(),
        maxTextSp = softMin,
        minTextSp = hardMin,
        preferredMaxLines = preferredLines
    )?.let { wrapped ->
        applyWrappedResult(wrapped)
        return
    }

    buildWrappedText(
        paragraphs = paragraphs,
        availableWidth = availableWidth.toFloat(),
        textSp = hardMin
    )?.let { wrapped ->
        applyWrappedResult(wrapped)
        return
    }

    setTextSize(TypedValue.COMPLEX_UNIT_SP, hardMin)
    setSingleLine(true)
    maxLines = 1
    text = paragraphs.firstOrNull().orEmpty()
}

private fun normalizeParagraphs(rawText: String): List<String> =
    rawText
        .split(Regex("\n\\s*\n"))
        .mapNotNull { paragraph ->
            paragraph
                .replace(Regex("\\s+"), " ")
                .trim()
                .takeIf { it.isNotEmpty() }
        }

private fun TextView.findSingleLineSize(
    text: String,
    availableWidth: Float,
    maxTextSp: Float,
    minTextSp: Float
): Float? {
    var size = maxTextSp
    while (size >= minTextSp) {
        val paint = TextPaint(paint).apply { textSize = spToPx(size) }
        if (paint.measureText(text) <= availableWidth) return size
        size -= TEXT_SIZE_STEP_SP
    }
    return null
}

private fun TextView.findWrappedText(
    paragraphs: List<String>,
    availableWidth: Float,
    maxTextSp: Float,
    minTextSp: Float,
    preferredMaxLines: Int
): WrappedTextResult? {
    var size = maxTextSp
    while (size >= minTextSp) {
        val wrapped = buildWrappedText(paragraphs, availableWidth, size)
        if (wrapped != null && wrapped.lineCount <= preferredMaxLines) return wrapped
        size -= TEXT_SIZE_STEP_SP
    }
    return null
}

private fun TextView.buildWrappedText(
    paragraphs: List<String>,
    availableWidth: Float,
    textSp: Float
): WrappedTextResult? {
    val textPaint = TextPaint(paint).apply { textSize = spToPx(textSp) }
    val lines = mutableListOf<String>()
    var lineCount = 0

    paragraphs.forEachIndexed { index, paragraph ->
        val wrappedLines = wrapParagraphByWords(paragraph, textPaint, availableWidth) ?: return null
        lines += wrappedLines
        lineCount += wrappedLines.size
        if (index != paragraphs.lastIndex) {
            lines += ""
            lineCount++
        }
    }

    return WrappedTextResult(
        text = lines.joinToString("\n"),
        textSp = textSp,
        lineCount = lineCount
    )
}

private fun wrapParagraphByWords(
    paragraph: String,
    textPaint: TextPaint,
    availableWidth: Float
): List<String>? {
    val words = paragraph.split(' ').filter { it.isNotBlank() }
    if (words.isEmpty()) return listOf("")

    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        if (textPaint.measureText(word) > availableWidth) return null

        val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (textPaint.measureText(candidate) <= availableWidth) {
            currentLine = candidate
        } else {
            if (currentLine.isNotEmpty()) lines += currentLine
            currentLine = word
        }
    }

    if (currentLine.isNotEmpty()) lines += currentLine
    return lines
}

private fun TextView.spToPx(textSp: Float): Float =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        textSp,
        resources.displayMetrics
    )
