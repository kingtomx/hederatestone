package org.mvp.hedera.commons

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object LocalDateParser {
    val format =  DateTimeFormatter.BASIC_ISO_DATE

    fun basicIsoParse(date: String): LocalDate{
        return try{
            LocalDate.parse(date, format)
        } catch (d: DateTimeParseException){
            LocalDate.now()
        }
    }
}