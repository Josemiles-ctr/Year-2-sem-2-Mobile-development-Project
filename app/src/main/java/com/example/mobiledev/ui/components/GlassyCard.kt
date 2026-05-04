package com.example.mobiledev.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = Color.White,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (border != null) Modifier.border(border, shape) else Modifier
        )

    val cardColors = CardDefaults.elevatedCardColors(
        containerColor = containerColor,
        contentColor = Color.Black,
        disabledContainerColor = containerColor,
        disabledContentColor = Color.Black
    )

    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = cardModifier,
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = shape,
            content = content
        )
    } else {
        ElevatedCard(
            modifier = cardModifier,
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            shape = shape,
            content = content
        )
    }
}
