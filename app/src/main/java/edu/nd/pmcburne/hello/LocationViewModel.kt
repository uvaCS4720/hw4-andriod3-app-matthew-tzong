package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUIState(
    val tags: List<String> = emptyList(),
    val selectedTag: String = "core",
    val locations: List<LocationEntity> = emptyList(),
    val isLoading: Boolean = true
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(application, AppDatabase::class.java, "locations.db").build()
    private val dao: LocationDao = database.locationDao()
    private val service: LocationAPIService = LocationAPI.service
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            try {
                syncLocationsIfNeeded()
                _uiState.update { it.copy(tags = getAllTags()) }
                val locations = getLocationsByTag("core")
                _uiState.update { it.copy(locations = locations, isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTag(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
        viewModelScope.launch {
            val locations = getLocationsByTag(tag)
            _uiState.update { it.copy(locations = locations, isLoading = false) }
        }
    }

    private suspend fun syncLocationsIfNeeded() {
        val locations = service.getLocations()
        val entities = locations.map { location ->
            LocationEntity(
                id = location.id,
                name = location.name,
                tags = location.tagList.joinToString(","),
                description = location.description,
                latitude = location.visualCenter.latitude,
                longitude = location.visualCenter.longitude
            )
        }
        dao.upsertLocations(entities)
    }

    private suspend fun getLocationsByTag(tag: String) = dao.getLocationsByTag(tag)

    private suspend fun getAllTags(): List<String> {
        return dao.getAllTags()
            .flatMap { it.split(",") }
            .map { it.trim() }
            .distinct()
            .sorted()
    }
}