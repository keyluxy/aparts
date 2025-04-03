//package com.example.service
//
//import org.apache.poi.ss.usermodel.WorkbookFactory
//import java.io.FileInputStream
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.time.format.DateTimeParseException
//
//class ExcelReaderService {
//    fun readAvitoListings(): List<Map<String, Any>> {
//        val excelPath = "/home/keylux/AMR/apart/parse/parser_avito/result/all.xlsx"
//        val listings = mutableListOf<Map<String, Any>>()
//
//        try {
//            FileInputStream(excelPath).use { fis ->
//                val workbook = WorkbookFactory.create(fis)
//                val sheet = workbook.getSheetAt(0)
//
//                // Получаем заголовки из первой строки
//                val headers = sheet.getRow(0).map { it.stringCellValue }
//
//                // Выводим заголовки для отладки
//                println("Excel headers: ${headers.joinToString(", ")}")
//
//                // Маппинг заголовков Excel в названия полей базы данных
//                val columnMapping = mapOf(
//                    "Название" to "title",
//                    "Цена" to "price",
//                    "URL" to "url",
//                    "Описание" to "description",
//                    "Просмотров" to "views",
//                    "Дата публикации" to "publication_date",
//                    "Продавец" to "seller",
//                    "Адрес" to "address",
//                    "Ссылка на продавца" to "seller_url"
//                )
//
//                // Читаем данные начиная со второй строки
//                for (rowIndex in 1..sheet.lastRowNum) {
//                    val row = sheet.getRow(rowIndex) ?: continue
//                    val rowData = mutableMapOf<String, Any>()
//
//                    // Добавляем дефолтные значения для обязательных полей
//                    rowData["source_id"] = 1 // Avito
//                    rowData["currency"] = "RUB"
//                    rowData["city_id"] = 1 // Дефолтный город
//                    rowData["user_id"] = 1 // Дефолтный пользователь
//
//                    headers.forEachIndexed { index, header ->
//                        val cell = row.getCell(index)
//                        val dbFieldName = columnMapping[header] ?: header
//
//                        when (cell?.cellType) {
//                            org.apache.poi.ss.usermodel.CellType.STRING -> {
//                                val value = cell.stringCellValue
//                                // Проверяем, является ли это датой публикации
//                                if (dbFieldName == "publication_date") {
//                                    try {
//                                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
//                                        val dateTime = LocalDateTime.parse(value, formatter)
//                                        rowData[dbFieldName] = dateTime
//                                    } catch (e: DateTimeParseException) {
//                                        rowData[dbFieldName] = LocalDateTime.now()
//                                    }
//                                } else {
//                                    rowData[dbFieldName] = value
//                                }
//                            }
//                            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
//                                val value = cell.numericCellValue
//                                rowData[dbFieldName] = value
//                            }
//                            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> {
//                                val value = cell.booleanCellValue
//                                rowData[dbFieldName] = value
//                            }
//                            else -> rowData[dbFieldName] = ""
//                        }
//                    }
//
//                    // Преобразуем цену из строки в число, если необходимо
//                    if (rowData["price"] is String) {
//                        val priceStr = rowData["price"] as String
//                        val priceNum = priceStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
//                        rowData["price"] = priceNum
//                    }
//
//                    // Преобразуем просмотры из строки в число, если необходимо
//                    if (rowData["views"] is String) {
//                        val viewsStr = rowData["views"] as String
//                        val viewsNum = viewsStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
//                        rowData["views"] = viewsNum
//                    }
//
//                    listings.add(rowData)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            throw Exception("Ошибка при чтении Excel файла: ${e.message}")
//        }
//
//        return listings
//    }
//} 