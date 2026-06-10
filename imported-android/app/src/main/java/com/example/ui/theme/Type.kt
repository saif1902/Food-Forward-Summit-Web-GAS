package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Explicit Custom Font Families mapped for the application
val NeogroteskProFontFamily = FontFamily.Default // Main Text Font Family: Neogrotesk Pro fallback
val InterFontFamily = FontFamily.SansSerif // Supporting Text Font Family: Inter
val HelveticaNeueFontFamily = FontFamily.SansSerif // Other Text Font Family: Helvetica Neue

// Convenient styling constants for surgical text formatting
val MainTextStyle = TextStyle(
    fontFamily = NeogroteskProFontFamily,
    color = MainTextColor
)
val SupportingTextStyle = TextStyle(
    fontFamily = InterFontFamily,
    color = SupportingTextColor
)
val OtherTextStyle = TextStyle(
    fontFamily = HelveticaNeueFontFamily,
    color = OtherTextColor
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        color = MainTextColor
    ),
    displayMedium = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        color = MainTextColor
    ),
    displaySmall = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = MainTextColor
    ),
    headlineLarge = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        color = MainTextColor
    ),
    headlineMedium = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        color = MainTextColor
    ),
    headlineSmall = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = MainTextColor
    ),
    titleLarge = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = MainTextColor
    ),
    titleMedium = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = MainTextColor
    ),
    titleSmall = TextStyle(
        fontFamily = NeogroteskProFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = MainTextColor
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = SupportingTextColor
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = SupportingTextColor
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = SupportingTextColor
    ),
    labelLarge = TextStyle(
        fontFamily = HelveticaNeueFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = OtherTextColor
    ),
    labelMedium = TextStyle(
        fontFamily = HelveticaNeueFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = OtherTextColor
    ),
    labelSmall = TextStyle(
        fontFamily = HelveticaNeueFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        color = OtherTextColor
    )
)
