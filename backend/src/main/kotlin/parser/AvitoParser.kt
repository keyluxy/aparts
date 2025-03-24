// Файл: com/example/parser/AvitoParser.kt
package com.example.parser

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import io.ktor.client.statement.*

class AvitoParser : ListingParser {
    private val client = HttpClient(CIO)

    override suspend fun parseListings(): List<ListingParserDTO> {
        val listings = mutableListOf<ListingParserDTO>()
        val url = "https://www.avito.ru/moskva/kvartiry/prodam"

        // Добавляем заголовок User-Agent
        val html: String = client.get(url) {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36")
        }.bodyAsText()
        println("Получен HTML длиной: ${html.length}")

        val document: Document = Jsoup.parse(html)

        // Обновите селекторы в соответствии с актуальной структурой сайта
        val items = document.select(".iva-item-root") // Проверьте актуальность этого селектора
        println("Найдено элементов: ${items.size}")

        for (item in items) {
            val title = item.select(".iva-item-titleStep-2").text().trim()
            val priceText = item.select(".price-text-1HrJ_").text().trim()

            val price = priceText.filter { it.isDigit() || it == '.' || it == ',' }
                .replace(',', '.')
                .toDoubleOrNull() ?: continue

            val relativeUrl = item.select("a").attr("href")
            val fullUrl = if (relativeUrl.startsWith("http")) relativeUrl else "https://www.avito.ru$relativeUrl"

            val listing = ListingParserDTO(
                title = title,
                price = price,
                cityId = 1,
                url = fullUrl
            )
            listings.add(listing)
            println("Parsed listing: $listing")
        }
        return listings
    }
}
