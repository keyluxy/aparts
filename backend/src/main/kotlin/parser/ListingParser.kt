package com.example.parser


import com.example.parser.ListingParserDTO
/**
 * Интерфейс для парсера объявлений.
 */
interface ListingParser {
    /**
     * Парсит объявления с источника и возвращает список [ListingParserDTO].
     */
    suspend fun parseListings(): List<ListingParserDTO>
}



