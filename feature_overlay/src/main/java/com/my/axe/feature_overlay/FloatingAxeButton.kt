package com.my.axe.feature_overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.my.axe.preference.Prefs
import com.my.axe.resources.R

@Composable
fun FloatingAxeButton(
    onExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    val opacity by remember { mutableStateOf(Prefs[Prefs.OVERLAY_OPACITY, 0.8f]) }
    val scale by remember { mutableStateOf(Prefs[Prefs.OVERLAY_SCALE, 1.0f]) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .scale(scale)
            .alpha(opacity)
            .size(60.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onExpand) {
            Icon(
                painter = painterResource(id = R.drawable.ic_apps),
                contentDescription = "Axe Overlay",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
