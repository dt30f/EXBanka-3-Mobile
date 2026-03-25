package com.exbanka.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ExBankaColors = lightColorScheme()

@Composable
fun ExBankaMobileTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ExBankaColors,
        content = content,
    )
}
