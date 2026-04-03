package edu.nd.pmcburne.hello

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val tags: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

@Dao
interface LocationDao {
    @Upsert
    suspend fun upsertLocations(locations: List<LocationEntity>)

    @Query("SELECT * FROM locations WHERE tags LIKE '%' || :tag || '%'")
    suspend fun getLocationsByTag(tag: String): List<LocationEntity>

    @Query("SELECT DISTINCT tags FROM locations")
    suspend fun getAllTags(): List<String>

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationCount(): Int
}

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}




