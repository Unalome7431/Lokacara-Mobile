package com.app.lokacara.ui.components.createevent

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SemanticErrorBase

@Composable
fun FieldError(message: String?) {
    if (message != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontFamily = NunitoFont,
            fontSize = 11.sp,
            color = SemanticErrorBase
        )
    }
}
