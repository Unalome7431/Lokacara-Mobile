package com.app.lokacara.ui.components.createevent

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.app.lokacara.ui.theme.Gray500
import com.app.lokacara.ui.theme.Gray700
import com.app.lokacara.ui.theme.Gray800
import com.app.lokacara.ui.theme.Gray900
import com.app.lokacara.ui.theme.NunitoFont
import com.app.lokacara.ui.theme.SvgOrange
import com.app.lokacara.ui.theme.SvgPrimaryBlue

@Composable
fun PriceSection(
    isFree: Boolean,
    onToggleFree: (Boolean) -> Unit,
    priceAmount: String,
    onPriceAmountChange: (String) -> Unit,
    containerColor: Color = Color.White,
    isError: Boolean = false
) {
    val selectedFreeBg by animateColorAsState(
        targetValue = if (isFree) SvgPrimaryBlue else Color.White,
        label = "priceFreeBg"
    )
    val selectedPaidBg by animateColorAsState(
        targetValue = if (!isFree) SvgPrimaryBlue else Color.White,
        label = "pricePaidBg"
    )
    val selectedFreeText by animateColorAsState(
        targetValue = if (isFree) Color.White else Gray700,
        label = "priceFreeText"
    )
    val selectedPaidText by animateColorAsState(
        targetValue = if (!isFree) Color.White else Gray700,
        label = "pricePaidText"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Harga Event",
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Gray800
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PriceChoiceChip(
                text = "Gratis",
                backgroundColor = selectedFreeBg,
                contentColor = selectedFreeText,
                modifier = Modifier.weight(1f),
                onClick = { onToggleFree(true) }
            )
            PriceChoiceChip(
                text = "Berbayar",
                backgroundColor = selectedPaidBg,
                contentColor = selectedPaidText,
                modifier = Modifier.weight(1f),
                onClick = { onToggleFree(false) }
            )
        }

        if (!isFree) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = priceAmount,
                    onValueChange = { input ->
                        val digits = input.filter(Char::isDigit).take(9)
                        onPriceAmountChange(digits)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Masukkan nominal harga", fontFamily = NunitoFont, fontSize = 12.sp, color = Gray500) },
                    prefix = {
                        Text(
                            text = "Rp",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = Gray700
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = containerColor,
                        unfocusedContainerColor = containerColor,
                        focusedBorderColor = SvgOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Gray900,
                        unfocusedTextColor = Gray900
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    text = "Masukkan nominal tanpa titik atau koma.",
                    fontFamily = NunitoFont,
                    fontSize = 11.sp,
                    color = Gray500
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = "Event gratis akan menampilkan label Gratis di aplikasi.",
                    fontFamily = NunitoFont,
                    fontSize = 12.sp,
                    color = Gray700,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PriceChoiceChip(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = contentColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
