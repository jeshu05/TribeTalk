package org.tribetalk.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// TRIBE TALK: MODERN EDUCATIONAL DESIGN SYSTEM (Clean, Friendly, Inspiring)
// Inspired by Khan Academy, Duolingo, Brilliant, and Apple Learning
// =============================================================================

// 1. Primary Palette: Fresh Learning Emerald & Teal
val EduPrimary = Color(0xFF0D9488)          // Inspiring Teal Emerald (Buttons, Key Accents)
val EduPrimaryDark = Color(0xFF0F766E)      // Deep Forest Teal (Active states, Dark Mode)
val EduPrimaryLight = Color(0xFFCCFBF1)     // Soft Teal Tint (Pills, Selection Containers)
val EduPrimaryContainer = Color(0xFFF0FDFA) // Ultra-soft Teal Canvas Tint

// 2. Secondary & Competency Accents
val EduIndigo = Color(0xFF4F46E5)           // Learning Indigo (Literacy, Vocabulary)
val EduIndigoLight = Color(0xFFEEF2FF)      // Soft Indigo Tint (Literacy Badges)
val EduAmber = Color(0xFFD97706)            // Sunshine Amber (Phonics, Solutions, Streaks)
val EduAmberLight = Color(0xFFFEF3C7)       // Soft Amber Tint (Solution Cards, Guides)
val EduSky = Color(0xFF0284C7)              // Sky Blue (Numeracy, Sequence Trains)
val EduSkyLight = Color(0xFFE0F2FE)         // Soft Sky Tint (Counting Containers)
val EduRose = Color(0xFFE11D48)             // Coral Rose (Attention, Difficulty: Challenge)
val EduRoseLight = Color(0xFFFFE4E6)        // Soft Rose Tint
val EduGreen = Color(0xFF16A34A)            // Vibrant Nature Green
val EduGreenLight = Color(0xFFDCFCE7)       // Soft Green Tint (Success, Completion)
val EduGreenDark = Color(0xFF15803D)        // Forest Green

// 3. Clean Educational Canvas & Card Surfaces (Light Mode Defaults)
val EduBackground = Color(0xFFF8FAFC)       // Slate 50 (Soft, calming, warm background)
val EduSurface = Color(0xFFFFFFFF)          // Pure White (Crisp elevated cards)
val EduSurfaceElevated = Color(0xFFF1F5F9)  // Slate 100 (Panels, input backgrounds)
val EduBorder = Color(0xFFE2E8F0)           // Slate 200 (Hairline 1dp subtle clean border)
val EduBorderSubtle = Color(0xFFF1F5F9)     // Slate 100 (Very soft divider)

// 4. Clean High-Legibility Typography
val EduTextPrimary = Color(0xFF0F172A)      // Slate 900 (High-contrast, crystal clear headers)
val EduTextSecondary = Color(0xFF475569)    // Slate 600 (Secondary descriptions, prompts)
val EduTextMuted = Color(0xFF94A3B8)        // Slate 400 (Captions, timestamps, hints)
val EduTextOnPrimary = Color(0xFFFFFFFF)    // Pure White on primary buttons

// 5. Dark Mode Surfaces (Midnight Slate - No harsh pitch-black)
val EduDarkBackground = Color(0xFF0B0F17)   // Deep Midnight Slate
val EduDarkSurface = Color(0xFF131B2E)      // Elegant Slate Card Surface
val EduDarkSurfaceElevated = Color(0xFF1E293B) // Slate 800
val EduDarkBorder = Color(0xFF233044)       // Slate 700 subtle border
val EduDarkTextPrimary = Color(0xFFF8FAFC)  // Slate 50
val EduDarkTextSecondary = Color(0xFF94A3B8) // Slate 400

// 6. Status Indicators
val StatusListening = Color(0xFF10B981)     // Soft emerald pulse
val StatusTranscribing = Color(0xFFF59E0B)  // Warm Amber
val StatusTranslating = Color(0xFF0284C7)   // Sky Blue
val StatusSynthesizing = Color(0xFF8B5CF6)  // Soft Violet
val StatusReady = Color(0xFF10B981)         // Ready Green

// 7. Backward-compatible aliases updated to modern harmonious palette
val EmeraldGreen = EduPrimary
val EmeraldPrimary = EduPrimary
val EmeraldDark = EduPrimaryDark
val EmeraldContainerDark = Color(0xFF134E4A)
val EmeraldContainerLight = EduPrimaryLight
val EmeraldMint = Color(0xFF14B8A6)
val EmeraldGlow = Color(0x250D9488)

val PureBlack = EduDarkBackground
val DarkSurface = EduDarkSurface
val DarkCard = EduDarkSurface
val DarkCardElevated = EduDarkSurfaceElevated
val DarkBorder = EduDarkBorder
val DarkBorderGreen = EduPrimary.copy(alpha = 0.5f)

val PureWhite = Color(0xFFFFFFFF)
val OffWhite = EduBackground
val WhiteSecondary = EduTextSecondary
val WhiteMuted = EduTextMuted
val CrispBorderLight = EduBorder
