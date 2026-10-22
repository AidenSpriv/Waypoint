package com.example.data.model

enum class BarrierCategory(val displayName: String, val iconEmoji: String) {
    BLOCKED_SIDEWALK("Blocked Sidewalk", "🚫"),
    DAMAGED_PAVEMENT("Damaged Pavement", "⚠️"),
    MISSING_CURB_RAMP("Missing Curb Ramp", "♿"),
    UNCLEAR_CROSSING("Unclear Crossing", "🚸"),
    LACK_OF_SHADE("Extreme Sun / Lack of Shade", "☀️"),
    CONSTRUCTION_DETOUR("Construction Detour", "🚧")
}

enum class ReportStatus(val displayName: String) {
    COMMUNITY_REPORTED("Community Reported"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified by Curator"),
    RESOLVED("Resolved / Cleared"),
    REJECTED_OR_DUPLICATE("Rejected / Duplicate")
}

enum class ReviewPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

data class BarrierReport(
    val id: String,
    val category: BarrierCategory,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String? = null,
    val source: String = "Community Contributor",
    val status: ReportStatus = ReportStatus.COMMUNITY_REPORTED,
    val priority: ReviewPriority = ReviewPriority.MEDIUM,
    val priorityRationale: String = "",
    val uncertaintyNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val pilotAreaId: String = "doha_msheireb",
    val moderatorNotes: String = "",
    val isDemoData: Boolean = false
)
