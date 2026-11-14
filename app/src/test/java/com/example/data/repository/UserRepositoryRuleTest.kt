package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class UserRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun authenticatedUser_canSaveAndGetProfile() = runBlocking {
        val uid = signInTestUser("player1@skymall.com")
        val repository = UserRepository(firestore, auth)

        val profile = UserProfile(
            userId = uid,
            displayName = "SkyMall Pilot",
            coins = 750,
            currentFloor = 42,
            selectedModelId = "model_default",
            visitedFloors = listOf(1, 10, 42),
            unlockedModelIds = listOf("model_default"),
            speedMode = "TURBO",
            cabinTheme = "GOLDEN_LUXURY"
        )

        repository.saveProfile(profile)
        val loaded = repository.getProfile(uid)

        assertNotNull(loaded)
        assertEquals(uid, loaded?.userId)
        assertEquals(750, loaded?.coins)
        assertEquals(42, loaded?.currentFloor)
    }

    @Test
    fun unauthenticatedUser_cannotSaveProfile() = runBlocking {
        auth.signOut()
        val repository = UserRepository(firestore, auth)

        val profile = UserProfile(
            userId = "unauthenticated_user",
            coins = 100,
            currentFloor = 1
        )

        try {
            repository.saveProfile(profile)
            fail("Expected exception when unauthenticated user attempts save")
        } catch (e: IllegalStateException) {
            // Expected: fail-fast check
        }
    }

    @Test
    fun authenticatedUser_cannotAccessAnotherUserProfile() = runBlocking {
        val aliceUid = signInTestUser("alice@skymall.com")
        val aliceRepo = UserRepository(firestore, auth)
        aliceRepo.saveProfile(
            UserProfile(
                userId = aliceUid,
                coins = 1200,
                currentFloor = 88
            )
        )

        val bobUid = signInTestUser("bob@skymall.com")
        val bobRepo = UserRepository(firestore, auth)

        try {
            val doc = firestore.collection("users").document(aliceUid).get()
            // In rules, Bob reading Alice's document should fail with PERMISSION_DENIED
            val snapshot = doc.await()
            if (snapshot.exists()) {
                fail("Bob should not be able to read Alice's user profile")
            }
        } catch (e: Exception) {
            // Expected: PERMISSION_DENIED
        }
    }
}
