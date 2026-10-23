package co.edu.uniquindio.akjevents.core.util

import java.text.Normalizer

/** Minúsculas y sin tildes, para que "academico" encuentre "Académico". */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
