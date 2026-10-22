package com.example.data.model

data class LatLng(val latitude: Double, val longitude: Double)

data class WalkingRoute(
    val id: String,
    val name: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val points: List<LatLng>,
    val facilitiesNearby: List<Facility> = emptyList(),
    val barriersNearby: List<BarrierReport> = emptyList(),
    val shadeShelterCoveragePercent: Int = 0,
    val confidenceScore: Int = 85,
    val comparisonExplanation: String = "",
    val limitationsExplanation: String = "",
    val isAlternative: Boolean = false,
    val provider: String = "OSRM OpenStreetMap Walking Routing"
) {
    val distanceDisplay: String
        get() = if (distanceMeters >= 1000) {
            String.format("%.1f km", distanceMeters / 1000.0)
        } else {
            "$distanceMeters m"
        }

    val walkingTimeDisplay: String
        get() {
            val minutes = Math.max(1, (durationSeconds + 30) / 60)
            return "~$minutes min"
        }
}
