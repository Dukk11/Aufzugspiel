package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val coins: Int = 300,
    val currentFloor: Int = 1,
    val selectedModelId: String = "model_default",
    val visitedFloors: List<Int> = listOf(1),
    val unlockedModelIds: List<String> = listOf("model_default"),
    val speedMode: String = "TURBO",
    val cabinTheme: String = "GOLDEN_LUXURY",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
