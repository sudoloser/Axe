package com.my.axe.data

import com.my.axe.preference.AppSettings
import com.my.axe.preference.DarkThemePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Tests for AppSettings data class changes in this PR:
 * - Removed `buttonShape` field (and `modifyButtonShape` function)
 * - Removed `MAIN_SCREEN_BUTTON_SHAPE` preference constant usage
 * - paletteStyleIndex default remains 0
 */
class AppSettingsTest {

    @Test
    fun `AppSettings default values are correct`() {
        val settings = AppSettings()
        assertFalse(settings.isDynamicColorEnabled)
        assertEquals(0, settings.paletteStyleIndex)
    }

    @Test
    fun `AppSettings default DarkThemePreference uses FOLLOW_SYSTEM`() {
        val settings = AppSettings()
        assertEquals(DarkThemePreference.FOLLOW_SYSTEM, settings.darkTheme.darkThemeValue)
        assertFalse(settings.darkTheme.isHighContrastModeEnabled)
    }

    @Test
    fun `AppSettings does not have buttonShape field`() {
        // If buttonShape were a field, this reflection check would find it.
        // Since it was removed in this PR, the field must not exist.
        val fieldNames = AppSettings::class.java.declaredFields.map { it.name }
        assertFalse(
            "buttonShape field must not exist in AppSettings after removal in this PR",
            fieldNames.contains("buttonShape")
        )
    }

    @Test
    fun `AppSettings copy preserves paletteStyleIndex`() {
        val original = AppSettings(paletteStyleIndex = 3)
        val copied = original.copy(isDynamicColorEnabled = true)
        assertEquals(3, copied.paletteStyleIndex)
    }

    @Test
    fun `AppSettings copy can update paletteStyleIndex`() {
        val original = AppSettings(paletteStyleIndex = 0)
        val updated = original.copy(paletteStyleIndex = 2)
        assertEquals(2, updated.paletteStyleIndex)
    }

    @Test
    fun `AppSettings with different paletteStyleIndex values are not equal`() {
        val a = AppSettings(paletteStyleIndex = 0)
        val b = AppSettings(paletteStyleIndex = 1)
        assert(a != b)
    }

    @Test
    fun `AppSettings with same values are equal`() {
        val a = AppSettings(paletteStyleIndex = 2, isDynamicColorEnabled = true)
        val b = AppSettings(paletteStyleIndex = 2, isDynamicColorEnabled = true)
        assertEquals(a, b)
    }

    @Test
    fun `DarkThemePreference constants have correct values`() {
        assertEquals(1, DarkThemePreference.FOLLOW_SYSTEM)
        assertEquals(2, DarkThemePreference.ON)
        assertEquals(3, DarkThemePreference.OFF)
    }

    @Test
    fun `AppSettings seedColor default matches DEFAULT_SEED_COLOR`() {
        val settings = AppSettings()
        // DEFAULT_SEED_COLOR = 0xFFAF92F1.toInt()
        assertEquals(0xFFAF92F1.toInt(), settings.seedColor)
    }
}