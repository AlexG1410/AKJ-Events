package co.edu.uniquindio.akjevents.core.util

import co.edu.uniquindio.akjevents.domain.model.Location

/** Punto elegido en el mapa, con el nombre de la dirección que se mostrará. */
data class PickedLocation(val address: String, val location: Location)
