package com.weeeol.activityapp

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class ModelsUnitTest {

    @Test
    fun note_defaultValues_areCorrect() {
        val note = Note(title = "Test Title", content = "Test Content")
        assertEquals("Test Title", note.title)
        assertEquals("Test Content", note.content)
        assertNull(note.folderId)
        assertFalse(note.isCodeMode)
        assertFalse(note.isPinned)
        assertEquals(0, note.colorIndex)
        assertNotNull(note.id)
        assertTrue(note.createdAt > 0)
    }

    @Test
    fun projectFolder_defaultEmoji_isFolder() {
        val folder = ProjectFolder(name = "Work")
        assertEquals("Work", folder.name)
        assertEquals("📂", folder.emoji)
        assertNotNull(folder.id)
    }

    @Test
    fun timerEvent_durationCalculation_isCorrect() {
        val timer = TimerEvent(activityName = "Focus", durationMinutes = 25)
        assertEquals("Focus", timer.activityName)
        assertEquals(1500L, timer.totalSeconds)
        assertEquals(1500L, timer.remainingSeconds)
        assertFalse(timer.isRunning)
    }

    @Test
    fun timerEvent_customTotalSeconds_takesPrecedence() {
        val timer = TimerEvent(
            activityName = "Reading",
            durationMinutes = 10,
            totalSecondsParam = 300L
        )
        assertEquals(300L, timer.totalSeconds)
        assertEquals(300L, timer.remainingSeconds)
    }

    @Test
    fun navItem_selectedAndUnselectedIcons_areDefined() {
        val health = NavItem.Health
        assertEquals("Health", health.title)
        assertNotNull(health.selectedIcon)
        assertNotNull(health.unselectedIcon)
        assertEquals(health.selectedIcon, health.icon)

        val timer = NavItem.Timer
        assertEquals("Timer", timer.title)
        assertNotNull(timer.selectedIcon)
        assertNotNull(timer.unselectedIcon)
    }
}
