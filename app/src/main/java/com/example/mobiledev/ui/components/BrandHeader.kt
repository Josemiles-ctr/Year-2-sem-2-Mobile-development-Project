package com.example.mobiledev.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R

@Composable
fun BrandHeader(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.backgroud),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 24.dp)
    )
}

