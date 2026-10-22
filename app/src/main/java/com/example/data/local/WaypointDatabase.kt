package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FacilityEntity::class, BarrierEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WaypointDatabase : RoomDatabase() {
    abstract fun facilityDao(): FacilityDao
    abstract fun barrierDao(): BarrierDao

    companion object {
        @Volatile
        private var INSTANCE: WaypointDatabase? = null

        fun getDatabase(context: Context): WaypointDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaypointDatabase::class.java,
                    "waypoint_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
