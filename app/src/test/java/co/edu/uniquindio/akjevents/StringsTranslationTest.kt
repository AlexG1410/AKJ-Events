package co.edu.uniquindio.akjevents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Inglés (values) y español (values-es) deben tener los mismos textos y los mismos argumentos. */
class StringsTranslationTest {

    private fun resources(folder: String): Map<String, List<String>> {
        val file = sequenceOf("src/main/res/$folder/strings.xml", "app/src/main/res/$folder/strings.xml")
            .map(::File).first { it.exists() }
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val strings = document.getElementsByTagName("string").let { nodes ->
            (0 until nodes.length).map { nodes.item(it) as Element }
                .filter { it.getAttribute("translatable") != "false" }
                .associate { it.getAttribute("name") to listOf(it.textContent) }
        }
        val plurals = document.getElementsByTagName("plurals").let { nodes ->
            (0 until nodes.length).map { nodes.item(it) as Element }.associate { plural ->
                val items = plural.getElementsByTagName("item")
                "plurals/" + plural.getAttribute("name") to (0 until items.length).map { items.item(it).textContent }
            }
        }
        return strings + plurals
    }

    private fun placeholders(texts: List<String>): Set<String> =
        texts.flatMap { Regex("""%(\d\$)?[sd]""").findAll(it).map { match -> match.value }.toList() }.toSet()

    @Test
    fun englishAndSpanishHaveTheSameKeys() {
        val english = resources("values")
        val spanish = resources("values-es")

        assertEquals("Faltan en español", emptySet<String>(), english.keys - spanish.keys)
        assertEquals("Faltan en inglés", emptySet<String>(), spanish.keys - english.keys)
        assertTrue(english.size > 500)
    }

    @Test
    fun translationsKeepTheSameArguments() {
        val english = resources("values")
        val spanish = resources("values-es")

        val different = english.keys.filter { placeholders(english.getValue(it)) != placeholders(spanish.getValue(it)) }
        assertEquals("Argumentos distintos entre idiomas", emptyList<String>(), different)
    }
}
