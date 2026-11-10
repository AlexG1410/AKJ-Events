package co.edu.uniquindio.akjevents.domain.usecase.location

import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import javax.inject.Inject

class ReverseGeocodeUseCase @Inject constructor(private val repository: GeocodingRepository) {
    suspend operator fun invoke(location: Location): GeocodedAddress? {
        require(location.latitude.isFinite() && location.latitude in -90.0..90.0)
        require(location.longitude.isFinite() && location.longitude in -180.0..180.0)
        return repository.reverseGeocode(location)
    }
}
