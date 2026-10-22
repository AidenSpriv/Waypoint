package com.example.data.model

enum class FacilityCategory(val displayName: String, val iconEmoji: String) {
    WATER_REFILL("Water Refill", "💧"),
    PUBLIC_TOILET("Public Toilet", "🚻"),
    BENCH_SEATING("Benches & Seating", "🪑"),
    SHELTERED_REST("Sheltered Rest Area", "☂️"),
    METRO_ENTRANCE("Metro & Transit", "🚇"),
    AC_WALKWAY("AC Walkway / Arcade", "❄️")
}

enum class VerificationStatus(val displayName: String) {
    VERIFIED("Verified by Field Survey"),
    COMMUNITY_REPORTED("Community Reported"),
    EXTERNALLY_SOURCED("Externally Sourced (OSM)")
}

data class Facility(
    val id: String,
    val name: String,
    val category: FacilityCategory,
    val latitude: Double,
    val longitude: Double,
    val source: String,
    val verificationStatus: VerificationStatus,
    val lastVerifiedAt: String,
    val notes: String = "",
    val caveats: String = "",
    val pilotAreaId: String = "doha_msheireb",
    val isDemoData: Boolean = true
)
