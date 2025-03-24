// Файл: com/example/routes/ParsingRoutes.kt
package com.example.routes

import com.example.parser.AvitoParser
import com.example.parser.CianParser
import com.example.service.ListingService
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun Route.parsingRoutes() {
    get("/parse") {
        // Запускаем процесс парсинга в корутине на диспетчере IO
        CoroutineScope(Dispatchers.IO).launch {
            val avitoParser = AvitoParser()
//            val cianParser = CianParser()

            // Получаем списки объявлений
            val avitoListings = avitoParser.parseListings()
//            val cianListings = cianParser.parseListings()

            // Сохраняем объявления, указывая идентификатор источника (например, 1 — Avito, 2 — Cian)
            avitoListings.forEach { ListingService.saveListing(it, sourceId = 1) }
//            cianListings.forEach { ListingService.saveListing(it, sourceId = 2) }
        }
        call.respondText("Запущен процесс парсинга")
    }
}

