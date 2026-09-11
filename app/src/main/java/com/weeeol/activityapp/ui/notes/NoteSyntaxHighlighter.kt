package com.weeeol.activityapp.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object NoteSyntaxHighlighter {
    val KeywordColor = Color(0xFFC678DD)
    val StringColor = Color(0xFF98C379)
    val NumberColor = Color(0xFFD19A66)
    val CommentColor = Color(0xFF7F848E)

    private val keywordRegex = Regex("\\b(val|var|fun|class|interface|if|else|for|while|return|true|false|null|import|package)\\b")
    private val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
    private val stringRegex = Regex("\".*?\"")
    private val commentRegex = Regex("//.*")
    private val wordRegex = Regex("\\w+")

    fun findActiveWord(textFieldValue: TextFieldValue): String {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        return if (selection.collapsed) {
            val cursor = selection.start
            wordRegex.findAll(text).firstOrNull { cursor in it.range.first..it.range.last + 1 }?.value ?: ""
        } else {
            text.substring(selection.start, selection.end)
        }
    }

    fun createTransformation(
        isCodeMode: Boolean,
        activeWord: String,
        highlightColor: Color,
        onHighlightTextColor: Color
    ): VisualTransformation {
        if (!isCodeMode) return VisualTransformation.None

        return VisualTransformation { text ->
            val annotatedString = buildAnnotatedString {
                append(text.text)

                numberRegex.findAll(text.text).forEach { match ->
                    addStyle(SpanStyle(color = NumberColor), match.range.first, match.range.last + 1)
                }
                keywordRegex.findAll(text.text).forEach { match ->
                    addStyle(SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                }
                stringRegex.findAll(text.text).forEach { match ->
                    addStyle(SpanStyle(color = StringColor), match.range.first, match.range.last + 1)
                }
                commentRegex.findAll(text.text).forEach { match ->
                    addStyle(SpanStyle(color = CommentColor), match.range.first, match.range.last + 1)
                }

                if (activeWord.isNotBlank() && activeWord.matches(wordRegex)) {
                    var startIndex = 0
                    while (startIndex < text.length) {
                        val index = text.indexOf(activeWord, startIndex)
                        if (index == -1) break

                        val isStartBoundary = index == 0 || !text[index - 1].isLetterOrDigit()
                        val isEndBoundary = index + activeWord.length == text.length || !text[index + activeWord.length].isLetterOrDigit()

                        if (isStartBoundary && isEndBoundary) {
                            addStyle(
                                style = SpanStyle(
                                    background = highlightColor,
                                    color = onHighlightTextColor
                                ),
                                start = index,
                                end = index + activeWord.length
                            )
                        }
                        startIndex = index + activeWord.length
                    }
                }
            }
            TransformedText(annotatedString, OffsetMapping.Identity)
        }
    }
}
