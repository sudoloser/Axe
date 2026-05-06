package com.my.axe.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TriangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class StarShape(val points: Int = 5, val innerRadiusRatio: Float = 0.5f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val outerRadius = size.width / 2f
            val innerRadius = outerRadius * innerRadiusRatio
            val angleStep = Math.PI / points
            
            var currentAngle = -Math.PI / 2
            
            moveTo(
                (centerX + outerRadius * Math.cos(currentAngle)).toFloat(),
                (centerY + outerRadius * Math.sin(currentAngle)).toFloat()
            )
            
            for (i in 1..points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val x = (centerX + radius * Math.cos(currentAngle + i * angleStep)).toFloat()
                val y = (centerY + radius * Math.sin(currentAngle + i * angleStep)).toFloat()
                lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

fun getButtonShape(shape: String, index: Int = 0): Shape {
    return when (shape) {
        "Circles" -> CircleShape
        "Triangles" -> TriangleShape()
        "Star" -> StarShape()
        else -> {
            if (index % 2 == 0) {
                RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp)
            } else {
                RoundedCornerShape(44.dp, 20.dp, 44.dp, 20.dp)
            }
        }
    }
}
