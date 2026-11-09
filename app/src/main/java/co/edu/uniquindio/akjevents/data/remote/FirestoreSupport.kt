package co.edu.uniquindio.akjevents.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/** Firestore guarda las fechas como [Timestamp]; la app trabaja con [LocalDateTime] en la zona del teléfono. */
fun LocalDateTime.toTimestamp(): Timestamp = Timestamp(Date.from(atZone(ZoneId.systemDefault()).toInstant()))

fun Timestamp.toLocalDateTime(): LocalDateTime = toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

/**
 * Escucha en tiempo real los documentos de una consulta y los convierte con [map]; los documentos que no
 * se pueden leer se omiten. Si Firestore niega la lectura (por ejemplo, justo al cerrar la sesión), se
 * conserva la última lista emitida.
 */
fun <T> Query.listen(map: (DocumentSnapshot) -> T?): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        if (error == null && snapshot != null) trySend(snapshot.documents.mapNotNull { runCatching { map(it) }.getOrNull() })
    }
    awaitClose { registration.remove() }
}
