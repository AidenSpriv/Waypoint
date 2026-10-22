package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiAnalysisResult
import com.example.data.model.BarrierCategory
import com.example.data.model.BarrierReport
import com.example.data.model.Facility
import com.example.data.model.FacilityCategory
import com.example.data.model.LatLng
import com.example.data.model.PilotArea
import com.example.data.model.ReportStatus
import com.example.data.model.WalkingRoute
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.local.UserProfilePreferences
import com.example.data.local.UserProfileData
import com.example.data.remote.GeoUtils
import com.example.data.remote.OpenRouterAiService
import com.example.data.repository.WaypointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppTab(val title: String) {
    MAP("Explore"),
    ROUTES("Routes"),
    PAUSE_POINTS("Facilities"),
    REPORT("Reports"),
    PROFILE("Profile"),
    REVIEW("Curator")
}

enum class MapPickMode {
    NONE,
    PICK_ORIGIN,
    PICK_DESTINATION,
    PICK_REPORT_LOCATION
}

data class MapUiState(
    val selectedFacility: Facility? = null,
    val selectedBarrier: BarrierReport? = null,
    val droppedPin: LatLng? = null,
    val droppedPinName: String = "",
    val origin: LatLng? = null,
    val originName: String = "",
    val destination: LatLng? = null,
    val destinationName: String = "",
    val routes: List<WalkingRoute> = emptyList(),
    val selectedRouteIndex: Int = 0,
    val isCalculatingRoutes: Boolean = false,
    val routeError: String? = null,
    val pickMode: MapPickMode = MapPickMode.NONE,
    val filterFacilityCategory: FacilityCategory? = null,
    val showBarriersOnMap: Boolean = true,
    val mapStyle: String = "google_streets",
    val isSheetExpanded: Boolean = false
)

