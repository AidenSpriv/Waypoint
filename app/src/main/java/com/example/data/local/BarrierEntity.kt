package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BarrierCategory
import com.example.data.model.BarrierReport
import com.example.data.model.ReportStatus
import com.example.data.model.ReviewPriority
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "barriers")
data class BarrierEntity(
    @PrimaryKey val id: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String?,
    val source: String,
    val status: String,
    val priority: String,
    val priorityRationale: String,
    val uncertaintyNotes: String,
    val createdAt: Long,
    val pilotAreaId: String,
    val moderatorNotes: String,
    val isDemoData: Boolean
) {
    fun toDomain(): BarrierReport {
        return BarrierReport(
            id = id,
            category = try { BarrierCategory.valueOf(category) } catch (e: Exception) { BarrierCategory.BLOCKED_SIDEWALK },
            description = description,
            latitude = latitude,
            longitude = longitude,
            photoUrl = photoUrl,
            source = source,
            status = try { ReportStatus.valueOf(status) } catch (e: Exception) { ReportStatus.COMMUNITY_REPORTED },
            priority = try { ReviewPriority.valueOf(priority) } catch (e: Exception) { ReviewPriority.MEDIUM },
            priorityRationale = priorityRationale,
            uncertaintyNotes = uncertaintyNotes,
            createdAt = createdAt,
            pilotAreaId = pilotAreaId,
            moderatorNotes = moderatorNotes,
            isDemoData = isDemoData
        )
    }

    companion object {
        fun fromDomain(report: BarrierReport): BarrierEntity {
            return BarrierEntity(
                id = report.id,
                category = report.category.name,
                description = report.description,
                latitude = report.latitude,
                longitude = report.longitude,
                photoUrl = report.photoUrl,
                source = report.source,
                status = report.status.name,
                priority = report.priority.name,
                priorityRationale = report.priorityRationale,
                uncertaintyNotes = report.uncertaintyNotes,
                createdAt = report.createdAt,
                pilotAreaId = report.pilotAreaId,
                moderatorNotes = report.moderatorNotes,
                isDemoData = report.isDemoData
            )
        }
    }
}

@Dao
interface BarrierDao {
    @Query("SELECT * FROM barriers WHERE pilotAreaId = :pilotAreaId ORDER BY createdAt DESC")
    fun getBarriersForPilot(pilotAreaId: String): Flow<List<BarrierEntity>>

    @Query("SELECT * FROM barriers ORDER BY createdAt DESC")
    fun getAllBarriers(): Flow<List<BarrierEntity>>

    @Query("SELECT * FROM barriers WHERE id = :id")
    suspend fun getBarrierById(id: String): BarrierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barrier: BarrierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(barriers: List<BarrierEntity>)

    @Update
    suspend fun update(barrier: BarrierEntity)

    @Query("UPDATE barriers SET status = :status, moderatorNotes = :notes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, notes: String)

    @Query("DELETE FROM barriers WHERE isDemoData = 1")
    suspend fun clearDemoBarriers()

    @Query("DELETE FROM barriers")
    suspend fun clearAll()
}
