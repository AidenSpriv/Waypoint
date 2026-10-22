package com.example.data.local

import com.example.data.model.BarrierCategory
import com.example.data.model.BarrierReport
import com.example.data.model.Facility
import com.example.data.model.FacilityCategory
import com.example.data.model.ReportStatus
import com.example.data.model.ReviewPriority
import com.example.data.model.VerificationStatus

object PilotDataSeeder {

    val INITIAL_FACILITIES = listOf(
        Facility(
            id = "fac_msh_metro_1",
            name = "Msheireb Metro Interchange (Exit 1)",
            category = FacilityCategory.METRO_ENTRANCE,
            latitude = 25.2868,
            longitude = 51.5315,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-12",
            notes = "Full accessibility elevator, escalator, air-conditioned concourse connecting Red, Gold, and Green lines.",
            caveats = "Accessible during metro operational hours (06:00 - 23:00, Fridays from 14:00).",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_msh_water_kahraba",
            name = "Chilled Water Refill — Al Kahraba St",
            category = FacilityCategory.WATER_REFILL,
            latitude = 25.2862,
            longitude = 51.5327,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-15",
            notes = "Public stainless steel refill station. Clean chilled drinking water. Push-button activation.",
            caveats = "Bespoke pedestrian dispenser; occasionally undergoing routine filter maintenance.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_msh_restrooms_galleria",
            name = "Public Restrooms — Msheireb Galleria (L1)",
            category = FacilityCategory.PUBLIC_TOILET,
            latitude = 25.2858,
            longitude = 51.5338,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-10",
            notes = "Fully accessible unisex and gendered restrooms. Baby changing station. High cleanliness standard.",
            caveats = "Located inside commercial atrium; subject to mall access hours (10:00 - 22:00).",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_msh_arcade_wadi",
            name = "Sikkat Wadi Msheireb Shaded Arcade",
            category = FacilityCategory.AC_WALKWAY,
            latitude = 25.2874,
            longitude = 51.5322,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-14",
            notes = "Continuous covered architectural colonnade with passive shade overhangs and mist cooling jets during peak daytime heat.",
            caveats = "Mist cooling operates during summer and peak temperature windows.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_msh_bench_square",
            name = "Stone Benches — Sahat Al Nakheel",
            category = FacilityCategory.BENCH_SEATING,
            latitude = 25.2852,
            longitude = 51.5318,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-08",
            notes = "Limestone shaded benches beneath mature date palms. Flat level granite ground.",
            caveats = "Direct afternoon sun after 15:30 on southern benches.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_sw_metro_exit",
            name = "Souq Waqif Metro Station (Exit 2)",
            category = FacilityCategory.METRO_ENTRANCE,
            latitude = 25.2879,
            longitude = 51.5358,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-11",
            notes = "Gold Line direct connection. Escalators and ramp access into Souq perimeter plaza.",
            caveats = "Heavy pedestrian foot traffic on weekend evenings.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_sw_toilets_central",
            name = "Public Restrooms — Souq Waqif Square",
            category = FacilityCategory.PUBLIC_TOILET,
            latitude = 25.2885,
            longitude = 51.5345,
            source = "OpenStreetMap Contributor",
            verificationStatus = VerificationStatus.EXTERNALLY_SOURCED,
            lastVerifiedAt = "2024-09-20",
            notes = "Free municipal public restrooms managed by Souq management. Attendant on duty.",
            caveats = "Steps at exterior entrance; accessible ramp located on north side only.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_sw_water_fountain",
            name = "Traditional Water Dispenser — Souq Waqif",
            category = FacilityCategory.WATER_REFILL,
            latitude = 25.2891,
            longitude = 51.5339,
            source = "Community Contributor",
            verificationStatus = VerificationStatus.COMMUNITY_REPORTED,
            lastVerifiedAt = "2024-10-02",
            notes = "Chilled charity Sabeel water dispenser near spices market alley.",
            caveats = "Unverified by survey team in last 30 days. Cup not guaranteed; bring refillable bottle.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        Facility(
            id = "fac_sw_pergola_rest",
            name = "Timber Pergola Rest Area — Souq West",
            category = FacilityCategory.SHELTERED_REST,
            latitude = 25.2871,
            longitude = 51.5349,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-16",
            notes = "Timber-trellised seating alcove with natural cross-ventilation. Good resting node between Msheireb and Souq.",
            caveats = "Open to ambient air; no active air cooling.",
            pilotAreaId = "doha_msheireb",
            isDemoData = true
        ),
        // Al Bidda Pilot Area
        Facility(
            id = "fac_bidda_metro",
            name = "Al Bidda Metro Hub (Red & Green)",
            category = FacilityCategory.METRO_ENTRANCE,
            latitude = 25.2932,
            longitude = 51.5173,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-05",
            notes = "Central park transfer hub with shaded escalators leading into park concourse.",
            caveats = "Elevator located at Exit 3.",
            pilotAreaId = "doha_al_bidda",
            isDemoData = true
        ),
        Facility(
            id = "fac_bidda_water",
            name = "Park Water Fountain — Al Bidda Plaza",
            category = FacilityCategory.WATER_REFILL,
            latitude = 25.2941,
            longitude = 51.5165,
            source = "Pilot Field Survey Team",
            verificationStatus = VerificationStatus.VERIFIED,
            lastVerifiedAt = "2024-11-05",
            notes = "Stainless dual-height drinking fountain and water bottle filler.",
            caveats = "Unshaded during midday hours.",
            pilotAreaId = "doha_al_bidda",
            isDemoData = true
        )
    )

