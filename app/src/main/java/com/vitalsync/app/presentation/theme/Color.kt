package com.vitalsync.app.presentation.theme

import androidx.compose.ui.graphics.Color

// === Ana Renk Paleti (Premium Sağlık Uygulaması) ===

// Primary — Güven veren tıbbi mavi
val PrimaryLight = Color(0xFF2563EB)
val PrimaryDark = Color(0xFF60A5FA)
val PrimaryContainer = Color(0xFFDBEAFE)
val OnPrimaryContainer = Color(0xFF1E3A5F)

// Secondary — Sağlık yeşili
val SecondaryLight = Color(0xFF059669)
val SecondaryDark = Color(0xFF34D399)
val SecondaryContainer = Color(0xFFD1FAE5)

// Tertiary — Sıcak amber
val TertiaryLight = Color(0xFFD97706)
val TertiaryDark = Color(0xFFFBBF24)

// Arka plan
val BackgroundLight = Color(0xFFF8FAFC)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF334155)

// Metin
val OnBackgroundLight = Color(0xFF0F172A)
val OnBackgroundDark = Color(0xFFF1F5F9)
val OnSurfaceLight = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFE2E8F0)

// === Klinik Durum Renkleri (Analiz motoru tarafından kullanılır) ===
val StatusNormal = Color(0xFF10B981)      // 🟢 Yeşil — Hedef aralıkta
val StatusAttention = Color(0xFFF59E0B)   // 🟡 Amber — Dikkat
val StatusHigh = Color(0xFFEF4444)        // 🔴 Kırmızı — Yüksek
val StatusCritical = Color(0xFFDC2626)    // 🚨 Koyu kırmızı — Acil
val StatusStable = Color(0xFF3B82F6)      // 🔵 Mavi — Stabil
val StatusLow = Color(0xFF6366F1)         // 🟣 Mor — Düşük

// Grafik renkleri
val ChartSystolic = Color(0xFFEF4444)     // Kırmızı — Büyük tansiyon
val ChartDiastolic = Color(0xFF3B82F6)    // Mavi — Küçük tansiyon
val ChartGlucose = Color(0xFFF59E0B)      // Amber — Şeker
val ChartHeartRate = Color(0xFFEC4899)    // Pembe — Nabız
val ChartOxygen = Color(0xFF06B6D4)       // Teal — Oksijen
val ChartTargetBand = Color(0x2010B981)   // Yarı saydam yeşil — Hedef bandı
