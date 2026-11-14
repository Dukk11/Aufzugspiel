package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StudioTheme

/**
 * Floor Directory Dialog supporting browsing, searching, and traveling to all 120 floors.
 */
@Composable
fun FloorDirectoryDialog(
    currentFloor: Int,
    targetFloor: Int,
    visitedFloors: Set<Int> = emptySet(),
    isMoving: Boolean = false,
    doorsOpen: Boolean = true,
    onFloorSelected: (Int) -> Unit,
    onTravelToFloor: (Int) -> Unit = onFloorSelected,
    onStepIntoMall: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val studioColors = StudioTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = studioColors.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            tonalElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
        ) {
            MallFloorDirectoryComponent(
                currentFloor = currentFloor,
                targetFloor = targetFloor,
                visitedFloors = visitedFloors,
                isMoving = isMoving,
                doorsOpen = doorsOpen,
                onSelectFloor = onFloorSelected,
                onTravelToFloor = onTravelToFloor,
                onStepIntoMall = onStepIntoMall,
                onDismiss = onDismiss
            )
        }
    }
}
