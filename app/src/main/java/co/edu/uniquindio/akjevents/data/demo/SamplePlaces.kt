package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.Location

/** Lugar conocido que se puede buscar en el mapa simulado. */
data class Place(val name: String, val address: String, val city: String, val location: Location)

/** Ciudad con su centro, para el registro, el filtro "en mi ciudad" y las etiquetas del mapa. */
data class City(val name: String, val location: Location)

/** Lugares de referencia del Eje Cafetero; reemplazan la búsqueda de un servicio de mapas. */
object SamplePlaces {
    val cities: List<City> = listOf(
        City("Armenia, Quindío", Location(4.5339, -75.6811)),
        City("Calarcá, Quindío", Location(4.5296, -75.6431)),
        City("Circasia, Quindío", Location(4.6189, -75.6356)),
        City("Montenegro, Quindío", Location(4.5664, -75.7506)),
        City("Salento, Quindío", Location(4.6378, -75.5703)),
        City("Filandia, Quindío", Location(4.6747, -75.6583)),
        City("Quimbaya, Quindío", Location(4.6231, -75.7628)),
        City("La Tebaida, Quindío", Location(4.4531, -75.7880)),
        City("Pereira, Risaralda", Location(4.8133, -75.6961))
    )

    val places: List<Place> = listOf(
        Place("Plaza de Bolívar", "Plaza de Bolívar, Cra. 13 # 20-00", "Armenia, Quindío", Location(4.5339, -75.6811)),
        Place("Parque Sucre", "Parque Sucre, Cra. 14 # 13-00", "Armenia, Quindío", Location(4.5382, -75.6731)),
        Place("Parque de la Vida", "Parque de la Vida, Av. Bolívar", "Armenia, Quindío", Location(4.5511, -75.6598)),
        Place("Parque Fundadores", "Parque Fundadores, Av. Bolívar # 1N", "Armenia, Quindío", Location(4.5452, -75.6672)),
        Place("Biblioteca Pública Municipal", "Biblioteca Pública, Cra. 17 # 16-00", "Armenia, Quindío", Location(4.5356, -75.6762)),
        Place("Universidad del Quindío", "Universidad del Quindío, Cra. 15 Calle 12N", "Armenia, Quindío", Location(4.5536, -75.6614)),
        Place("Centro de Innovación", "Centro de Innovación, Calle 21 # 15-20", "Armenia, Quindío", Location(4.5320, -75.6750)),
        Place("Estadio Centenario", "Estadio Centenario, Av. Centenario", "Armenia, Quindío", Location(4.5208, -75.6858)),
        Place("Cancha Barrio Granada", "Cancha Barrio Granada, Calle 50", "Armenia, Quindío", Location(4.5150, -75.6920)),
        Place("Casa comunal Centro", "Casa comunal, Calle 19 # 16-30", "Armenia, Quindío", Location(4.5410, -75.6700)),
        Place("Jardín Botánico del Quindío", "Jardín Botánico, Km 3 vía Valle del Cocora", "Calarcá, Quindío", Location(4.5186, -75.6425)),
        Place("Plaza principal de Calarcá", "Parque Bolívar de Calarcá", "Calarcá, Quindío", Location(4.5296, -75.6431)),
        Place("Valle de Cocora", "Valle de Cocora, vía Salento", "Salento, Quindío", Location(4.6380, -75.4870)),
        Place("Parque principal de Circasia", "Parque principal de Circasia", "Circasia, Quindío", Location(4.6189, -75.6356)),
        Place("Universidad Tecnológica de Pereira", "UTP, Cra. 27 # 10-02", "Pereira, Risaralda", Location(4.7946, -75.6900))
    )

    /** Ciudad más cercana a un punto, para nombrar las ubicaciones elegidas en el mapa. */
    fun nearestCity(location: Location): City = cities.minBy { it.location.distanceKmTo(location) }

    fun cityNamed(name: String): City? = cities.find { it.name == name }
}
