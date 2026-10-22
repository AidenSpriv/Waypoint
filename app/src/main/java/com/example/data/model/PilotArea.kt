package com.example.data.model

data class PilotArea(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultZoom: Int = 16,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double,
    val surveyTeam: String = "Waypoint Doha Pilot Survey"
) {
    companion object {
        val ALL_PILOTS = listOf(
            PilotArea(
                id = "doha_msheireb",
                name = "Doha — Msheireb Downtown & Souq Waqif",
                shortName = "Msheireb Metro",
                description = "Primary surveyed pilot zone around Msheireb interchange station, Al Kahraba St, and Souq Waqif pedestrian alleys.",
                centerLat = 25.2867,
                centerLng = 51.5333,
                defaultZoom = 16,
                minLat = 25.2800,
                maxLat = 25.2950,
                minLng = 51.5250,
                maxLng = 51.5420
            ),
            PilotArea(
                id = "doha_al_bidda",
                name = "Doha — Al Bidda Park & Corniche",
                shortName = "Al Bidda",
                description = "Secondary pilot zone covering Al Bidda Metro hub, pedestrian promenade, and park shade corridors.",
                centerLat = 25.2930,
                centerLng = 51.5170,
                defaultZoom = 16,
                minLat = 25.2850,
                maxLat = 25.3050,
                minLng = 51.5080,
                maxLng = 51.5280
            ),
            PilotArea(
                id = "doha_education_city",
                name = "Doha — Education City & QNL",
                shortName = "Education City",
                description = "Academic transit hub between Education City Metro, Qatar National Library, and shaded tram connections.",
                centerLat = 25.3180,
                centerLng = 51.4420,
                defaultZoom = 16,
                minLat = 25.3080,
                maxLat = 25.3280,
                minLng = 51.4320,
                maxLng = 51.4550
            )
        )

        val DEFAULT = ALL_PILOTS[0]
    }
}
