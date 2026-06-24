package com.app.lokacara.ui.components.createevent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@Composable
fun DatePickerField(
    value: String,
    onClick: () -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(
            text = label,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Gray800
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isError) SemanticErrorBase.copy(alpha = 0.06f) else Color.White, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) Gray500 else Gray900,
                fontSize = 14.sp,
                fontFamily = NunitoFont
            )
        }
    }
}
