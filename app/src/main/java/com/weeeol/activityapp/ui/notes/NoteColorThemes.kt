package com.weeeol.activityapp.ui.notes

import androidx.compose.ui.graphics.Color

data class NoteColorTheme(
    val id: Int,
    val name: String,
    val accentColor: Color,
    val lightBg: Color,
    val darkBg: Color
)

val NoteColors = listOf(
    NoteColorTheme(0, "Default", Color(0xFF8E8E93), Color.Transparent, Color.Transparent),
    NoteColorTheme(1, "Coral", Color(0xFFFF453A), Color(0xFFFFECEB), Color(0xFF2C1517)),
    NoteColorTheme(2, "Amber", Color(0xFFFF9F0A), Color(0xFFFFF4E5), Color(0xFF2C2013)),
    NoteColorTheme(3, "Mint", Color(0xFF30D158), Color(0xFFE8F9ED), Color(0xFF132A1C)),
    NoteColorTheme(4, "Azure", Color(0xFF0A84FF), Color(0xFFE8F2FF), Color(0xFF132235)),
    NoteColorTheme(5, "Purple", Color(0xFFBF5AF2), Color(0xFFF7ECFD), Color(0xFF291836)),
    NoteColorTheme(6, "Rose", Color(0xFFFF375F), Color(0xFFFFEBF0), Color(0xFF2E131E))
)

fun getNoteColorTheme(colorIndex: Int): NoteColorTheme {
    return NoteColors.getOrElse(colorIndex.coerceIn(0, NoteColors.size - 1)) { NoteColors[0] }
}
