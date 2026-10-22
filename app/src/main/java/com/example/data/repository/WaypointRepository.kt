package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.BarrierDao
import com.example.data.local.BarrierEntity
import com.example.data.local.FacilityDao
import com.example.data.local.FacilityEntity
import com.example.data.local.PilotDataSeeder
import com.example.data.local.WaypointDatabase
import com.example.data.model.AiAnalysisResult
import com.example.data.model.BarrierReport
import com.example.data.model.Facility
import com.example.data.model.LatLng
import com.example.data.model.PilotArea
import com.example.data.model.ReportStatus
import com.example.data.model.WalkingRoute
import com.example.data.remote.GeminiVisionService
import com.example.data.remote.OsrmRoutingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaypointRepository(
    private val context: Context,
    private val database: WaypointDatabase = WaypointDatabase.getDatabase(context),
    private val osrmService: OsrmRoutingService = OsrmRoutingService(),
    private val geminiService: GeminiVisionService = GeminiVisionService()
) {
    private val facilityDao: FacilityDao = database.facilityDao()
    private val barrierDao: BarrierDao = database.barrierDao()

    private val _currentPilot = MutableStateFlow(PilotArea.DEFAULT)
    val currentPilot: StateFlow<PilotArea> = _currentPilot.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    suspend fun setPilotArea(pilot: PilotArea) {
        _currentPilot.value = pilot
    }

    private suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingFacilities = facilityDao.getAllFacilities().first()
        if (existingFacilities.isEmpty()) {
            val entities = PilotDataSeeder.INITIAL_FACILITIES.map { FacilityEntity.fromDomain(it) }
            facilityDao.insertAll(entities)
        }

        val existingBarriers = barrierDao.getAllBarriers().first()
        if (existingBarriers.isEmpty()) {
            val barrierEntities = PilotDataSeeder.INITIAL_BARRIERS.map { BarrierEntity.fromDomain(it) }
            barrierDao.insertAll(barrierEntities)
        }
    }

    suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        facilityDao.clearAll()
        barrierDao.clearAll()
        val entities = PilotDataSeeder.INITIAL_FACILITIES.map { FacilityEntity.fromDomain(it) }
        facilityDao.insertAll(entities)
        val barrierEntities = PilotDataSeeder.INITIAL_BARRIERS.map { BarrierEntity.fromDomain(it) }
        barrierDao.insertAll(barrierEntities)
    }

    fun getFacilitiesForCurrentPilot(): Flow<List<Facility>> {
        return facilityDao.getFacilitiesForPilot(_currentPilot.value.id).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllFacilities(): Flow<List<Facility>> {
        return facilityDao.getAllFacilities().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getBarriersForCurrentPilot(): Flow<List<BarrierReport>> {
        return barrierDao.getBarriersForPilot(_currentPilot.value.id).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllBarriers(): Flow<List<BarrierReport>> {
        return barrierDao.getAllBarriers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun submitBarrierReport(report: BarrierReport) = withContext(Dispatchers.IO) {
        // Enforce community reported lifecycle
        val safeReport = report.copy(
            status = ReportStatus.COMMUNITY_REPORTED,
            pilotAreaId = _currentPilot.value.id,
            isDemoData = false
        )
        barrierDao.insert(BarrierEntity.fromDomain(safeReport))
    }

    suspend fun addFacility(facility: Facility) = withContext(Dispatchers.IO) {
        val safeFacility = facility.copy(pilotAreaId = _currentPilot.value.id)
        facilityDao.insert(FacilityEntity.fromDomain(safeFacility))
    }

    suspend fun updateBarrierStatus(id: String, newStatus: ReportStatus, moderatorNotes: String) = withContext(Dispatchers.IO) {
        barrierDao.updateStatus(id, newStatus.name, moderatorNotes)
    }

    suspend fun calculateWalkingRoutes(origin: LatLng, destination: LatLng): List<WalkingRoute> {
        seedInitialDataIfNeeded()
        val currentFacilities = getFacilitiesForCurrentPilot().first()
        val currentBarriers = getBarriersForCurrentPilot().first()
        return osrmService.getWalkingRoutes(origin, destination, currentFacilities, currentBarriers)
    }

    suspend fun analyzeReportWithAi(description: String, imageUri: Uri?): AiAnalysisResult {
        return geminiService.analyzeReport(context, description, imageUri)
    }
}