    val INITIAL_BARRIERS = listOf(
        BarrierReport(
            id = "bar_asmakh_blocked",
            category = BarrierCategory.BLOCKED_SIDEWALK,
            description = "Construction fence and generator encroaching onto sidewalk near Al Asmakh intersection, reducing width to ~45cm. Pedestrians forced to step into road.",
            latitude = 25.2855,
            longitude = 51.5342,
            photoUrl = null,
            source = "Pedestrian Field Survey",
            status = ReportStatus.VERIFIED,
            priority = ReviewPriority.HIGH,
            priorityRationale = "Forces vulnerable pedestrians into active vehicular traffic lane without warning signs.",
            uncertaintyNotes = "Hoarding marked for 3-week utility works as of Nov 10.",
            createdAt = System.currentTimeMillis() - 86400000L * 2,
            pilotAreaId = "doha_msheireb",
            moderatorNotes = "Verified on-site by curator. Traffic barrier recommended.",
            isDemoData = true
        ),
        BarrierReport(
            id = "bar_kahraba_curb",
            category = BarrierCategory.MISSING_CURB_RAMP,
            description = "14cm sharp vertical curb without drop ramp at corner of crossing towards Al Asmakh. Barrier for wheelchairs and baby strollers.",
            latitude = 25.2863,
            longitude = 51.5339,
            photoUrl = null,
            source = "Resident Report (Accessibility Survey)",
            status = ReportStatus.VERIFIED,
            priority = ReviewPriority.HIGH,
            priorityRationale = "Blocks step-free traversal between Msheireb core and Souq Waqif perimeter.",
            uncertaintyNotes = "Alternate ramp exists 60m west at mid-block crossing.",
            createdAt = System.currentTimeMillis() - 86400000L * 5,
            pilotAreaId = "doha_msheireb",
            moderatorNotes = "Curator verified Nov 12. Included in municipal issue list.",
            isDemoData = true
        ),
        BarrierReport(
            id = "bar_souq_pavement",
            category = BarrierCategory.DAMAGED_PAVEMENT,
            description = "Loose and cracked stone cobbles along historic alleyway causing trip hazard, especially after street washing.",
            latitude = 25.2878,
            longitude = 51.5348,
            photoUrl = null,
            source = "Community Contributor",
            status = ReportStatus.UNDER_REVIEW,
            priority = ReviewPriority.MEDIUM,
            priorityRationale = "Moderate tripping risk in heavily trafficked shopping corridor.",
            uncertaintyNotes = "Awaiting physical survey check on stone displacement depth.",
            createdAt = System.currentTimeMillis() - 86400000L * 1,
            pilotAreaId = "doha_msheireb",
            moderatorNotes = "Scheduled for field inspection.",
            isDemoData = true
        ),
        BarrierReport(
            id = "bar_grand_hamad_sun",
            category = BarrierCategory.LACK_OF_SHADE,
            description = "Unshaded 220m continuous concrete sidewalk along Grand Hamad Ave with zero tree canopy or awning shelter. High heat stress.",
            latitude = 25.2882,
            longitude = 51.5367,
            photoUrl = null,
            source = "Community Contributor",
            status = ReportStatus.COMMUNITY_REPORTED,
            priority = ReviewPriority.LOW,
            priorityRationale = "Informational comfort factor; no immediate physical obstruction.",
            uncertaintyNotes = "Direct sunlight documented between 09:00 and 16:30.",
            createdAt = System.currentTimeMillis() - 86400000L * 3,
            pilotAreaId = "doha_msheireb",
            moderatorNotes = "",
            isDemoData = true
        ),
        BarrierReport(
            id = "bar_wadi_cross_unclear",
            category = BarrierCategory.UNCLEAR_CROSSING,
            description = "Faded road striping at tram track intersection near Wadi Msheireb. Vehicles often fail to yield to pedestrians crossing to metro.",
            latitude = 25.2870,
            longitude = 51.5318,
            photoUrl = null,
            source = "Metro Commuter",
            status = ReportStatus.COMMUNITY_REPORTED,
            priority = ReviewPriority.MEDIUM,
            priorityRationale = "Visual confusion between tram alignment and pedestrian desire line.",
            uncertaintyNotes = "Tram signals operate, but ground markings are worn.",
            createdAt = System.currentTimeMillis() - 86400000L * 4,
            pilotAreaId = "doha_msheireb",
            moderatorNotes = "",
            isDemoData = true
        )
    )
}
