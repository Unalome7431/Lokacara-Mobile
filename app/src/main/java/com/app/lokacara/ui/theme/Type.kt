package com.app.lokacara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.lokacara.R

// 1. Setup FontFamily untuk Nunito (Headings)
val NunitoFont = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

// 2. Setup FontFamily untuk Plus Jakarta Sans (Body)
val PlusJakartaSansFont = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold)
)

// 3. Mapping ke Material 3 Typography
val Typography = Typography(

    // === HEADINGS (Nunito) ===
    displayLarge = TextStyle( // h1
        fontFamily = NunitoFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 64.sp
    ),
    displayMedium = TextStyle( // h2
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp
    ),
    displaySmall = TextStyle( // h3
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    headlineLarge = TextStyle( // h4
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp
    ),
    headlineMedium = TextStyle( // h5
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    headlineSmall = TextStyle( // h6
        fontFamily = NunitoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),

    // === BODY (Plus Jakarta Sans) ===
    bodyLarge = TextStyle( // large
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Normal, // Regular
        fontSize = 24.sp
    ),
    bodyMedium = TextStyle( // base
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Normal, // Regular
        fontSize = 20.sp
    ),
    bodySmall = TextStyle( // small
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle( // micro
        fontFamily = PlusJakartaSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)