package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import org.w3c.dom.Element
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * ResourceProvider para pruebas en la JVM (guía 14): lee los textos en español de values-es/strings.xml,
 * así las pruebas comprueban los mensajes reales que ve el usuario con el teléfono en español.
 */
object TestResourceProvider : ResourceProvider {
    private val spanish = Locale.forLanguageTag("es-CO")

    private val file: File by lazy {
        // Gradle ejecuta las pruebas desde app/; otras herramientas, desde la raíz del proyecto
        sequenceOf("src/main/res/values-es/strings.xml", "app/src/main/res/values-es/strings.xml")
            .map(::File)
            .firstOrNull { it.exists() }
            ?: error("No se encontró values-es/strings.xml desde ${File(".").absolutePath}")
    }

    private val document by lazy { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file) }

    private val strings: Map<String, String> by lazy {
        val nodes = document.getElementsByTagName("string")
        (0 until nodes.length).map { nodes.item(it) as Element }.associate { it.getAttribute("name") to unescape(it.textContent) }
    }

    private val plurals: Map<String, Map<String, String>> by lazy {
        val nodes = document.getElementsByTagName("plurals")
        (0 until nodes.length).map { nodes.item(it) as Element }.associate { plural ->
            val items = plural.getElementsByTagName("item")
            plural.getAttribute("name") to (0 until items.length).map { items.item(it) as Element }
                .associate { it.getAttribute("quantity") to unescape(it.textContent) }
        }
    }

    // Nombre del recurso a partir de su id, con las clases R generadas
    private val stringNames: Map<Int, String> by lazy { namesOf(R.string::class.java) }
    private val pluralNames: Map<Int, String> by lazy { namesOf(R.plurals::class.java) }

    override fun getString(id: Int, vararg args: Any): String {
        val name = stringNames[id] ?: error("Recurso de texto desconocido: $id")
        val text = strings[name] ?: error("Falta el texto en español: $name")
        return if (args.isEmpty()) text else String.format(spanish, text, *args)
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg args: Any): String {
        val name = pluralNames[id] ?: error("Plural desconocido: $id")
        val forms = plurals[name] ?: error("Falta el plural en español: $name")
        val text = (if (quantity == 1) forms["one"] else forms["other"]) ?: error("Falta una forma del plural $name")
        return String.format(spanish, text, *args)
    }

    private fun namesOf(type: Class<*>): Map<Int, String> =
        type.fields.associate { it.getInt(null) to it.name }

    private fun unescape(text: String): String = text.trim('"').replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n").replace("\\@", "@")
}
