package com.example

import com.example.model.ElevatorModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ElevatorModelTest {

    @Test
    fun `elevator models has at least three distinct models`() {
        val models = ElevatorModel.entries
        assertTrue("Must provide at least 3 elevator models", models.size >= 3)
        assertEquals(4, models.size)
    }

    @Test
    fun `each elevator model has unique speed properties`() {
        val speeds = ElevatorModel.entries.map { it.topSpeedKmh }
        assertEquals("Each model should have distinct top speed", speeds.distinct().size, ElevatorModel.entries.size)

        val delays = ElevatorModel.entries.map { it.stepDelayMs }
        assertEquals("Each model should have distinct step travel delay", delays.distinct().size, ElevatorModel.entries.size)
    }

    @Test
    fun `each elevator model has unique capacity and payload limits`() {
        val passengerCaps = ElevatorModel.entries.map { it.maxPassengers }
        assertEquals("Each model must have distinct passenger capacity", passengerCaps.distinct().size, ElevatorModel.entries.size)

        val payloads = ElevatorModel.entries.map { it.maxPayloadKg }
        assertEquals("Each model must have distinct payload limit", payloads.distinct().size, ElevatorModel.entries.size)

        // Freight elevator must have the largest payload
        val titan = ElevatorModel.TITAN_FREIGHT
        assertTrue(titan.maxPayloadKg >= 3000)
        assertTrue(titan.maxPassengers >= 30)

        // Vintage heritage elevator is smaller and charming
        val heritage = ElevatorModel.HERITAGE_VINTAGE
        assertTrue(heritage.maxPassengers <= 10)
    }

    @Test
    fun `each elevator model has distinct visual design attributes`() {
        val categories = ElevatorModel.entries.map { it.styleCategory }
        assertEquals("Each model must have a unique style category", categories.distinct().size, ElevatorModel.entries.size)

        ElevatorModel.entries.forEach { model ->
            assertNotNull(model.modelName)
            assertNotNull(model.manufacturer)
            assertNotNull(model.emoji)
            assertNotNull(model.tagline)
            assertNotEquals(0L, model.primaryAccentColor)
            assertNotEquals(0L, model.wallColorDark)
            assertNotEquals(0L, model.doorColorStart)
        }
    }

    @Test
    fun `retrieval by ID works accurately`() {
        ElevatorModel.entries.forEach { model ->
            val found = ElevatorModel.fromId(model.id)
            assertEquals(model, found)
        }

        // Unknown id returns default model
        val fallback = ElevatorModel.fromId("unknown_model_xyz")
        assertEquals(ElevatorModel.SKYLINE_PANORAMA, fallback)
    }
}
