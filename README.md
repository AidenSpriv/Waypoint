# WAYPOINT — Neighborhood Walking MVP

> **"Know what your walk will actually be like."**

WAYPOINT is a civic technology application designed to help people make better short walking journeys through their neighborhoods by combining walking route calculations, nearby useful facilities (PausePoints), and community-verified pedestrian barrier reports.

Our initial pilot zone is centered on **Doha, Qatar** (Msheireb Downtown, Souq Waqif, and Al Bidda Metro hubs).

---

## Key Features:

1. **Explore Map (Map & Route Intelligence)**
   - High-performance interactive Leaflet map with OpenStreetMap tiles.
   - Live pedestrian walking directions via OSRM (OpenStreetMap Walking Routing Engine).
   - Proximity analysis: Calculates known PausePoints and reported barriers intersecting or within buffer distances of each route.
   - Transparent, explainable route comparisons (e.g. *"Route is 120m longer than Alternative, offers 2 more PausePoints, and avoids 1 reported obstacle"*).
   - Map controls: Recenter on pilot area, zoom controls, location picker mode, and category markers.

2. **PausePoints (Neighborhood Facility Discovery)**
   - Curated directory of walking amenities:
     - 💧 Public Water Refill stations (chilled dispensers)
     - 🚻 Public Toilets (municipal & atrium facilities)
     - 🪑 Benches & Seating (shaded rest nodes)
     - ☂️ Sheltered Rest Areas (timber pergolas & shaded corridors)
     - 🚇 Metro & Public Transit Entrances (Msheireb & Souq Waqif interchanges)
     - ❄️ Air-conditioned Walkways & Misted Arcades (Sikkat Wadi Msheireb)
   - Clear provenance badges: *Field Survey Verified*, *Community Reported*, or *Externally Sourced (OSM)*.
   - Explicit operational hours and access caveats.

3. **Community Barrier Reporting**
   - Step-by-step reporting flow:
     1. Select location (tap on map or use pilot center).
     2. Select category: Blocked sidewalk, Damaged pavement, Missing curb ramp, Unclear crossing, Lack of shade, Construction detour.
     3. Add description & optional photo attachment.
     4. Review priority: Low, Medium, High, Urgent.
     5. Lifecycle enforcement: New reports default to **Community Reported** and are never automatically marked verified.
   - Stored locally and persistently in Room database.

4. **AI Report Assistant**
   - Multimodal infrastructure classifier powered by Google Gemini (`gemini-3.5-flash`).
   - Analyzes user photo and description to suggest the most accurate category, generate a neutral 1-sentence summary, highlight uncertainty caveats (e.g. inability to gauge curb height from a 2D image), and suggest review priority.
   - Fallback protection: If no `GEMINI_API_KEY` is configured in the Secrets panel, the app automatically switches to an honest, clearly labeled deterministic demo classifier.

5. **Curator Workbench (Moderator & Pilot Management)**
   - Configurable Pilot Area switcher:
     - *Doha — Msheireb Downtown & Souq Waqif* (Default)
     - *Doha — Al Bidda Park & Corniche*
     - *Doha — Education City & Qatar National Library*
   - Curator triage tools to advance barrier report status: *Under Review*, *Confirm Hazard (Verified)*, *Clear/Resolved*, or *Rejected*.
   - One-click *Reset Pilot Baseline Data* button.

---

## Architecture & Technology Stack

- **Platform:** Android (Min SDK 24, Target SDK 36)
- **UI Framework:** Jetpack Compose with Material Design 3
- **Design System:** Civic Dark foundation (`#0D131F`, `#131B2A`), warm off-white surfaces (`#F8F9FA`), and restrained lime-green accents (`#84CC16`)
- **Mapping:** Self-contained Leaflet 1.9.4 inside Android WebView with bidirectional Kotlin bridge (`WaypointMapBridge`)
- **Local Persistence:** Room Database (`WaypointDatabase`) with Flow-based reactive updates
- **Routing Engine:** OSRM Pedestrian Walking API with surveyed local street network fallback
- **AI Classification:** Google Gemini API (`gemini-3.5-flash`) via Retrofit & OkHttp with 60s timeouts

---

## Configuration & Environment Variables

API keys are managed securely via the **Secrets panel in AI Studio** and accessed via `BuildConfig`:

- `GEMINI_API_KEY`: Used for the AI Report Assistant multimodal classification.
  - Set your Gemini API key in the Secrets panel.
  - A template is provided in `.env.example`.
  - If omitted, the app continues to operate with a labeled deterministic heuristic fallback.

---

## Demonstrating the MVP

1. **Explore the Map:**
   - Launch the app. It opens directly into the Doha Msheireb pilot area with an active walking route calculated between Msheireb Metro (A) and Souq Waqif Metro (B).
   - Tap "Compare (2)" to open the Route Comparison sheet and see why the routes differ in amenities and hazards.
   - Tap any marker on the map (e.g. the 💧 chilled water dispenser or 🚫 blocked sidewalk) to open its detail sheet.
2. **Explore PausePoints:**
   - Switch to the "PausePoints" tab to filter amenities by category or search by keywords.
   - Tap "Map" on any card to jump directly to it on the interactive map.
3. **Submit a Community Report:**
   - Switch to the "Report" tab.
   - Tap "Pick on Map" or "Pilot Center" to set the coordinate.
   - Type a description such as *"Broken pavement tiles on tram track crossing"* and tap "Analyze with AI Assistant".
   - Review the AI suggestions and tap "Submit Community Report".
   - Notice the success confirmation and tap "View on Map" to see your new report pinned on the map with status "Community Reported".
4. **Curator Workbench:**
   - Switch to the "Curator" tab to view all triage reports.
   - As a moderator, tap "Review" or "Confirm Hazard" to advance report lifecycle.
   - Test switching pilot neighborhoods (e.g. Al Bidda Park or Education City).
   - Use "Reset Pilot Baseline Data" to restore the clean baseline at any time.