data class ReportFormState(
    val category: BarrierCategory = BarrierCategory.BLOCKED_SIDEWALK,
    val description: String = "",
    val location: LatLng? = null,
    val locationLabel: String = "Tap map or select below",
    val photoUri: Uri? = null,
    val aiAnalysis: AiAnalysisResult? = null,
    val isAiAnalyzing: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

class WaypointViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WaypointRepository(application)

    val currentPilot: StateFlow<PilotArea> = repository.currentPilot

    private val _currentTab = MutableStateFlow(AppTab.MAP)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _mapState = MutableStateFlow(MapUiState())
    val mapState: StateFlow<MapUiState> = _mapState.asStateFlow()

    private val _reportForm = MutableStateFlow(ReportFormState())
    val reportForm: StateFlow<ReportFormState> = _reportForm.asStateFlow()

    private val _facilityCategoryFilter = MutableStateFlow<FacilityCategory?>(null)
    val facilityCategoryFilter: StateFlow<FacilityCategory?> = _facilityCategoryFilter.asStateFlow()

    private val _facilitySearchQuery = MutableStateFlow("")
    val facilitySearchQuery: StateFlow<String> = _facilitySearchQuery.asStateFlow()

    private val _barrierStatusFilter = MutableStateFlow<ReportStatus?>(null)
    val barrierStatusFilter: StateFlow<ReportStatus?> = _barrierStatusFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Persistent User Profile and Onboarding
    private val userProfilePrefs = UserProfilePreferences(application)
    private val _userProfile = MutableStateFlow(userProfilePrefs.getUserProfile())
    val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private val _isReplayingOnboarding = MutableStateFlow(false)
    val isReplayingOnboarding: StateFlow<Boolean> = _isReplayingOnboarding.asStateFlow()

    val showOnboardingModal: StateFlow<Boolean> = _isReplayingOnboarding.asStateFlow()

    fun completeOnboarding(profile: UserProfileData) {
        val updated = profile.copy(isOnboardingCompleted = true)
        userProfilePrefs.saveUserProfile(updated)
        _userProfile.value = updated
        _isReplayingOnboarding.value = false
        _statusMessage.value = "Welcome to WAYPOINT, ${updated.name.split(" ").firstOrNull() ?: "Navigator"}!"
    }

    fun dismissOnboarding() {
        _isReplayingOnboarding.value = false
    }

    fun dismissReplayOnboarding() {
        _isReplayingOnboarding.value = false
    }

    fun triggerOnboarding() {
        _isReplayingOnboarding.value = true
    }

    fun updateUserProfile(profile: UserProfileData) {
        userProfilePrefs.saveUserProfile(profile)
        _userProfile.value = profile
        _statusMessage.value = "Profile settings updated."
    }

    // Facilities for current pilot filtered
    val filteredFacilities: StateFlow<List<Facility>> = combine(
        repository.getFacilitiesForCurrentPilot(),
        _facilityCategoryFilter,
        _facilitySearchQuery
    ) { facilities, categoryFilter, query ->
        facilities.filter { fac ->
            (categoryFilter == null || fac.category == categoryFilter) &&
            (query.isBlank() || fac.name.contains(query, ignoreCase = true) || fac.notes.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Barriers for current pilot filtered
    val filteredBarriers: StateFlow<List<BarrierReport>> = combine(
        repository.getBarriersForCurrentPilot(),
        _barrierStatusFilter
    ) { barriers, statusFilter ->
        barriers.filter { bar ->
            statusFilter == null || bar.status == statusFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Chat Navigation Assistant
    private val openRouterAiService = OpenRouterAiService()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Hello! I am Wayfinder, your intelligent walking navigator for WAYPOINT in Doha. I have live access to your routes, shade levels, water refill points, and walking conditions. How can I assist you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendAiChatMessage(userText: String, isWalkingActive: Boolean = false) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isAiThinking.value) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val currentMapState = _mapState.value
                val activeRoute = currentMapState.routes.getOrNull(currentMapState.selectedRouteIndex)
                val responseText = openRouterAiService.askAssistant(
                    userPrompt = trimmed,
                    history = _chatMessages.value,
                    pilotArea = currentPilot.value,
                    activeRoute = activeRoute,
                    originName = currentMapState.originName,
                    destinationName = currentMapState.destinationName,
                    facilities = filteredFacilities.value,
                    barriers = filteredBarriers.value,
                    isWalkingActive = isWalkingActive
                )
                val aiMsg = ChatMessage(
                    sender = MessageSender.AI,
                    text = responseText,
                    relatedRouteSummary = activeRoute?.let { "${it.name} · ${it.shadeShelterCoveragePercent}% shade" }
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.AI,
                    text = "I had trouble connecting to the AI service. Please check connection and try again."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearChatMessages() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Conversation refreshed. I have your latest route and Doha walking telemetry ready. What would you like to know?"
            )
        )
    }

    init {
        // Start with clean initial state: wait for user to search destination
    }

    private fun setInitialPilotEndpoints(pilot: PilotArea) {
        // Available if user resets or selects pilot preset
        val origin = LatLng(25.2868, 51.5315) // Msheireb Metro Exit 1
        val dest = LatLng(25.2879, 51.5358)   // Souq Waqif Metro Exit 2
        _mapState.value = _mapState.value.copy(
            origin = origin,
            originName = "Msheireb Metro Interchange",
            destination = dest,
            destinationName = "Souq Waqif Metro Plaza",
            pickMode = MapPickMode.NONE
        )
        calculateWalkingRoute(origin, dest)
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun switchPilotArea(pilot: PilotArea) {
        viewModelScope.launch {
            repository.setPilotArea(pilot)
            _mapState.value = MapUiState() // Reset map state
            // Set default points for new pilot
            val orig = LatLng(pilot.centerLat - 0.003, pilot.centerLng - 0.003)
            val dst = LatLng(pilot.centerLat + 0.003, pilot.centerLng + 0.003)
            _mapState.value = _mapState.value.copy(
                origin = orig,
                originName = "${pilot.shortName} West",
                destination = dst,
                destinationName = "${pilot.shortName} East"
            )
            calculateWalkingRoute(orig, dst)
            _statusMessage.value = "Switched to ${pilot.name}"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun selectFacility(facility: Facility?) {
        _mapState.value = _mapState.value.copy(
            selectedFacility = facility,
            selectedBarrier = null
        )
    }

    fun selectBarrier(barrier: BarrierReport?) {
        _mapState.value = _mapState.value.copy(
            selectedBarrier = barrier,
            selectedFacility = null
        )
    }

    fun setPickMode(mode: MapPickMode) {
        _mapState.value = _mapState.value.copy(pickMode = mode)
    }

    fun handleMapClick(lat: Double, lng: Double) {
        val point = LatLng(lat, lng)
        when (_mapState.value.pickMode) {
            MapPickMode.PICK_ORIGIN -> {
                _mapState.value = _mapState.value.copy(
                    origin = point,
                    originName = String.format("Pin (%.4f, %.4f)", lat, lng),
                    pickMode = MapPickMode.NONE
                )
                _mapState.value.destination?.let { dest ->
                    calculateWalkingRoute(point, dest)
                }
            }
            MapPickMode.PICK_DESTINATION -> {
                _mapState.value = _mapState.value.copy(
                    destination = point,
                    destinationName = String.format("Pin (%.4f, %.4f)", lat, lng),
                    pickMode = MapPickMode.NONE
                )
                _mapState.value.origin?.let { orig ->
                    calculateWalkingRoute(orig, point)
                }
            }
            MapPickMode.PICK_REPORT_LOCATION -> {
                _reportForm.value = _reportForm.value.copy(
                    location = point,
                    locationLabel = String.format("Lat: %.4f, Lng: %.4f", lat, lng)
                )
                _mapState.value = _mapState.value.copy(pickMode = MapPickMode.NONE)
                _currentTab.value = AppTab.REPORT
            }
            MapPickMode.NONE -> {
                // Interactive dropped pin with live route calculation
                _mapState.value = _mapState.value.copy(
                    selectedFacility = null,
                    selectedBarrier = null,
                    droppedPin = point,
                    droppedPinName = String.format("Pin (%.4f, %.4f)", lat, lng)
                )
                _mapState.value.origin?.let { orig ->
                    calculateWalkingRoute(orig, point)
                }
            }
        }
    }

    fun setMapStyle(style: String) {
        _mapState.value = _mapState.value.copy(mapStyle = style)
    }

    fun clearDroppedPin() {
        _mapState.value = _mapState.value.copy(droppedPin = null, droppedPinName = "")
    }

    fun setDroppedPinAsOrigin() {
        _mapState.value.droppedPin?.let { pin ->
            _mapState.value = _mapState.value.copy(
                origin = pin,
                originName = _mapState.value.droppedPinName,
                droppedPin = null
            )
            _mapState.value.destination?.let { dest ->
                calculateWalkingRoute(pin, dest)
            }
        }
    }

    fun setDroppedPinAsDestination() {
        _mapState.value.droppedPin?.let { pin ->
            _mapState.value = _mapState.value.copy(
                destination = pin,
                destinationName = _mapState.value.droppedPinName,
                droppedPin = null
            )
            _mapState.value.origin?.let { orig ->
                calculateWalkingRoute(orig, pin)
            }
        }
    }

    fun setDestinationPreset(name: String, point: LatLng) {
        _mapState.value = _mapState.value.copy(
            destination = point,
            destinationName = name,
            droppedPin = null
        )
        _mapState.value.origin?.let { orig ->
            calculateWalkingRoute(orig, point)
        }
    }

    fun setCustomRoute(originName: String, originCoord: LatLng, destName: String, destCoord: LatLng) {
        _mapState.value = _mapState.value.copy(
            origin = originCoord,
            originName = originName,
            destination = destCoord,
            destinationName = destName,
            droppedPin = null
        )
        calculateWalkingRoute(originCoord, destCoord)
    }

    fun reportBarrierOnSpot(category: BarrierCategory, description: String, location: LatLng) {
        viewModelScope.launch {
            try {
                val report = BarrierReport(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    description = description.ifBlank { "Obstacle reported on the spot by pedestrian" },
                    latitude = location.latitude,
                    longitude = location.longitude,
                    pilotAreaId = currentPilot.value.id,
                    status = ReportStatus.COMMUNITY_REPORTED,
                    source = "On-the-spot Walker Report",
                    createdAt = System.currentTimeMillis()
                )
                repository.submitBarrierReport(report)
                _statusMessage.value = "⚠️ Obstacle reported on the spot. Walkers will be re-routed around this obstacle!"
            } catch (e: Exception) {
                _statusMessage.value = "Report error: ${e.message}"
            }
        }
    }

    fun toggleSheetExpanded() {
        _mapState.value = _mapState.value.copy(isSheetExpanded = !_mapState.value.isSheetExpanded)
    }

    fun clearRouteError() {
        _mapState.value = _mapState.value.copy(
            isCalculatingRoutes = false,
            routeError = null
        )
    }

    fun calculateWalkingRoute(origin: LatLng, destination: LatLng) {
        // Prevent concurrent duplicate executions
        if (_mapState.value.isCalculatingRoutes) return

        viewModelScope.launch {
            _mapState.value = _mapState.value.copy(
                isCalculatingRoutes = true,
                routeError = null
            )
            val startTime = System.currentTimeMillis()
            try {
                val routes = repository.calculateWalkingRoutes(origin, destination)
                // Tasteful minimum display duration (850ms) to prevent jarring flicker
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 850) {
                    delay(850 - elapsed)
                }
                _mapState.value = _mapState.value.copy(
                    routes = routes,
                    selectedRouteIndex = 0,
                    isCalculatingRoutes = false
                )
            } catch (e: Exception) {
                _mapState.value = _mapState.value.copy(
                    isCalculatingRoutes = false,
                    routeError = "Unable to map route: ${e.message ?: "Unknown route error"}"
                )
            }
        }
    }

    fun selectRouteIndex(index: Int) {
        if (index in _mapState.value.routes.indices) {
            _mapState.value = _mapState.value.copy(selectedRouteIndex = index)
        }
    }

    fun setFacilityFilter(category: FacilityCategory?) {
        _facilityCategoryFilter.value = category
    }

    fun setFacilitySearchQuery(query: String) {
        _facilitySearchQuery.value = query
    }

    fun setBarrierStatusFilter(status: ReportStatus?) {
        _barrierStatusFilter.value = status
    }

    // Report Form Actions
    fun updateReportCategory(cat: BarrierCategory) {
        _reportForm.value = _reportForm.value.copy(category = cat)
    }

    fun updateReportDescription(desc: String) {
        _reportForm.value = _reportForm.value.copy(description = desc)
    }

    fun updateReportPhoto(uri: Uri?) {
        _reportForm.value = _reportForm.value.copy(photoUri = uri)
    }

    fun setReportLocationDirect(latLng: LatLng, label: String) {
        _reportForm.value = _reportForm.value.copy(
            location = latLng,
            locationLabel = label
        )
    }

    fun analyzeReportWithAi() {
        val state = _reportForm.value
        if (state.description.isBlank() && state.photoUri == null) {
            _reportForm.value = state.copy(errorMessage = "Please enter a description or attach a photo for AI analysis.")
            return
        }

        viewModelScope.launch {
            _reportForm.value = _reportForm.value.copy(
                isAiAnalyzing = true,
                errorMessage = null
            )
            try {
                val result = repository.analyzeReportWithAi(state.description, state.photoUri)
                _reportForm.value = _reportForm.value.copy(
                    aiAnalysis = result,
                    category = result.suggestedCategory,
                    isAiAnalyzing = false
                )
            } catch (e: Exception) {
                _reportForm.value = _reportForm.value.copy(
                    isAiAnalyzing = false,
                    errorMessage = "AI assistance unavailable: ${e.message}"
                )
            }
        }
    }

    fun submitReport() {
        val state = _reportForm.value
        if (state.description.isBlank()) {
            _reportForm.value = state.copy(errorMessage = "Description cannot be empty.")
            return
        }
        val loc = state.location ?: LatLng(currentPilot.value.centerLat, currentPilot.value.centerLng)

        viewModelScope.launch {
            _reportForm.value = state.copy(isSubmitting = true, errorMessage = null)
            try {
                val newReport = BarrierReport(
                    id = "report_${UUID.randomUUID().toString().take(8)}",
                    category = state.category,
                    description = state.description.trim(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    photoUrl = state.photoUri?.toString(),
                    source = "Community Pedestrian",
                    status = ReportStatus.COMMUNITY_REPORTED,
                    priority = state.aiAnalysis?.suggestedPriority ?: com.example.data.model.ReviewPriority.MEDIUM,
                    priorityRationale = state.aiAnalysis?.priorityRationale ?: "Awaiting curator review.",
                    uncertaintyNotes = state.aiAnalysis?.uncertaintyNotice ?: "Submitted via community portal.",
                    pilotAreaId = currentPilot.value.id,
                    isDemoData = false
                )

                repository.submitBarrierReport(newReport)

                _reportForm.value = ReportFormState(submitSuccess = true)
                _statusMessage.value = "Report submitted successfully! Now appearing under review on map."
            } catch (e: Exception) {
                _reportForm.value = state.copy(
                    isSubmitting = false,
                    errorMessage = "Failed to save report: ${e.message}"
                )
            }
        }
    }

    fun resetReportForm() {
        _reportForm.value = ReportFormState()
    }

    // Moderator Workflow
    fun updateBarrierReportStatus(id: String, newStatus: ReportStatus, moderatorNotes: String) {
        viewModelScope.launch {
            try {
                repository.updateBarrierStatus(id, newStatus, moderatorNotes)
                _statusMessage.value = "Barrier status updated to ${newStatus.displayName}"
                // If it was selected, update map state selection
                _mapState.value.selectedBarrier?.let { current ->
                    if (current.id == id) {
                        _mapState.value = _mapState.value.copy(
                            selectedBarrier = current.copy(status = newStatus, moderatorNotes = moderatorNotes)
                        )
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update barrier: ${e.message}"
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _mapState.value = MapUiState()
            _statusMessage.value = "Demo data reset to initial pilot baseline."
        }
    }
}
