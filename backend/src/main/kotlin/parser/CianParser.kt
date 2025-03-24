// Файл: com/example/parser/CianParser.kt
package com.example.parser

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class CianParser : ListingParser {
    private val client = HttpClient(CIO)

    override suspend fun parseListings(): List<ListingParserDTO> {
        val listings = mutableListOf<ListingParserDTO>()
        // Пример URL для поиска квартир на продажу
        val url = "https://www.cian.ru/cat.php?deal_type=sale&offer_type=flat"
        val html: String = client.get(url).toString()
        val document: Document = Jsoup.parse(html)

        // Пример разбора — селекторы могут отличаться от реальных
        val items = document.select(".cian-listing-item") // условный селектор
        for (item in items) {
            val title = item.select(".cian-listing-title").text().trim()
            val priceText = item.select(".cian-listing-price").text().trim()
            val price = priceText.filter { it.isDigit() || it == '.' || it == ',' }
                .replace(',', '.')
                .toDoubleOrNull() ?: continue

            val listing = ListingParserDTO(
                title = title,
                price = price,
                cityId = 1,
                url = item.select("a").attr("href")
            )
            listings.add(listing)
        }
        return listings
    }
}
