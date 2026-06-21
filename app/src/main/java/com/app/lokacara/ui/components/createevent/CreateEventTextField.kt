package com.app.lokacara.ui.components.createevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lokacara.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    containerColor: Color = CreateEventLightBlue,
    labelSize: TextUnit = 16.sp,
    supportingText: String? = null,
    supportingColor: Color = Gray500,
    isError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                fontSize = labelSize,
                color = Gray800
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = Gray500
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isError) SemanticErrorBase.copy(alpha = 0.06f) else containerColor,
                unfocusedContainerColor = if (isError) SemanticErrorBase.copy(alpha = 0.06f) else containerColor,
                disabledContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = if (isError) SemanticErrorBase else Color.Transparent,
                focusedTextColor = Gray900,
                unfocusedTextColor = Gray900
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                fontFamily = NunitoFont,
                fontSize = 11.sp,
                color = supportingColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
