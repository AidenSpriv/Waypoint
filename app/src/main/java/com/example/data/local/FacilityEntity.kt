package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.data.model.Facility
import com.example.data.model.FacilityCategory
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val source: String,
    val verificationStatus: String,
    val lastVerifiedAt: String,
    val notes: String,
    val caveats: String,
    val pilotAreaId: String,
    val isDemoData: Boolean
) {
    fun toDomain(): Facility {
        return Facility(
            id = id,
            name = name,
            category = try { FacilityCategory.valueOf(category) } catch (e: Exception) { FacilityCategory.WATER_REFILL },
            latitude = latitude,
            longitude = longitude,
            source = source,
            verificationStatus = try { VerificationStatus.valueOf(verificationStatus) } catch (e: Exception) { VerificationStatus.COMMUNITY_REPORTED },
            lastVerifiedAt = lastVerifiedAt,
            notes = notes,
            caveats = caveats,
            pilotAreaId = pilotAreaId,
            isDemoData = isDemoData
        )
    }

    companion object {
        fun fromDomain(facility: Facility): FacilityEntity {
            return FacilityEntity(
                id = facility.id,
                name = facility.name,
                category = facility.category.name,
                latitude = facility.latitude,
                longitude = facility.longitude,
                source = facility.source,
                verificationStatus = facility.verificationStatus.name,
                lastVerifiedAt = facility.lastVerifiedAt,
                notes = facility.notes,
                caveats = facility.caveats,
                pilotAreaId = facility.pilotAreaId,
                isDemoData = facility.isDemoData
            )
        }
    }
}

@Dao
interface FacilityDao {
    @Query("SELECT * FROM facilities WHERE pilotAreaId = :pilotAreaId")
    fun getFacilitiesForPilot(pilotAreaId: String): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities WHERE id = :id")
    suspend fun getFacilityById(id: String): FacilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facilities: List<FacilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(facility: FacilityEntity)

    @Query("DELETE FROM facilities")
    suspend fun clearAll()
}
