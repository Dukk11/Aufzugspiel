package com.example

import com.example.model.FloorCategory
import com.example.model.SkyscraperFloorRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyscraperFloorModelTest {

    @Test
    fun `skyscraper has over 100 floors`() {
        val allFloors = SkyscraperFloorRegistry.getAllFloors()
        assertTrue("Skyscraper must have 100+ floors", allFloors.size >= 100)
        assertEquals(120, allFloors.size)
        assertEquals(1, allFloors.first().floorNumber)
        assertEquals(120, allFloors.last().floorNumber)
    }

    @Test
    fun `floor altitude increases consistently with floor height`() {
        val ground = SkyscraperFloorRegistry.getFloor(1)
        val top = SkyscraperFloorRegistry.getFloor(120)

        assertEquals(12, ground.altitudeMeters)
        assertEquals(12 + (120 - 1) * 4, top.altitudeMeters)
        assertTrue(top.altitudeMeters > ground.altitudeMeters)
        assertEquals(476, top.altitudeDeltaMeters(1))
    }

    @Test
    fun `travel time calculation gives positive duration for distinct floors`() {
        val ground = SkyscraperFloorRegistry.getFloor(1)
        val floor50 = SkyscraperFloorRegistry.getFloor(50)

        assertEquals(0, ground.estimateTravelSeconds(1))
        assertTrue(floor50.estimateTravelSeconds(1) > 0)
    }

    @Test
    fun `landmarks are identified with titles and badges`() {
        val landmarks = SkyscraperFloorRegistry.getLandmarkFloors()
        assertTrue("Must have multiple landmark floors", landmarks.size >= 8)

        val floor100 = SkyscraperFloorRegistry.getFloor(100)
        assertTrue(floor100.isLandmark)
        assertNotNull(floor100.landmarkTitle)
        assertTrue(floor100.hasCoinFountain)
    }

    @Test
    fun `fountain floors exist every 10 floors`() {
        val fountains = SkyscraperFloorRegistry.getFountainFloors()
        assertEquals(12, fountains.size)
        fountains.forEach { floor ->
            assertEquals(0, floor.floorNumber % 10)
        }
    }

    @Test
    fun `search filter finds floors by number or keyword`() {
        val retroResults = SkyscraperFloorRegistry.filterFloors(query = "Arcade")
        assertTrue(retroResults.isNotEmpty())
        assertTrue(retroResults.any { it.floorNumber == 42 })

        val numberResults = SkyscraperFloorRegistry.filterFloors(query = "77")
        assertTrue(numberResults.any { it.floorNumber == 77 })
    }

    @Test
    fun `sector navigation groups 120 floors into distinct ranges`() {
        val sectors = SkyscraperFloorRegistry.SECTORS
        assertEquals(6, sectors.size)
        assertEquals(1..20, sectors[0].range)
        assertEquals(101..120, sectors[5].range)

        val floor10Sector = SkyscraperFloorRegistry.getSectorForFloor(10)
        assertEquals("sec_1", floor10Sector.id)

        val floor115Sector = SkyscraperFloorRegistry.getSectorForFloor(115)
        assertEquals("sec_6", floor115Sector.id)
    }

    @Test
    fun `ride progress calculates correctly between floors`() {
        val startFloor = 10
        val targetFloor = 60

        fun calcProgress(current: Int): Float {
            return (kotlin.math.abs(current - startFloor).toFloat() /
                    kotlin.math.abs(targetFloor - startFloor)).coerceIn(0f, 1f)
        }

        assertEquals(0.0f, calcProgress(10), 0.001f)
        assertEquals(0.5f, calcProgress(35), 0.001f)
        assertEquals(1.0f, calcProgress(60), 0.001f)
    }
}
